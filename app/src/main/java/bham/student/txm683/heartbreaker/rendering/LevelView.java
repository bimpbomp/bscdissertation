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
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.input.Button;
import bham.student.txm683.heartbreaker.input.Click;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.Thumbstick;
import bham.student.txm683.heartbreaker.map.MapConstructor;
import bham.student.txm683.heartbreaker.map.Room;
import bham.student.txm683.heartbreaker.physics.Grid;
import bham.student.txm683.heartbreaker.utils.DebugInfo;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;


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

    private Grid grid;
    private int tileSize;

    private DebugInfo debugInfo;

    /**
     * Creates a new Level view with the given context
     * @param context The context to create the view.
     */
    public LevelView(Context context){
        super(context);

        getHolder().addCallback(this);

        this.level = new Level(this);
        setFocusable(true);

        textPaint = RenderingTools.initPaintForText(Color.BLACK, 30, Paint.Align.CENTER);

        tilePaint = new Paint();
        tilePaint.setStrokeWidth(8f);
        tilePaint.setColor(Color.MAGENTA);
    }

    /**
     * Creates the LevelState object based off of the given JSON formatted string
     * @param stateString String containing state LevelState in JSON format
     */
    public void loadSaveFromStateString(String stateString){
        /*Log.d(TAG, "stateStringLengthInBytes: " + stateString.getBytes().length + ", stateString: " + stateString);
        try {
            this.levelState = new LevelState(stateString);
        } catch (Exception e){
            Log.d(TAG,"Parsing error: " + e.getMessage());
            this.levelState = null;
        }*/
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

            tileSize = 200;
            MapConstructor mapConstructor = new MapConstructor();

            this.levelState = new LevelState(mapConstructor.loadMap("Map2", tileSize));

            this.levelState.setScreenDimensions(viewWidth, viewHeight);

            this.inputManager = new InputManager(levelState);

            this.level.setInputManager(inputManager);
            this.level.setLevelState(levelState);
        }

        this.debugInfo = levelState.getDebugInfo();

        this.levelThread = new Thread(this.level);
        this.level.setRunning(true);
        this.levelThread.start();
    }

    /**
     * Creates the in game ui objects. Called on surface creation and when the surface changes.
     * Will dynamically create the positions and sizes based on available space.
     */
    private void initInGameUI(){
        int buttonColor = Color.LTGRAY;

        float thumbstickMaxRadius = 150f;
        this.inputManager.setThumbstick(new Thumbstick(new Point(thumbstickMaxRadius, viewHeight-thumbstickMaxRadius), thumbstickMaxRadius/3f, thumbstickMaxRadius));

        int pauseButtonRadius = 75;
        Click pauseButtonFunction = () -> levelState.setPaused(!levelState.isPaused());
        this.inputManager.setPauseButton(new Button("PAUSE", new Point(pauseButtonRadius + 20, pauseButtonRadius + 20), pauseButtonRadius, buttonColor, pauseButtonFunction));

        int attackButtonRadius = 100;
        this.inputManager.setMeleeButton(new Button("THUMP", new Point(viewWidth-(attackButtonRadius*3) - 20, viewHeight-attackButtonRadius -10), attackButtonRadius, buttonColor, null));

        this.inputManager.setRangedButton(new Button("PEW", new Point(viewWidth-attackButtonRadius -10, viewHeight-attackButtonRadius-10), attackButtonRadius, buttonColor, null));

        textPaint.setTextSize(36f);
        //gives space for 8 debug buttons
        int debugButtonDiameter = viewHeight/6;
        int debugButtonRadius = debugButtonDiameter / 2;
        Button[] debugButtons = {
                new Button("PHYS", new Point(viewWidth-debugButtonRadius, debugButtonRadius),
                        debugButtonRadius, buttonColor, () -> debugInfo.invertRenderPhysicsGrid()),

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
        viewWorldOrigin = levelState.getPlayer().getCenter().add(new Point(-1*viewWidth/2f, -1*viewHeight/2f));
        viewWorldMax = viewWorldOrigin.add(new Point(viewWidth, viewHeight));

        //Calculate what entities are in view of the player on screen

        ArrayList<Wall> wallsToRender = new ArrayList<>();
        for (Wall wall: levelState.getMap().getWalls()){
            if (isOnScreen(wall.getCollisionVertices())){
                wallsToRender.add(wall);
            }
        }

        ArrayList<Renderable> enemiesToRender = new ArrayList<>();

        for (AIEntity enemy: levelState.getEnemyEntities()){
            if (isOnScreen(enemy.getCollisionVertices())){
                enemiesToRender.add(enemy);
            }
        }

        Canvas canvas = getHolder().lockCanvas();

        Vector interpolationVector = new Vector();

        if (canvas != null){
            super.draw(canvas);

            Point renderOffset = viewWorldOrigin.smult(-1f);

            //draw background
            canvas.drawRGB(32,32,32);

            //draw room backgrounds
            for (Room room : levelState.getMap().getRoomPerimeters().values()){
                room.getPerimeter().draw(canvas, renderOffset, interpolationVector, false);
            }

            //draw grids
            if (debugInfo.renderPhysicsGrid())
                drawGrid(canvas, grid.getGridMinimum(), grid.getGridMaximum(), grid.getCellSize(), renderOffset, textPaint);

            if (debugInfo.renderMapTileGrid())
                drawGrid(canvas, new Point(), new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight()), tileSize, renderOffset, tilePaint);

            for (Door door : levelState.getMap().getDoors()){
                door.draw(canvas, renderOffset, interpolationVector, debugInfo.renderEntityNames());
            }

            //draw player and entities
            levelState.getPlayer().draw(canvas, renderOffset, new Vector(), debugInfo.renderEntityNames());

            for (Renderable entity : enemiesToRender){
                entity.draw(canvas, renderOffset, interpolationVector, debugInfo.renderEntityNames());
            }

            for (Wall entity : wallsToRender){
                entity.draw(canvas, renderOffset, interpolationVector, debugInfo.renderEntityNames());
            }

            //draw grid labels
            if (debugInfo.renderMapTileGrid()){
                drawGridLabels(canvas, new Point(), new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight()), tileSize, renderOffset);
            }

            //draw ui
            if (!levelState.isPaused()) {
                RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, "RenderFPS: " + renderFPS
                        + ". GameTickFPS: " + gameTickFPS + ". Collisions: " + level.getCollisionManager().collisionCount,
                        new Point(viewWidth/2f, 50), Color.WHITE, 10);
            } else {
                canvas.drawARGB(200, 0,0,0);

                int oldColor = textPaint.getColor();
                textPaint.setColor(Color.WHITE);
                RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, "Game is Paused", new Point(viewWidth/2f, viewHeight/3f), Color.RED, 50);
                textPaint.setColor(oldColor);
            }

            //draw in game ui
            inputManager.draw(canvas, textPaint);
        }
        try {
            getHolder().unlockCanvasAndPost(canvas);
        } catch (IllegalArgumentException e){
            //canvas is destroyed already
        }
    }

    private void drawGridLabels(Canvas canvas, Point minimum, Point maximum, int cellSize, Point renderOffset){
        Point center;
        for (int i = (int)minimum.getX(); i < maximum.getX()/cellSize; i++){
            for (int j = (int)minimum.getY(); j < maximum.getY()/cellSize; j++){
                center = new Point(i * cellSize + cellSize/2f, j * cellSize + cellSize/2f).add(renderOffset);

                int tileColor = Color.WHITE;

                for (AIEntity entity : levelState.getEnemyEntities()){
                    if (entity.getPath() != null) {
                        for (Tile tile : entity.getPath()) {
                            if (tile.equals(new Tile(i, j))) {
                                tileColor = Color.RED;
                                break;
                            }
                        }
                    }
                }
                RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, (i*tileSize) + ",\n" + (j*tileSize), center, tileColor, 10);
            }
        }
    }

    private void drawGrid(Canvas canvas, Point minimum, Point maximum, int cellSize, Point renderOffset, Paint gridPaint){
        maximum = maximum.add(minimum.smult(-1f));

        for (float i = minimum.getX(); i <= maximum.getX(); i += cellSize){
            canvas.drawLine(i+renderOffset.getX(),minimum.getY()+renderOffset.getY(), i+renderOffset.getX(), maximum.getY() + renderOffset.getY(), gridPaint);
        }

        for (float i = minimum.getY(); i <= maximum.getY(); i += cellSize){
            canvas.drawLine(minimum.getX()+renderOffset.getX(),i+renderOffset.getY(), maximum.getX() + renderOffset.getX(), i+renderOffset.getY(), gridPaint);
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