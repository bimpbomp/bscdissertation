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
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.Thumbstick;
import bham.student.txm683.heartbreaker.physics.Grid;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

public class LevelView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "hb::LevelView";

    private LevelThread levelThread;
    private LevelState levelState;
    private InputManager inputManager;

    private Paint textPaint;

    private int viewWidth;
    private int viewHeight;

    private Grid grid;

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

        UniqueID uniqueID = new UniqueID();

        this.levelState = new LevelState();
        this.levelState.setScreenDimensions(viewWidth, viewHeight);
        this.levelState.getMap().loadMap(viewWidth, viewHeight);

        this.levelState.setPlayer(new Player("player", new Vector(100,100)));
        this.levelState.getPlayer().setID(uniqueID.id());

        for (int i = 0; i < 5; i++){
            for (int j = 0; j < 5; j++) {
                Entity entity = new Entity("NPE-" + (i+1) + (j+1), new Vector(300+200*i,300+200*j), Color.BLUE);
                entity.setColor(Color.BLUE);
                entity.setID(uniqueID.id());
                this.levelState.addNonPlayerEntity(entity);
            }
        }

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

            if (grid != null) {

                for (int i = 0; i < viewWidth; i += grid.getCellSize()) {
                    //Log.d(TAG, i + ", " + 0 + ", " + i + ", " + viewHeight);
                    canvas.drawLine(i, 0, i, viewHeight, textPaint);
                }

                for (int i = grid.getCellSize(); i < viewHeight; i += grid.getCellSize()) {
                    canvas.drawLine(0, i, viewWidth, i, textPaint);
                }
            }

            levelState.getPlayer().draw(canvas, timeSinceLastGameTick);

            for (Entity entity : levelState.getNonPlayerEntities()){
                entity.draw(canvas, 0);
            }

            inputManager.getThumbstick().draw(canvas);

            canvas.drawText("RenderFPS: " + renderfps + ". GameTickFPS: " + gametickfps, 50, 50, textPaint);
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    //creates a black paint object for use with any text on screen.
    private void initPaintForText(){
        this.textPaint = new Paint(Color.BLACK);
        this.textPaint.setStrokeWidth(2f);
        this.textPaint.setTextSize(48f);
    }

    public void setPaused(boolean isPaused){
        this.levelThread.setPaused(isPaused);
    }

    public void setRunning(boolean running){
        this.levelThread.setRunning(running);
    }

    public void setGrid(Grid grid){
        this.grid = grid;
    }
}