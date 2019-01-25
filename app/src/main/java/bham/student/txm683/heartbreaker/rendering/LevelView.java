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
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
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
    private int tileSize;

    /**
     * Creates a new Level view with the given context
     * @param context The context to create the view.
     */
    public LevelView(Context context){
        super(context);

        getHolder().addCallback(this);

        this.level = new Level(this);
        setFocusable(true);

        textPaint = RenderingTools.initPaintForText(Color.BLACK, 48f, Paint.Align.CENTER);

    }

    /**
     * Creates the LevelState object based off of the given JSON formatted string
     * @param stateString String containing state LevelState in JSON format
     */
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

    /**
     * Called when the dimensions of the surface change
     * @param w new width
     * @param h new height
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged");
        this.viewWidth = w;
        this.viewHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);

        if (this.levelState != null)
            this.levelState.setScreenDimensions(viewWidth, viewHeight);
    }

    /**
     * Called when the surface is created.
     * Initialises LevelState if it is null, and starts the levelThread
     * @param holder container for the canvas
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        if (this.levelState == null) {
            Map map = new Map();
            tileSize = 300;
            map.loadMap("TestMap", tileSize);
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

    /**
     * Creates the in game ui objects. Called on surface creation and when the surface changes.
     * Will dynamically create the positions and sizes based on available space.
     */
    private void initInGameUI(){
        float thumbstickMaxRadius = 150f;
        this.inputManager.setThumbstick(new Thumbstick(new Point(thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), thumbstickMaxRadius/3f, thumbstickMaxRadius));

        int pauseButtonRadius = 50;
        this.inputManager.setPauseButton(new Button(new Point(pauseButtonRadius+20, pauseButtonRadius+20), pauseButtonRadius, Color.LTGRAY));
    }

    /**
     * Called when the surface changes (and after surfaceCreated) to update the dimensions.
     * @param holder Container for canvas
     * @param format
     * @param width width of screen
     * @param height height of screen
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        this.viewWidth = width;
        this.viewHeight = height;

        this.levelState.setScreenDimensions(viewWidth, viewHeight);

        initInGameUI();

        Log.d(TAG, "surfaceChanged");
    }

    /**
     * Called when the surface is destroyed, joins with the levelThread to end game ticks
     * @param holder container for canvas
     */
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

    /**
     * Draws the visible entities, and in game ui to the canvas.
     * @param renderFPS Number of render ticks in the last second
     * @param gameTickFPS Number of game ticks in the last second
     * @param secondsSinceLastGameTick Used for interpolation purposes
     */
    public void draw(int renderFPS, int gameTickFPS, float secondsSinceLastGameTick){

        //get visible boundaries relative to world coordinates
        viewWorldOrigin = levelState.getPlayer().getShape().getCenter().add(new Point(-1*viewWidth/2f, -1*viewHeight/2f));
        viewWorldMax = viewWorldOrigin.add(new Point(viewWidth, viewHeight));

        //Calculate what entities are in view of the player on screen
        ArrayList<Entity> staticEntitiesToRender = new ArrayList<>();

        for (Entity staticEntity: levelState.getStaticEntities()){
            if (isOnScreen(staticEntity.getShape().getCollisionVertices())){
                staticEntitiesToRender.add(staticEntity);
            }
        }

        ArrayList<Entity> enemiesToRender = new ArrayList<>();

        for (Entity enemy: levelState.getEnemyEntities()){
            if (isOnScreen(enemy.getShape().getCollisionVertices())){
                enemiesToRender.add(enemy);
            }
        }


        Canvas canvas = getHolder().lockCanvas();

        //TODO: Is currently set to zero to disable interpolation. Remove line to re-enable
        secondsSinceLastGameTick = 0f;

        if (canvas != null){
            super.draw(canvas);

            Point renderOffset = viewWorldOrigin.smult(-1f);

            //draw background
            canvas.drawRGB(255,255,255);

            //drawGrid(canvas, grid.getGridMinimum(), grid.getGridMaximum(), grid.getCellSize(), renderOffset);

            drawGrid(canvas, new Point(), new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight()), tileSize, renderOffset);

            levelState.getPlayer().draw(canvas, renderOffset, secondsSinceLastGameTick);

            for (Entity entity : enemiesToRender){
                ((MoveableEntity)entity).draw(canvas, renderOffset, secondsSinceLastGameTick);
            }

            for (Entity entity : staticEntitiesToRender){
                entity.draw(canvas, renderOffset);
            }

            //draw in game ui
            inputManager.draw(canvas);
            //if paused, draw the pause menu
            if (!levelState.isPaused()) {
                canvas.drawText("RenderFPS: " + renderFPS + ". GameTickFPS: " + gameTickFPS + ". Collisions: " + level.getCollisionManager().collisionCount, viewWidth/2f, 50, textPaint);
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

    private void drawGrid(Canvas canvas, Point minimum, Point maximum, int cellSize, Point renderOffset){
        maximum = maximum.add(minimum.smult(-1f));

        for (float i = minimum.getX(); i <= maximum.getX(); i += cellSize){
            canvas.drawLine(i+renderOffset.getX(),minimum.getY()+renderOffset.getY(), i+renderOffset.getX(), maximum.getY() + renderOffset.getY(), textPaint);
        }

        for (float i = minimum.getY(); i <= maximum.getY(); i += cellSize){
            canvas.drawLine(minimum.getX()+renderOffset.getX(),i+renderOffset.getY(), maximum.getX() + renderOffset.getX(), i+renderOffset.getY(), textPaint);
        }
    }

    /**
     * Checks if any of the given vertices lie in the region given by the screen boundaries.
     * @param vertices Point array of vertices to check
     * @return True if one or more vertices exist in the screen boundaries, false if none are.
     */
    private boolean isOnScreen(Point[] vertices){
        for (Point point : vertices) {
            if ((point.getX() >= viewWorldOrigin.getX() && point.getX() <= viewWorldMax.getX()) && (point.getY() >= viewWorldOrigin.getY() && point.getY() <= viewWorldMax.getY())){
                return true;
            }
        }
        return false;
    }

    public void setGrid(Grid grid){
        this.grid = grid;
    }

    public LevelState getLevelState() {
        return levelState;
    }
}