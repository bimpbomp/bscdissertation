package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;

public interface Collidable {

    Point[] getCollisionVertices();
    boolean isSolid();
    boolean canMove();
    String getName();
    ShapeIdentifier getShapeIdentifier();
    Point getPosition();
    void setPosition(Point newPosition);
}
