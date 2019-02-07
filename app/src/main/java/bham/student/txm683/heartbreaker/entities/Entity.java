package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Entity  implements Collidable {
    private String name;
    private Point position;

    private Paint paint;

    Entity(String name, Point position){
        this.name = name;
        this.position = position;

        this.paint = new Paint();
        this.paint.setColor(Color.GRAY);
    }

    public String getName() {
        return name;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public abstract boolean canMove();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Entity)
            return name.equals(((Entity) o).name);

        return false;
    }

    public void drawName(Canvas canvas, Point renderOffset){
        RenderingTools.renderCenteredText(canvas, paint, getName(), getPosition().add(renderOffset));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 43).append(name).toHashCode();
    }
}

    /*String TAG;

    String name;
    Polygon shape;

    Point spawnCoordinates;

    Paint textPaint;

    boolean moveable;
    boolean launchedAttack;
    boolean collided;

    Vector pushVector;

    public Entity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, int colorValue){
        this.name = name;
        this.TAG = "hb::" + this.getClass().getSimpleName() + ":" + name;

        this.spawnCoordinates = spawnCoordinates;

        switch (shapeIdentifier){

            case ISO_TRIANGLE:
                this.shape = new IsoscelesTriangle(spawnCoordinates, width, height, colorValue);
                break;
            case RECT:
                this.shape = new Rectangle(spawnCoordinates, width, height, colorValue);
                break;
            case KITE:
                this.shape = new Kite(spawnCoordinates, width, height* 0.667f, height * 0.333f, colorValue);
                break;
            case ISO_TRAPEZIUM:
                this.shape = new IsoscelesTrapezium(spawnCoordinates, width* 0.667f, width, height, colorValue);
                break;
        }
        moveable = false;
        launchedAttack = false;

        pushVector = new Vector();

        initTextPaint();
    }

    public Entity(String name, Polygon shape){
        this.name = name;
        this.TAG = "hb::" + this.getClass().getSimpleName() + ":" + name;

        this.shape = shape;

        moveable = false;
        launchedAttack = false;

        pushVector = new Vector();

        initTextPaint();
    }

    public Entity(String stateString) throws ParseException, JSONException {
        JSONObject jsonObject = new JSONObject(stateString);

        this.name = jsonObject.getString("name");
        this.TAG = "hb::" + this.getClass().getName() + ":" + name;

        try {
            switch (ShapeIdentifier.fromInt(jsonObject.getInt("shapeidentifier"))) {
                case ISO_TRIANGLE:
                    this.shape = new IsoscelesTriangle((String)jsonObject.get("shape"));
                    break;
                case RECT:
                    this.shape = new Rectangle(jsonObject.getString("shape"));
                    break;
                case KITE:
                    this.shape = new Kite(jsonObject.getString("shape"));
                    break;
                case ISO_TRAPEZIUM:
                    this.shape = new IsoscelesTrapezium(jsonObject.getString("shape"));
                    break;
                default:
                    throw new ParseException("Invalid shape identifier", 0);
            }
        } catch (JSONException e){
            throw new ParseException(name + e.getMessage(),0);
        }

        moveable = false;
        pushVector = new Vector();
        launchedAttack = false;

        initTextPaint();
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getCollisionVertices();
    }

    public Point getSpawnCoordinates() {
        return spawnCoordinates;
    }

    public boolean canMove() {
        return moveable;
    }

    private void initTextPaint(){
        *//*textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setStrokeWidth(15f);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);*//*
        textPaint = RenderingTools.initPaintForText(Color.GRAY, 28f, Paint.Align.CENTER);
    }

    public void draw(Canvas canvas, Point renderOffset, boolean renderEntityName){

        if (collided){
            shape.setColor(Color.RED);
            collided = false;
        } else {
            shape.resetToDefaultColor();
        }

        shape.draw(canvas, renderOffset, new Vector());

        if (renderEntityName) {
            Point center = shape.getCenter().add(renderOffset);
            RenderingTools.renderCenteredText(canvas, textPaint, name, center);
        }
    }

    public boolean hasAttacked(){
        return launchedAttack;
    }

    public void setPushVector(Vector pushVector) {
        this.pushVector = pushVector.sMult(5);
    }

    public String getName(){
        return this.name;
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", name);
        jsonObject.put("shape", shape.getStateObject());
        jsonObject.put("shapeidentifier", shape.getShapeIdentifier().getId());
        return jsonObject;
    }

    public boolean isCollided() {
        return collided;
    }

    public void setCollided(boolean collided) {
        this.collided = collided;
    }

    public Polygon getShape(){
        return this.shape;
    }*/