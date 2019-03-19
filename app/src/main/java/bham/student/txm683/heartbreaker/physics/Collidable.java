package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;

public interface Collidable {

    Point[] getCollisionVertices();
    BoundingBox getBoundingBox();

    boolean isSolid();
    boolean canMove();

    String getName();

    ShapeIdentifier getShapeIdentifier();
    CollidableType getCollidableType();

    Point getCenter();
    void setCenter(Point newCenter);

    //Vector getForwardUnitVector();
}
