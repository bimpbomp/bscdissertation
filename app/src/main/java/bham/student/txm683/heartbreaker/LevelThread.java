package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.messaging.MessageBus;
import bham.student.txm683.heartbreaker.physics.PhysicsController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;

public class LevelThread extends Thread {
    private final String TAG = "hb::LevelThread";

    private LevelView levelView;
    private LevelState levelState;
    private InputManager inputManager;
    private PhysicsController physicsController;

    private MessageBus messageBus;

    private boolean running;

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

        int gameTicksPerSecond = 25;
        int gameTickTimeStepInMillis = 1000 / gameTicksPerSecond;
        int maxSkipTick = 10;

        long currentGameTick;
        long nextScheduledGameTick = System.currentTimeMillis();

        int loops;

        while (running){

            loops = 0;

            currentGameTick = System.currentTimeMillis();
            while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick){

                levelState.getPlayer().setMovementUnitVector(inputManager.getThumbstick().getMovementVector().getUnitVector());

                physicsController.update(gameTickTimeStepInMillis/1000f);

                gameFPSMonitor.updateFPS();

                nextScheduledGameTick += gameTickTimeStepInMillis;
                loops++;
            }

            float timeSinceLastGameTick = (System.currentTimeMillis() + gameTickTimeStepInMillis - nextScheduledGameTick)/1000f;

            //Log.d(TAG, "LGT: " + currentGameTick + ", timeSinceLGT: " + timeSinceLastGameTick + ", nextGT: " + nextScheduledGameTick);

            levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), timeSinceLastGameTick);

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
}