package bham.student.txm683.heartbreaker;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import bham.student.txm683.heartbreaker.rendering.LevelView;

public class Level extends Thread {
    private final String TAG = "hb::Level";

    private SurfaceHolder surfaceHolder;
    private LevelView levelView;
    private boolean running;
    public static Canvas canvas;

    public Level(SurfaceHolder surfaceHolder, LevelView levelView){
        super();
        this.surfaceHolder = surfaceHolder;
        this.levelView = levelView;
    }

    public void setRunning(boolean isRunning){
        this.running = isRunning;
    }

    @Override
    public void run(){
        while(running){
            canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();

                synchronized (surfaceHolder){
                    this.levelView.update();
                    this.levelView.draw(canvas);
                }
            } catch (Exception e){
                //do nothing
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}