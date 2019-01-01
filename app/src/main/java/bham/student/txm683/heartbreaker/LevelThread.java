package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.physics.PhysicsController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;
import bham.student.txm683.heartbreaker.utils.InputManager;

public class LevelThread extends Thread {
    private final String TAG = "hb::LevelThread";

    private LevelView levelView;
    private LevelState levelState;
    private InputManager inputManager;
    private PhysicsController physicsController;

    private boolean running;

    private FPSMonitor gameFPSMonitor;
    private FPSMonitor renderFPSMonitor;

    public LevelThread(LevelView levelView){
        super();

        this.levelView = levelView;

        this.levelState = new LevelState();
        this.levelView.setLevelState(levelState);

        this.inputManager = new InputManager();
        this.levelView.setInputManager(inputManager);

        physicsController = new PhysicsController(levelState);

        gameFPSMonitor = new FPSMonitor();
        renderFPSMonitor = new FPSMonitor();
    }

    @Override
    public void run(){

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

                physicsController.update(gameTickTimeStepInMillis/1000f);
                gameFPSMonitor.updateFPS();

                nextScheduledGameTick += gameTickTimeStepInMillis;
                loops++;
            }

            levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay());

        }
    }

    public void setRunning(boolean isRunning){
        this.running = isRunning;
    }
}