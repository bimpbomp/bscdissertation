package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.RectButtonBuilder;
import bham.student.txm683.heartbreaker.map.MapConstructor;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.messaging.MessageBus;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.EntityController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.rendering.popups.Popup;
import bham.student.txm683.heartbreaker.rendering.popups.TextBoxBuilder;
import bham.student.txm683.heartbreaker.utils.BenchMarker;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;

public class Level implements Runnable {
    private final String TAG = "hb::Level";

    private LevelView levelView;
    private LevelState levelState;
    private InputManager inputManager;
    private EntityController entityController;
    private CollisionManager collisionManager;

    private MessageBus messageBus;

    private boolean running;

    private FPSMonitor gameFPSMonitor;
    private FPSMonitor renderFPSMonitor;

    private int gameTicksPerSecond = 25;
    private int gameTickTimeStepInMillis = 1000 / gameTicksPerSecond;
    private int maxSkipTick = 10;

    private long currentGameTick;
    private long nextScheduledGameTick = System.currentTimeMillis();

    private int loops;

    private String mapName;

    private Popup diedPopup;
    private Popup completePopup;

    public Level(LevelView levelView, String mapName){
        super();

        this.mapName = mapName;

        this.messageBus = new MessageBus();

        this.levelView = levelView;

        gameFPSMonitor = new FPSMonitor();
        renderFPSMonitor = new FPSMonitor();
    }

    private void load(){
        levelView.drawLoading();

        if (this.levelState == null) {

            this.levelView.setTileSize(200);
            MapConstructor mapConstructor = new MapConstructor(levelView.getContext());

            this.levelState = new LevelState(mapConstructor.loadMap(mapName, levelView.getTileSize()));

            this.levelState.setScreenDimensions(levelView.getWidth(), levelView.getHeight());

            this.inputManager = new InputManager(levelState, levelView.getContext(), levelView);

            this.levelView.setInputManager(inputManager);
            this.levelView.setLevelState(levelState);

            levelView.initInGameUI();
            levelView.setDebugInfo(levelState.getDebugInfo());

            RectButtonBuilder[] buttonBuilders = new RectButtonBuilder[]{
                    //TODO set restart function
                    new RectButtonBuilder("Restart", 40, () -> {levelView.restartLevel();}),
                    new RectButtonBuilder("Return To Menu", 60, () -> levelView.returnToMenu())
            };

            TextBoxBuilder[] textBuilders = new TextBoxBuilder[]{
                    new TextBoxBuilder("You Died...", 20, 60, Color.RED)
            };

            this.diedPopup = inputManager.createMOSPopup(buttonBuilders, textBuilders);

            buttonBuilders = new RectButtonBuilder[]{
                    new RectButtonBuilder("Return To Menu", 60, () -> levelView.returnToMenu())
            };

            textBuilders = new TextBoxBuilder[]{
                    new TextBoxBuilder("Success!", 20, 60, Color.GREEN)
            };

            this.completePopup = inputManager.createMOSPopup(buttonBuilders, textBuilders);

            //initialise systems
            entityController = new EntityController(levelState);
            collisionManager = new CollisionManager(levelState);

            levelState.setAiManager(new AIManager(levelState, levelState.getAliveAIEntities()));
        }

        this.setRunning(true);
        levelState.setReadyToRender(true);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e){
            //do nothing
        }
    }

    @Override
    public void run(){

        load();

        nextScheduledGameTick = System.currentTimeMillis();

        BenchMarker benchMarker = new BenchMarker();

        //levelState.setPaused(false);
        while (running){

            loops = 0;

            if (!levelState.isPaused()) {

                currentGameTick = System.currentTimeMillis();

                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    levelState.getPlayer().setRequestedMovementVector(inputManager.getThumbstick().getMovementVector());
                    levelState.getPlayer().setRotationVector(inputManager.getRotationThumbstick().getMovementVector());


                    benchMarker.begin();
                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){

                        //Log.d("hb::AI", "Checking meshploygon for " + aiEntity.getName());
                        for (MeshPolygon meshPolygon : levelState.getRootMeshPolygons().values()){
                            if (meshPolygon.getBoundingBox().intersecting(aiEntity.getBoundingBox())){
                                aiEntity.getContext().addPair(BKeyType.CURRENT_MESH, meshPolygon);
                                //Log.d("hb::AI", "Meshfound for " + aiEntity.getName() + ": " + meshPolygon.getId());
                                break;
                            }
                        }
                    }
                    benchMarker.output("meshCalc");

                    entityController.update(gameTickTimeStepInMillis / 1000f);

                    benchMarker.begin();
                    levelState.getAiManager().update(gameTickTimeStepInMillis / 1000f);
                    benchMarker.output("AI");

                    benchMarker.begin();
                    collisionManager.checkCollisions();
                    benchMarker.output("collisions");

                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    levelState.setReadyToRender(true);

                    //check level end conditions
                    if (levelState.getPlayer().getHealth() < 1){
                        /*levelState.getLevelEnder().setStatus(LevelEndStatus.PLAYER_DIED);
                        levelView.returnToMenu();*/
                        levelState.getLevelEnder().setStatus(LevelEndStatus.PLAYER_DIED);
                        inputManager.setActivePopup(diedPopup);
                    } else if (levelState.getCore().getHealth() < 1) {
                        /*levelState.getLevelEnder().setStatus(LevelEndStatus.CORE_DESTROYED);
                        levelView.returnToMenu();*/
                        levelState.getLevelEnder().setStatus(LevelEndStatus.CORE_DESTROYED);
                        inputManager.setActivePopup(completePopup);
                    }
                }
            } else {
                nextScheduledGameTick = System.currentTimeMillis();
            }

            if (!levelState.isPaused() && levelState.isReadyToRender()) {

                benchMarker.begin();
                float timeSinceLastGameTick = (System.currentTimeMillis() + gameTickTimeStepInMillis - nextScheduledGameTick) / 1000f;
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), timeSinceLastGameTick);
                benchMarker.output("render");

            } else if (levelState.isPaused() && levelState.isReadyToRender()){
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), 0f);
            }
        }
    }

    public void shutDown(){
        levelState.getAiManager().shutDown();
    }

    public void setRunning(boolean isRunning){
        this.running = isRunning;
    }

    public void setInputManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }


}