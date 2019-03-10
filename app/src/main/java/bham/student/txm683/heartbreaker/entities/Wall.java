package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Wall extends Entity implements Renderable {

    private Point[] collisionVertices;

    private Rectangle shape;

    public Wall(String name, Point[] collisionVertices, Point topLeft, Point bottomRight, Point center, int colorValue){
        super(name);
        this.collisionVertices = collisionVertices;

        this.shape = new Rectangle(center, new Vector[]{
                new Vector(center, topLeft),
                new Vector(center, new Point(bottomRight.getX(), topLeft.getY())),
                new Vector(center, bottomRight),
                new Vector(center, new Point(topLeft.getX(), bottomRight.getY()))
        }, colorValue);
    }

    public Wall(String name, Point topLeft, Point bottomRight, Point center, int colorValue){
        super(name);

        this.shape = new Rectangle(center, new Vector[]{
                new Vector(center, topLeft),
                new Vector(center, new Point(bottomRight.getX(), topLeft.getY())),
                new Vector(center, bottomRight),
                new Vector(center, new Point(topLeft.getX(), bottomRight.getY()))
        }, colorValue);
    }

    public Wall(String name, Point center, int size, int colorValue){
        super(name);

        this.shape = new Rectangle(center, size, size, colorValue);
        this.collisionVertices = shape.getVertices();
    }

    public static Wall build(JSONObject jsonObject) throws JSONException{
        String name = jsonObject.getString("name");
        Point center = new Point(jsonObject.getJSONObject("center"));
        Point tl = new Point(jsonObject.getJSONObject("tl"));
        Point br = new Point(jsonObject.getJSONObject("br"));
        int color = jsonObject.getInt("color");
        return new Wall(name, tl, br, center, color);
    }

    public JSONObject pack() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", getName());
        jsonObject.put("center", getCenter().getStateObject());

        BoundingBox b = getBoundingBox();
        jsonObject.put("tl", b.getTopLeft().getStateObject());
        jsonObject.put("br", b.getBottomRight().getStateObject());

        jsonObject.put("color", shape.getColor());

        return jsonObject;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName){

        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        //Point offsetCenter = getCenter().add(renderOffset);

        //canvas.drawCircle(offsetCenter.getX(), offsetCenter.getY(), 10, new Paint());

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
    }

    @Override
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public void setColor(int color) {
        this.shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        this.shape.revertToDefaultColor();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    public Point[] getCollisionVertices(){
        if (collisionVertices == null){
            return shape.getVertices();
        } else {
            return collisionVertices;
        }
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.WALL;
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translateShape(new Vector(shape.getCenter(), newCenter));
    }
}