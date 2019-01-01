package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.physics.PhysicsController;
import bham.student.txm683.heartbreaker.rendering.LevelRenderer;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;

public class LevelThread extends Thread {
    private final String TAG = "hb::LevelThread";

    private LevelRenderer levelRenderer;
    private LevelState levelState;
    private PhysicsController physicsController;

    private boolean running;

    private FPSMonitor gameFPSMonitor;
    private FPSMonitor renderFPSMonitor;

    public LevelThread(LevelRenderer levelRenderer){
        super();

        this.levelRenderer = levelRenderer;

        this.levelState = new LevelState();
        this.levelRenderer.setLevelState(levelState);

        physicsController = new PhysicsController(levelState);

        gameFPSMonitor = new FPSMonitor();
        renderFPSMonitor = new FPSMonitor();
    }

    @Override
    public void run(){

        int ticksPerSecond = 50;
        int skipTicks = 1000 / ticksPerSecond;
        int maxSkipTick = 10;

        long lastGameTick = -1;
        long currentGameTick;
        long nextGameTick = System.currentTimeMillis();
        float delta = 0;

        int loops;

        while (running){

            loops = 0;

            currentGameTick = System.currentTimeMillis();
            while (currentGameTick > nextGameTick && loops < maxSkipTick){

                if (lastGameTick < 0){
                    delta = 0f;
                } else {
                    delta = (currentGameTick-lastGameTick)/1000f;
                }

                physicsController.update(delta);
                gameFPSMonitor.updateFPS();

                lastGameTick = currentGameTick;

                nextGameTick += skipTicks;
                loops++;
            }

            levelRenderer.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay());

        }
    }

    public void setRunning(boolean isRunning){
        this.running = isRunning;
    }
}