package bham.student.txm683.heartbreaker.rendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.heartbreaker.Level;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.Thumbstick;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.physics.Grid;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.ArrayList;


public class LevelView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "hb::LevelView";

    private Thread levelThread;
    private Level level;
    private LevelState levelState;
    private InputManager inputManager;

    private Paint textPaint;

    private int viewWidth;
    private int viewHeight;

    private Grid grid;

    private boolean resumingFromSaveFile;

    //private IsoscelesTriangle triangle;

    public LevelView(Context context){
        super(context);

        getHolder().addCallback(this);

        this.level = new Level(this);
        setFocusable(true);

        initPaintForText();

        resumingFromSaveFile = false;
    }

    public void loadSaveFromStateString(String stateString){
        Log.d(TAG, "stateStringLengthInBytes: " + stateString.getBytes().length + ", stateString: " + stateString);
        try {
            this.levelState = new LevelState(stateString);
        } catch (Exception e){
            Log.d(TAG,"Parsing error: " + e.getMessage());
            this.levelState = null;
        }
    }

    public LevelState onPause(){
        this.setPaused(true);
        return levelState;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged");
        this.viewWidth = w;
        this.viewHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);

        if (this.levelState != null)
            this.levelState.setScreenDimensions(viewWidth, viewHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        float thumbstickMaxRadius = 150f;

        this.inputManager = new InputManager(new Thumbstick(new Point(thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), 50, thumbstickMaxRadius));
        this.level.setInputManager(inputManager);

        if (this.levelState == null) {
            Map map = new Map();
            map.loadMap("TestMap", 300);
            this.levelState = new LevelState(map);

            this.levelState.setScreenDimensions(viewWidth, viewHeight);
        }
        this.level.setLevelState(levelState);

        this.levelThread = new Thread(this.level);
        this.level.setRunning(true);
        this.levelThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");

        this.viewWidth = width;
        this.viewHeight = height;

        this.levelState.setScreenDimensions(viewWidth, viewHeight);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        while(retry){
            try {
                level.setRunning(false);
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

        Point viewWorldOrigin = levelState.getPlayer().getShape().getCenter().add(new Point(-1*viewWidth/2f, -1*viewHeight/2f));
        Point viewWorldMax = viewWorldOrigin.add(new Point(viewWidth, viewHeight));

        ArrayList<Entity> staticEntitiesToRender = new ArrayList<>();

        for (Entity staticEntity: levelState.getStaticEntities()){

            for (Point point : staticEntity.getShape().getVertices()) {

                if ((point.getX() >= viewWorldOrigin.getX() && point.getX() <= viewWorldMax.getX()) && (point.getY() >= viewWorldOrigin.getY() && point.getY() <= viewWorldMax.getY())) {
                    staticEntitiesToRender.add(staticEntity);
                    break;
                }
            }
        }

        ArrayList<Entity> enemiesToRender = new ArrayList<>();

        for (Entity enemy: levelState.getEnemyEntities()){

            for (Point point : enemy.getShape().getVertices()) {

                if ((point.getX() >= viewWorldOrigin.getX() && point.getX() <= viewWorldMax.getX()) && (point.getY() >= viewWorldOrigin.getY() && point.getY() <= viewWorldMax.getY())) {
                    enemiesToRender.add(enemy);
                    break;
                }
            }
        }


        Canvas canvas = getHolder().lockCanvas();

        //TODO: This is to disable interpolation
        timeSinceLastGameTick = 0f;

        if (canvas != null){
            super.draw(canvas);

            Point renderOffset = viewWorldOrigin.smult(-1f);

            canvas.drawRGB(255,255,255);

            /*//draw physics broad phase grid (if set)
            if (grid != null) {

                for (int i = 0; i < viewWidth; i += grid.getCellSize()) {
                    //Log.d(TAG, i + ", " + 0 + ", " + i + ", " + viewHeight);
                    canvas.drawLine(i, 0, i, viewHeight, textPaint);
                }

                for (int i = grid.getCellSize(); i < viewHeight; i += grid.getCellSize()) {
                    canvas.drawLine(0, i, viewWidth, i, textPaint);
                }
            }*/

            levelState.getPlayer().draw(canvas, renderOffset, timeSinceLastGameTick);

            for (Entity entity : enemiesToRender){
                entity.draw(canvas, renderOffset);
            }

            for (Entity entity : staticEntitiesToRender){
                entity.draw(canvas, renderOffset);
            }

            inputManager.getThumbstick().draw(canvas);

            canvas.drawText("RenderFPS: " + renderfps + ". GameTickFPS: " + gametickfps, 50, 50, textPaint);
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    //creates a black paint object for use with any text on screen.
    private void initPaintForText(){
        this.textPaint = new Paint();
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setStrokeWidth(2f);
        this.textPaint.setTextSize(48f);
    }

    public void setPaused(boolean isPaused){
        this.level.setPaused(isPaused);
    }

    public void setRunning(boolean running){
        this.level.setRunning(running);
    }

    public void setGrid(Grid grid){
        this.grid = grid;
    }
}