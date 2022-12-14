package bham.student.txm683.heartbreaker.rendering;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.framework.input.Button;
import bham.student.txm683.framework.input.Click;
import bham.student.txm683.framework.input.Thumbstick;
import bham.student.txm683.framework.map.MeshPolygon;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.rendering.RenderingTools;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.DebugInfo;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.heartbreaker.Level;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.MenuActivity;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.intentbundleholders.LevelEndStatus;

@SuppressLint("ViewConstructor")
public class LevelView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "hb::LevelView";

    private Thread levelThread;
    private Level level;
    private LevelState levelState;
    private InputManager inputManager;

    private Paint textPaint;
    private Paint tilePaint;

    private int viewWidth;
    private int viewHeight;

    private Point viewWorldOrigin;
    private Point viewWorldMax;

    private BoundingBox visibleBounds;

    private int tileSize;

    private DebugInfo debugInfo;

    private Context context;

    private String mapName;

    /**
     * Creates a new Level view with the given context
     * @param context The context to create the view.
     */
    public LevelView(Context context, String mapName){
        super(context);

        getHolder().addCallback(this);

        this.mapName = mapName;

        this.context = context;

        setFocusable(true);

        textPaint = RenderingTools.initPaintForText(Color.BLACK, 30, Paint.Align.CENTER);

        tilePaint = new Paint();
        tilePaint.setStrokeWidth(8f);
        tilePaint.setColor(Color.MAGENTA);
    }

    public void startLevel(){
        this.level = new Level(this, mapName);

        levelThread = new Thread(level);
        levelThread.start();
    }

    public void loadStage(String stage){
        drawLoading();


        Level oldLevel = level;
        Thread oldLevelThread = levelThread;

        level = new Level(this, mapName);
        levelThread = new Thread(level);
        level.setStage(stage);

        levelThread.start();

        endLevelThread(oldLevel, oldLevelThread);

    }

    public void endLevelThread(Level level, Thread levelThread){
        boolean retry = true;

        while(retry){
            try {
                level.setRunning(false);
                levelThread.join();
            } catch (InterruptedException e){
                Log.d("LOADING", "INTERRUPTED EXCEPTION JOINING LEVELTHREAD");
                e.printStackTrace();
            }
            retry = false;
        }
    }

    public void restartLevel(){
        endLevelThread(level, levelThread);
        startLevel();
    }

    public void shutDown(){
        level.shutDown();
    }

    public void returnToMenu(){

        Bundle bundle = new Bundle();

        if (levelState != null)
            bundle = levelState.getLevelEnder().createBundle();

        Intent intent = new Intent(context, MenuActivity.class);

        intent.putExtra("bundle", bundle);

        Log.d("LOADING", "ACTIVITY CREATED, SHUTTING DOWN LEVEL THREAD");
        shutDown();
        endLevelThread(level, levelThread);

        Log.d("LOADING", "LEVEL THREAD DEAD");
        context.startActivity(intent);

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
    }

    /**
     * Called when the surface is created.
     * Initialises LevelState if it is null, and starts the levelThread
     * @param holder container for the canvas
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        /*levelThread = new Thread(level);
        levelThread.start();*/

        startLevel();
    }

    public void start(){

    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public void setInputManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setDebugInfo(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    /**
     * Creates the in game ui objects. Called on surface creation and when the surface changes.
     * Will dynamically create the positions and sizes based on available space.
     */
    public void initInGameUI(){
        int buttonColor = Color.LTGRAY;

        float thumbstickMaxRadius = 150f;
        this.inputManager.setThumbstick(new Thumbstick(new Point(thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), thumbstickMaxRadius/3f, thumbstickMaxRadius));

        this.inputManager.setRotationThumbstick(new Thumbstick(new Point(viewWidth - thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), thumbstickMaxRadius/3f, thumbstickMaxRadius));

        int pauseButtonRadius = 75;
        Click pauseButtonFunction = () -> levelState.setPaused(!levelState.isPaused());
        this.inputManager.setPauseButton(new Button("PAUSE", new Point(pauseButtonRadius + 20, pauseButtonRadius + 20), pauseButtonRadius, buttonColor, pauseButtonFunction));

        int returnToMenuButtonRadius = 100;


        Click menuButtonFunction = () -> {
            levelState.getLevelEnder().setStatus(LevelEndStatus.USER_QUIT);
            returnToMenu();
        };

        this.inputManager.setReturnToMenuButton(new Button("QUIT", new Point(pauseButtonRadius*2 + 40 + returnToMenuButtonRadius, returnToMenuButtonRadius + 20), returnToMenuButtonRadius, buttonColor, menuButtonFunction));

        textPaint.setTextSize(36f);
        //gives space for 8 debug buttons
        int debugButtonDiameter = viewHeight/6;
        int debugButtonRadius = debugButtonDiameter / 2;
        Button[] debugButtons = {
                new Button("VIS", new Point(viewWidth-debugButtonRadius, debugButtonRadius),
                        debugButtonRadius, buttonColor, () -> debugInfo.invertRenderVisSet()),

                new Button("ENGRID", new Point(viewWidth-debugButtonRadius, debugButtonDiameter +
                        debugButtonRadius), debugButtonRadius, buttonColor, () -> debugInfo.invertRenderMapTileGrid()),

                new Button("NAMES", new Point(viewWidth-debugButtonRadius, 2*debugButtonDiameter
                        + debugButtonRadius), debugButtonRadius, buttonColor, () -> debugInfo.invertRenderEntityNames()),

                new Button("AI", new Point(viewWidth-debugButtonRadius, 3*debugButtonDiameter
                        + debugButtonRadius), debugButtonRadius, buttonColor, () -> debugInfo.invertActivateAI())
        };

        textPaint.setTextSize(48f);
        inputManager.setDebugButtons(debugButtons);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        this.viewWidth = width;
        this.viewHeight = height;

        Log.d(TAG, "surfaceChanged");
    }

    /**
     * Called when the surface is destroyed, joins with the levelThread to end game ticks
     * @param holder container for canvas
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        endLevelThread(level, levelThread);
        Log.d(TAG, "surfaceDestroyed");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (inputManager != null)
            return inputManager.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    public void onBackPressed(){
        inputManager.showPauseScreen();
    }

    public void drawLoading(){
        Canvas canvas = getHolder().lockCanvas();

        if (canvas != null){
            Log.d("LOADING", "canvas not null");

            canvas.drawColor(Color.GRAY);
            RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, "Loading...", new Point(viewWidth/2f, viewHeight/2f), Color.RED, 10);

        } else {
            Log.d("LOADING", "canvas is null");
        }

        try {
            getHolder().unlockCanvasAndPost(canvas);
        } catch (IllegalArgumentException e){
            //do nothing
        }
    }

    /**
     * Draws the visible entities, and in game ui to the canvas.
     * @param renderFPS Number of render ticks in the last second
     * @param gameTickFPS Number of game ticks in the last second
     * @param secondsSinceLastGameTick Used for interpolation purposes
     */
    public void draw(int renderFPS, int gameTickFPS, float secondsSinceLastGameTick){

        //get visible boundaries relative to world coordinates
        viewWorldOrigin = levelState.getPlayer().getCenter().add(new Point(-1*viewWidth/2f, -1*viewHeight/2f));
        viewWorldMax = viewWorldOrigin.add(new Point(viewWidth, viewHeight));

        visibleBounds = new BoundingBox(viewWorldOrigin, viewWorldMax);

        Canvas canvas = getHolder().lockCanvas();

        int numOnScreen = 0;

        if (canvas != null){
            super.draw(canvas);

            Point renderOffset = viewWorldOrigin.sMult(-1f);

            //draw background
            canvas.drawColor(Color.GREEN);

            //draw meshGrid
            for (MeshPolygon meshPolygon : levelState.getRootMeshPolygons().values()){
                if (true) {
                    numOnScreen++;
                    meshPolygon.draw(canvas, renderOffset);
                }
            }

            //draw doors
            for (Renderable door : levelState.getMap().getDoors().values()){
                if (true) {
                    numOnScreen++;
                    door.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }

            //draw dead ai
            for (AIEntity deadAI : levelState.getDeadAI()){
                if (true){
                    numOnScreen++;
                    deadAI.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }

            //draw pickups
            for (Renderable pickup : levelState.getPickups()){
                if (true) {
                    numOnScreen++;
                    pickup.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }

            //draw bullets
            for (Renderable bullet : levelState.getBullets()){
                if (true) {
                    numOnScreen++;
                    bullet.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }

            //draw player
            levelState.getPlayer().draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());

            //draw alive ai
            for (AIEntity entity : levelState.getAliveAIEntities()){
                if (true) {
                    numOnScreen++;
                    entity.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                    entity.setOnScreen(true);
                } else
                    entity.setOnScreen(false);
            }

            //draw explosions
            for (Renderable explosion : levelState.getLingeringExplosions()){
                if (true) {
                    numOnScreen++;
                    explosion.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }
            levelState.removeLingeringExplosions();


            //draw walls
            for (Renderable wall : levelState.getMap().getWalls()){
                if (true) {
                    numOnScreen++;
                    wall.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }

            //draw grid (if turned on)
            if (debugInfo.renderMapTileGrid())
                drawGrid(canvas, new Point(), new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight()), tileSize, renderOffset, tilePaint);

            //draw ui
            if (!levelState.isPaused()) {
                /*RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, "RenderFPS: " + renderFPS
                        + " GameTickFPS: " + gameTickFPS,
                        new Point(viewWidth/2f, 50), Color.WHITE, 10);*/
            }
            if (levelState.isPaused()) {
                canvas.drawARGB(200, 0,0,0);
            }

            //draw in game ui
            inputManager.draw(canvas, textPaint);
        }
        try {
            getHolder().unlockCanvasAndPost(canvas);
        } catch (IllegalArgumentException e){
            //canvas is destroyed already
        }

        levelState.getBenchLog().addItemsOnScreenCount(numOnScreen);
    }

    private void drawGrid(Canvas canvas, Point minimum, Point maximum, int cellSize, Point renderOffset, Paint gridPaint){
        maximum = maximum.add(minimum.sMult(-1f));

        for (float i = minimum.getX(); i <= maximum.getX(); i += cellSize){
            canvas.drawLine(i+renderOffset.getX(),minimum.getY()+renderOffset.getY(), i+renderOffset.getX(), maximum.getY() + renderOffset.getY(), gridPaint);
        }

        for (float i = minimum.getY(); i <= maximum.getY(); i += cellSize){
            canvas.drawLine(minimum.getX()+renderOffset.getX(),i+renderOffset.getY(), maximum.getX() + renderOffset.getX(), i+renderOffset.getY(), gridPaint);
        }
    }

    /**
     * Checks if any of the given vertices lie in the region given by the screen boundaries.
     * @param entity Entity to check if visible
     * @return True if one or more vertices exist in the screen boundaries, false if none are.
     */
    private boolean isOnScreen(Renderable entity){
        return visibleBounds.intersecting(entity.getBoundingBox());
    }

    private boolean isOnScreen(BoundingBox b){
        return visibleBounds.intersecting(b);
    }

    public BoundingBox getVisibleBounds(){
        return visibleBounds;
    }

    public LevelState getLevelState() {
        return levelState;
    }
}