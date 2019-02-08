package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Wall extends Entity implements Collidable, Renderable {

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

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName){

        shape.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        Point offsetCenter = getCenter().add(renderOffset);

        canvas.drawCircle(offsetCenter.getX(), offsetCenter.getY(), 10, new Paint());

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
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
        return collisionVertices;
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
        shape.translateShape(new Vector(shape.getCenter(), newCenter));
    }
}