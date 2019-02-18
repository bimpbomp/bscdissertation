package bham.student.txm683.heartbreaker.utils;

public class GameTickTimer {

    private boolean active;
    private long lastTick;
    private long currentTime;
    private long diff;

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

    /**
     * Returns the number of ticks that have passed or 0 if no ticks/not active
     * @return ticks that have passed
     */
    public int tick(){
        currentTime = System.currentTimeMillis();
        diff = currentTime - lastTick;
        if (active && (diff >= millisBetweenTicks)) {
            lastTick = currentTime;
            return (int) diff / millisBetweenTicks;
        }
        return 0;
    }
}
