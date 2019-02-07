package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.entities.entityshapes.EntityShape;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;

public interface Collidable {
    boolean canMove();

    Point[] getCollisionVertices();
    String getName();
    ShapeIdentifier getShapeIdentifier();
    EntityShape getShape();
}
