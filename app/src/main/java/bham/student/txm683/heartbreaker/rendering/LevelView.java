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
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.Thumbstick;
import bham.student.txm683.heartbreaker.utils.Vector;

public class LevelView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "hb::LevelView";

    private LevelThread levelThread;
    private LevelState levelState;
    private InputManager inputManager;

    private Paint textPaint;

    private int viewWidth;
    private int viewHeight;

    public LevelView(Context context){
        super(context);

        getHolder().addCallback(this);

        this.levelThread = new LevelThread(this);
        setFocusable(true);

        initPaintForText();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged");
        this.viewWidth = w;
        this.viewHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        float thumbstickMaxRadius = 150f;

        this.inputManager = new InputManager(new Thumbstick(new Vector(thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), 50, thumbstickMaxRadius));
        this.levelThread.setInputManager(inputManager);

        this.levelState = new LevelState();
        this.levelThread.setLevelState(levelState);

        levelThread.setRunning(true);
        levelThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
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

    public void draw(int renderfps, int gametickfps, float timeSinceLastGameTick){
        Canvas canvas = getHolder().lockCanvas();

        if (canvas != null){
            super.draw(canvas);

            canvas.drawRGB(255,255,255);
            levelState.getPlayer().draw(canvas, timeSinceLastGameTick);

            inputManager.getThumbstick().draw(canvas);

            canvas.drawText("RenderFPS: " + renderfps + ". GameTickFPS: " + gametickfps, 50, 50, textPaint);
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    //creates a black paint object for use with any text on screen.
    private void initPaintForText(){
        this.textPaint = new Paint(Color.BLACK);
        this.textPaint.setTextSize(48f);
    }
}