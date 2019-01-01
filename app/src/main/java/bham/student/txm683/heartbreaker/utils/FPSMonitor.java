package bham.student.txm683.heartbreaker.utils;

public class FPSMonitor {

    //counter for number of render updates this second
    private int fps;
    //time in millis of last updateFPS() call for render tick
    private long lastFPSUpdate;
    //given to renderer to display renderFPS count
    private int fpsToDisplay;

    public FPSMonitor(){
        fps = 0;
        lastFPSUpdate = System.currentTimeMillis();
        fpsToDisplay = 0;
    }

    public void updateFPS() {
        if (System.currentTimeMillis() - lastFPSUpdate > 1000) {
            fpsToDisplay = fps;
            fps = 0;
            lastFPSUpdate += 1000;
        }
        fps++;
    }

    public int getFpsToDisplay(){
        return fpsToDisplay;
    }

    public int getFPSToDisplayAndUpdate(){
        updateFPS();
        return fpsToDisplay;
    }
}
