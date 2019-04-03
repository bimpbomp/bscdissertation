package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Wall extends Entity implements Renderable {

    private Rectangle shape;

    public Wall(String name, Point topLeft, Point bottomRight, Point center, int colorValue){
        super(name);

        this.shape = new Rectangle(center, new Vector[]{
                new Vector(center, topLeft),
                new Vector(center, new Point(bottomRight.getX(), topLeft.getY())),
                new Vector(center, bottomRight),
                new Vector(center, new Point(topLeft.getX(), bottomRight.getY()))
        }, colorValue);
    }

    public static Wall build(JSONObject jsonObject) throws JSONException{
        String name = jsonObject.getString("name");
        Point center = new Point(jsonObject.getJSONObject("center"));
        Point tl = new Point(jsonObject.getJSONObject("tl"));
        Point br = new Point(jsonObject.getJSONObject("br"));
        int color = jsonObject.getInt("color");
        return new Wall(name, tl, br, center, color);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName){

        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

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
        return shape.getVertices();
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translate(new Vector(shape.getCenter(), newCenter));
    }
}