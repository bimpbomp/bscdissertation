package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public interface Shape {
    ShapeIdentifier getShapeIdentifier();
    Point getCenter();
    void translateShape(Vector movementVector);
    int getColor();
}
