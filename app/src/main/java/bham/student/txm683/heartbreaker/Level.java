package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.input.InputManager;
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
    private boolean isPaused;

    private FPSMonitor gameFPSMonitor;
    private FPSMonitor renderFPSMonitor;

    private int gameTicksPerSecond = 25;
    private int gameTickTimeStepInMillis = 1000 / gameTicksPerSecond;
    private int maxSkipTick = 10;

    private long currentGameTick;
    private long nextScheduledGameTick = System.currentTimeMillis();

    private int loops;

    private boolean readyToRender = false;


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
        while (running){

            loops = 0;

            currentGameTick = System.currentTimeMillis();

            if (!isPaused) {
                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    //Log.d(TAG, "GAMETICK");

                    levelState.getPlayer().setMovementVector(inputManager.getThumbstick().getMovementVector());

                    //Log.d(TAG, "movement vector set");

                    physicsController.update(gameTickTimeStepInMillis / 1000f);

                    //Log.d(TAG, "player updated");
                    collisionManager.checkCollisions();

                    levelView.setGrid(collisionManager.getBroadPhaseGrid());

                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    readyToRender = true;
                }
            }

            if (!isPaused && readyToRender) {
                float timeSinceLastGameTick = (System.currentTimeMillis() + gameTickTimeStepInMillis - nextScheduledGameTick) / 1000f;

                //Log.d(TAG, "LGT: " + currentGameTick + ", timeSinceLGT: " + timeSinceLastGameTick + ", nextGT: " + nextScheduledGameTick);

                //Log.d(TAG, "RENDERTICK");
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), timeSinceLastGameTick);
                //Log.d(TAG, "rendered");
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

    public void setPaused(boolean isPaused){
        this.isPaused = isPaused;
    }
}