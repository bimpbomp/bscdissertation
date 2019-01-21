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
import bham.student.txm683.heartbreaker.input.Button;
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

    private Point viewWorldOrigin;
    private Point viewWorldMax;

    private Grid grid;

    public LevelView(Context context){
        super(context);

        getHolder().addCallback(this);

        this.level = new Level(this);
        setFocusable(true);

        initPaintForText();

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

    public void setLevelState(LevelState levelState){
        this.levelState = levelState;
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

        if (this.levelState == null) {
            Map map = new Map();
            map.loadMap("TestMap", 300);
            this.levelState = new LevelState(map);

            this.levelState.setScreenDimensions(viewWidth, viewHeight);

            this.inputManager = new InputManager(levelState);

            this.level.setInputManager(inputManager);
            this.level.setLevelState(levelState);
        }

        this.levelThread = new Thread(this.level);
        this.level.setRunning(true);
        this.levelThread.start();
    }

    private void initInGameUI(){
        float thumbstickMaxRadius = 150f;
        this.inputManager.setThumbstick(new Thumbstick(new Point(thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), thumbstickMaxRadius/3f, thumbstickMaxRadius));

        int pauseButtonRadius = 50;
        this.inputManager.setPauseButton(new Button(new Point(pauseButtonRadius+20, pauseButtonRadius+20), pauseButtonRadius, Color.LTGRAY));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        this.viewWidth = width;
        this.viewHeight = height;

        this.levelState.setScreenDimensions(viewWidth, viewHeight);

        initInGameUI();

        Log.d(TAG, "surfaceChanged");
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

    public void draw(int renderFPS, int gameTickFPS, float timeSinceLastGameTick){

        //get visible boundaries relative to world coordinates
        viewWorldOrigin = levelState.getPlayer().getShape().getCenter().add(new Point(-1*viewWidth/2f, -1*viewHeight/2f));
        viewWorldMax = viewWorldOrigin.add(new Point(viewWidth, viewHeight));

        //Calculate what entities are in view of the player on screen
        ArrayList<Entity> staticEntitiesToRender = new ArrayList<>();

        for (Entity staticEntity: levelState.getStaticEntities()){
            if (isOnScreen(staticEntity.getShape().getVertices())){
                staticEntitiesToRender.add(staticEntity);
            }
        }

        ArrayList<Entity> enemiesToRender = new ArrayList<>();

        for (Entity enemy: levelState.getEnemyEntities()){
            if (isOnScreen(enemy.getShape().getVertices())){
                enemiesToRender.add(enemy);
            }
        }


        Canvas canvas = getHolder().lockCanvas();

        //TODO: Is currently set to zero to disable interpolation. Remove line to re-enable
        timeSinceLastGameTick = 0f;

        if (canvas != null){
            super.draw(canvas);

            Point renderOffset = viewWorldOrigin.smult(-1f);

            //draw background
            canvas.drawRGB(255,255,255);

            levelState.getPlayer().draw(canvas, renderOffset, timeSinceLastGameTick);

            for (Entity entity : enemiesToRender){
                entity.draw(canvas, renderOffset);
            }

            for (Entity entity : staticEntitiesToRender){
                entity.draw(canvas, renderOffset);
            }

            //draw in game ui
            inputManager.draw(canvas);
            //if paused, draw the pause menu
            if (!levelState.isPaused()) {
                canvas.drawText("RenderFPS: " + renderFPS + ". GameTickFPS: " + gameTickFPS, 150, 50, textPaint);
            } else {
                //draw pause menu
                canvas.drawText("Game is paused", viewWidth/2f, viewHeight/3f, textPaint);
            }
        }
        try {
            getHolder().unlockCanvasAndPost(canvas);
        } catch (IllegalArgumentException e){
            //canvas is destroyed already
        }
    }

    private boolean isOnScreen(Point[] vertices){
        for (Point point : vertices) {
            if ((point.getX() >= viewWorldOrigin.getX() && point.getX() <= viewWorldMax.getX()) && (point.getY() >= viewWorldOrigin.getY() && point.getY() <= viewWorldMax.getY())){
                return true;
            }
        }
        return false;
    }

    private void renderInGameUI(Canvas canvas){
        inputManager.getThumbstick().draw(canvas);
    }

    //creates a black paint object for use with any text on screen.
    private void initPaintForText(){
        this.textPaint = new Paint();
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setStrokeWidth(2f);
        this.textPaint.setTextSize(48f);
    }

    public void setGrid(Grid grid){
        this.grid = grid;
    }

    public LevelState getLevelState() {
        return levelState;
    }
}