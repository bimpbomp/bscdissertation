package bham.student.txm683.heartbreaker;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.messaging.MessageBus;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.EntityController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
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

    public Level(LevelView levelView){
        super();

        this.messageBus = new MessageBus();

        this.levelView = levelView;

        gameFPSMonitor = new FPSMonitor();
        renderFPSMonitor = new FPSMonitor();
    }

    @Override
    public void run(){

        //initialise systems
        entityController = new EntityController(levelState);
        collisionManager = new CollisionManager(levelState);

        nextScheduledGameTick = System.currentTimeMillis();

        levelState.setAiManager(new AIManager(levelState, levelState.getAliveAIEntities()));

        //levelState.setPaused(false);
        while (running){

            loops = 0;

            if (!levelState.isPaused()) {

                currentGameTick = System.currentTimeMillis();

                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    levelState.getPlayer().setRequestedMovementVector(inputManager.getThumbstick().getMovementVector());
                    levelState.getPlayer().setRotationVector(inputManager.getRotationThumbstick().getMovementVector());

                    /*for (Room room : levelState.getMap().getRooms().values()){
                        if (room.isEntityInRoom(levelState.getPlayer())) {
                            levelState.getPlayer().setRoomID(room.getId());
                            break;
                        }
                    }

                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){
                        for (Room room : levelState.getMap().getRooms().values()){
                            if (room.isEntityInRoom(aiEntity)){
                                aiEntity.setRoomID(room.getId());
                                break;
                            }
                        }
                    }*/

                    //TODO tileset now obsolete
                    /*levelState.getTileSet().clearTemporaries();

                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){
                        levelState.getTileSet().addTemporaryToGrid(aiEntity);
                    }

                    levelState.getTileSet().addTemporaryToGrid(levelState.getPlayer());

                    for (Projectile projectile : levelState.getBullets()){
                        levelState.getTileSet().addTemporaryToGrid(projectile);
                    }*/

                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){

                        Log.d("hb::AI", "Checking meshploygon for " + aiEntity.getName());
                        for (MeshPolygon meshPolygon : levelState.getRootMeshPolygons().values()){
                            if (meshPolygon.getBoundingBox().intersecting(aiEntity.getBoundingBox())){
                                aiEntity.getContext().addPair(BKeyType.CURRENT_MESH, meshPolygon);
                                Log.d("hb::AI", "Meshfound for " + aiEntity.getName() + ": " + meshPolygon.getId());
                                break;
                            }
                        }
                    }

                    entityController.update(gameTickTimeStepInMillis / 1000f);

                    levelState.getAiManager().update(gameTickTimeStepInMillis / 1000f);

                    collisionManager.checkCollisions();

                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    levelState.setReadyToRender(true);

                    //check level end conditions
                    if (levelState.getPlayer().getHealth() < 1){
                        levelState.getLevelEnder().setStatus(LevelEndStatus.PLAYER_DIED);
                        levelView.returnToMenu();
                    } else if (levelState.getCore().getHealth() < 1) {
                        levelState.getLevelEnder().setStatus(LevelEndStatus.CORE_DESTROYED);
                        levelView.returnToMenu();
                    }
                }
            } else {
                nextScheduledGameTick = System.currentTimeMillis();
            }

            if (!levelState.isPaused() && levelState.isReadyToRender()) {

                float timeSinceLastGameTick = (System.currentTimeMillis() + gameTickTimeStepInMillis - nextScheduledGameTick) / 1000f;
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), timeSinceLastGameTick);

            } else if (levelState.isPaused() && levelState.isReadyToRender()){
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), 0f);
            }
        }
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