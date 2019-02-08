package bham.student.txm683.heartbreaker.physics;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class InteractionField implements Renderable, Collidable {

    private Rectangle shape;
    private String name;

    public InteractionField(Point center, Vector[] vertexVectors, int color){
        shape = new Rectangle(center, vertexVectors, color);
        this.name = "";
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, interpolationVector, renderEntityName);
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
        //TODO add to constructor
        //e.g. door field cant move but enemy sight and player melee can
        return false;
    }

    @Override
    public String getName() {
        return name;
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
