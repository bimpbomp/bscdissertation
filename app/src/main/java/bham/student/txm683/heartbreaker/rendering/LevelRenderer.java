package bham.student.txm683.heartbreaker.rendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.LevelThread;

public class LevelRenderer extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "hb::LevelRenderer";

    private LevelThread levelThread;
    private LevelState levelState;

    private Paint textPaint;

    public LevelRenderer(Context context){
        super(context);

        getHolder().addCallback(this);

        this.levelThread = new LevelThread(this);
        setFocusable(true);

        initPaintForText();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        levelThread.setRunning(true);
        levelThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        while(retry){
            try {
                levelThread.setRunning(false);
                levelThread.join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            retry = false;
        }
        Log.d(TAG, "surfaceDestroyed");
    }

    public void draw(int renderfps, int gametickfps){
        Canvas canvas = getHolder().lockCanvas();

        if (canvas != null){
            super.draw(canvas);

            canvas.drawRGB(255,255,255);
            levelState.getEntity().draw(canvas);
            canvas.drawText("RenderFPS: " + renderfps + ". GameTickFPS: " + gametickfps, 50, 50, textPaint);
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    private void initPaintForText(){
        this.textPaint = new Paint(Color.BLACK);
        this.textPaint.setTextSize(48f);
    }

    public void setLevelState(LevelState levelState){
        this.levelState = levelState;
    }
}