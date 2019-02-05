package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.map.Room;
import bham.student.txm683.heartbreaker.messaging.MessageBus;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.PhysicsController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;

public class Level implements Runnable {
    private final String TAG = "hb::Level";

    private LevelView levelView;
    private LevelState levelState;
    private InputManager inputManager;
    private PhysicsController physicsController;
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
        physicsController = new PhysicsController(levelState);
        collisionManager = new CollisionManager(levelState);

        nextScheduledGameTick = System.currentTimeMillis();

        levelState.setAiManager(new AIManager(levelState, levelState.getEnemyEntities()));

        //levelState.setPaused(false);
        while (running){

            loops = 0;

            if (!levelState.isPaused()) {

                currentGameTick = System.currentTimeMillis();

                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    levelState.getPlayer().setMovementVector(inputManager.getThumbstick().getMovementVector());

                    for (Room room : levelState.getMap().getRooms().values()){
                        if (room.isEntityInRoom(levelState.getPlayer())) {
                            levelState.getPlayer().setRoomID(room.getId());
                            break;
                        }
                    }

                    for (AIEntity aiEntity : levelState.getEnemyEntities()){
                        for (Room room : levelState.getMap().getRooms().values()){
                            if (room.isEntityInRoom(aiEntity)){
                                aiEntity.setRoomID(room.getId());
                                break;
                            }
                        }
                    }

                    physicsController.update(gameTickTimeStepInMillis / 1000f);

                    levelState.getAiManager().update(gameTickTimeStepInMillis / 1000f);

                    collisionManager.checkCollisions();

                    levelView.setGrid(collisionManager.getBroadPhaseGrid());

                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    levelState.setReadyToRender(true);
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