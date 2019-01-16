package bham.student.txm683.heartbreaker;

import android.util.Log;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.messaging.MessageBus;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.PhysicsController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LevelThread extends Thread {
    private final String TAG = "hb::LevelThread";

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

    public LevelThread(LevelView levelView){
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

        int gameTicksPerSecond = 25;
        int gameTickTimeStepInMillis = 1000 / gameTicksPerSecond;
        int maxSkipTick = 10;

        long currentGameTick;
        long nextScheduledGameTick = System.currentTimeMillis();

        int loops;

        boolean readyToRender = false;

        while (running){

            loops = 0;

            currentGameTick = System.currentTimeMillis();

            //used for interpolation. set to 0 so if the game is paused, the positions will not be changed from in levelstate.
            float timeSinceLastGameTick = 0f;

            if (!isPaused) {
                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    //Log.d(TAG, "GAMETICK");

                    levelState.getPlayer().setMovementVector(inputManager.getThumbstick().getMovementVector());

                    //Log.d(TAG, "movement vector set");

                    physicsController.update(gameTickTimeStepInMillis / 1000f);

                    //Log.d(TAG, "player updated");
                    Log.d(TAG, "checking collisions");
                    collisionManager.checkCollisions();

                    Log.d(TAG, "collisions checked");
                    levelView.setGrid(collisionManager.getBroadPhaseGrid());

                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    readyToRender = true;
                }
            }

            if (!isPaused && readyToRender) {
                timeSinceLastGameTick = (System.currentTimeMillis() + gameTickTimeStepInMillis - nextScheduledGameTick)/1000f;

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

    //TODO implement state save feature
    public String getJSONString(){
        ObjectMapper mapper = new ObjectMapper();

        return "";
    }
}