package bham.student.txm683.heartbreaker.utils;

public class GameTickTimer {

    private boolean active;
    private long lastTick;
    private long currentTime;

    private int millisBetweenTicks;

    public GameTickTimer(int millisBetweenTicks){
        this.millisBetweenTicks = millisBetweenTicks;
        this.active = false;
    }

    public void start(){
        lastTick = System.currentTimeMillis();
        active = true;
    }

    public void stop(){
        active = false;
    }

    public boolean hasTicked(){
        currentTime = System.currentTimeMillis();
        if (active && (currentTime - lastTick >= millisBetweenTicks)) {
            lastTick = currentTime;
            return true;
        }
        return false;
    }
}
