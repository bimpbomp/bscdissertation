package bham.student.txm683.framework.physics.fields;

import bham.student.txm683.framework.entities.entityshapes.Rectangle;
import bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

public class DoorField extends InteractionField {

    private Rectangle shape;

    public DoorField(String owner, String name, Point center, float width, float height, int color){
        super(owner, name);

        shape = new Rectangle(center, width, height, color);
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.RECTANGLE;
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
