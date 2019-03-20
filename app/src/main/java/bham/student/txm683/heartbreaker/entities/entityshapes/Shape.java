package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public interface Shape {
    ShapeIdentifier getShapeIdentifier();
    Vector getForwardUnitVector();
    Point getCenter();

    void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName);

    Point[] getVertices();
    Point[] getVertices(Point offset);

    void setColor(int color);
    void revertToDefaultColor();

    BoundingBox getBoundingBox();

    void translate(Vector movementVector);
    void translate(Point newCenter);

    void rotate(float angle);
    void rotate(Vector v);

    int getColor();
}
