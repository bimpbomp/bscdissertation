package bham.student.txm683.heartbreaker.rendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.LevelThread;
import bham.student.txm683.heartbreaker.utils.InputManager;
import bham.student.txm683.heartbreaker.utils.Vector;

public class LevelView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "hb::LevelRenderer";

    private LevelThread levelThread;
    private LevelState levelState;
    private InputManager inputManager;

    private Paint textPaint;

    public LevelView(Context context){
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return inputManager.onTouchEvent(event);
    }

    public void draw(int renderfps, int gametickfps){
        Canvas canvas = getHolder().lockCanvas();

        if (canvas != null){
            super.draw(canvas);

            canvas.drawRGB(255,255,255);
            levelState.getEntity().draw(canvas);

            Vector coordinatesPressed = inputManager.getLastPress();

            if (coordinatesPressed != null){
                canvas.drawCircle(coordinatesPressed.getX(), coordinatesPressed.getY(), 100, textPaint);
            }

            canvas.drawText("RenderFPS: " + renderfps + ". GameTickFPS: " + gametickfps, 50, 50, textPaint);
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    //creates a black paint object for use with any text on screen.
    private void initPaintForText(){
        this.textPaint = new Paint(Color.BLACK);
        this.textPaint.setTextSize(48f);
    }

    public void setLevelState(LevelState levelState){
        this.levelState = levelState;
    }

    public void setInputManager(InputManager inputManager){
        this.inputManager = inputManager;
    }
}