package bham.student.txm683.framework.physics;

import bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;

public interface Collidable {

    Point[] getCollisionVertices();
    BoundingBox getBoundingBox();

    boolean isSolid();
    boolean canMove();

    String getName();

    ShapeIdentifier getShapeIdentifier();

    Point getCenter();
    void setCenter(Point newCenter);
}
