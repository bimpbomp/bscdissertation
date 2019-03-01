package bham.student.txm683.heartbreaker.rendering;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.heartbreaker.Level;
import bham.student.txm683.heartbreaker.LevelEndStatus;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.MenuActivity;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.input.Button;
import bham.student.txm683.heartbreaker.input.Click;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.Thumbstick;
import bham.student.txm683.heartbreaker.map.MapConstructor;
import bham.student.txm683.heartbreaker.map.Room;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.DebugInfo;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

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

        this.context = context;
    }

    public void returnToMenu(){
        Intent intent = new Intent(context, MenuActivity.class);

        intent.putExtra("bundle", levelState.getLevelEnder().createBundle());

        context.startActivity(intent);
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

            this.inputManager = new InputManager(levelState, context);

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

        int attackButtonRadius = 100;
        this.inputManager.setSecondaryWeaponButton(new Button("BOMB", new Point(viewWidth-(attackButtonRadius + thumbstickMaxRadius*2) - 20, viewHeight-attackButtonRadius -10), attackButtonRadius, Color.RED, null));

        //this.inputManager.setPrimaryWeaponButton(new Button("PEW", new Point(viewWidth-attackButtonRadius -10, viewHeight-attackButtonRadius-10), attackButtonRadius, Color.MAGENTA, null));

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

        visibleBounds = new BoundingBox(viewWorldOrigin, viewWorldMax);

        Canvas canvas = getHolder().lockCanvas();

        if (canvas != null){
            super.draw(canvas);

            Point renderOffset = viewWorldOrigin.sMult(-1f);

            //draw background
            canvas.drawRGB(32,32,32);

            //draw room backgrounds
            for (Room room : levelState.getMap().getRoomPerimeters().values()){
                if (isOnScreen(room.getPerimeter()))
                    room.getPerimeter().draw(canvas, renderOffset, secondsSinceLastGameTick, false);
            }

            //draw doors
            for (Renderable door : levelState.getMap().getDoors().values()){
                if (isOnScreen(door))
                    door.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
            }

            //draw pickups
            for (Renderable pickup : levelState.getPickups()){
                if (isOnScreen(pickup))
                    pickup.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
            }

            //draw dead ai
            for (AIEntity deadAI : levelState.getDeadAI()){
                if (isOnScreen(deadAI)){
                    deadAI.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
                }
            }

            //draw player
            levelState.getPlayer().draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());

            //draw alive ai
            for (Renderable entity : levelState.getAliveAIEntities()){
                if (isOnScreen(entity))
                    entity.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
            }

            if (isOnScreen(levelState.getCore()))
                levelState.getCore().draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());

            //draw explosions
            for (Renderable explosion : levelState.getLingeringExplosions()){
                explosion.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
            }
            levelState.removeLingeringExplosions();


            //draw walls
            for (Renderable wall : levelState.getMap().getWalls()){
                if (isOnScreen(wall))
                    wall.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
            }

            //draw bullets
            for (Renderable bullet : levelState.getBullets()){
                if (isOnScreen(bullet))
                    bullet.draw(canvas, renderOffset, secondsSinceLastGameTick, debugInfo.renderEntityNames());
            }

            //draw vis set (if turned on)
            if (debugInfo.renderVisSet()){
                Point center;
                for (Tile tile : levelState.getTileSet().getTilesVisibleToPlayer()){
                    Log.d("hb::DrawingVis", tile.toString());
                    center = new Point(tile.add(tileSize/2, tileSize/2)).add(renderOffset);
                    canvas.drawCircle(center.getX(), center.getY(), 20, tilePaint);
                }
            }

            //draw grid (if turned on)
            if (debugInfo.renderMapTileGrid())
                drawGrid(canvas, new Point(), new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight()), tileSize, renderOffset, tilePaint);

            //draw grid labels (if turned on)
            if (debugInfo.renderMapTileGrid()){
                drawGridLabels(canvas, new Point(), new Point(levelState.getMap().getWidth(), levelState.getMap().getHeight()), tileSize, renderOffset);
            }

            //draw ui
            if (!levelState.isPaused()) {
                RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, "RenderFPS: " + renderFPS
                        + " GameTickFPS: " + gameTickFPS,
                        new Point(viewWidth/2f, 50), Color.WHITE, 10);
            }
            if (levelState.isPaused()) {
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

                for (AIEntity entity : levelState.getAliveAIEntities()){
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

    public LevelState getLevelState() {
        return levelState;
    }
}