package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.utils.Point;

public interface Collidable {

    Point[] getCollisionVertices();
    boolean isSolid();
}
