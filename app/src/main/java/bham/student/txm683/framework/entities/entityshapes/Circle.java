package bham.student.txm683.framework.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

public class Circle implements Renderable, Shape {

    private float radius;
    private Point center;

    private int defaultColor;
    private int currentColor;

    private Paint paint;

    public Circle(Point center, float radius, int color){
        this.radius = radius;
        this.center = center;

        this.defaultColor = color;
        this.currentColor = color;

        this.paint = new Paint();
    }

    @Override
    public Point[] getVertices() {
        return getBoundingBox().getCollisionVertices();
    }

    @Override
    public Point[] getVertices(Point offset) {
        Point[] vertices = getBoundingBox().getCollisionVertices();

        for (int i = 0; i < vertices.length; i++){
            vertices[i] = vertices[i].add(offset);
        }

        return vertices;
    }

    @Override
    public void translate(Point newCenter) {
        this.center = newCenter;
    }

    @Override
    public void rotate(Vector v) {

    }

    @Override
    public void rotate(float angle) {

    }

    @Override
    public Vector getForwardUnitVector() {
        return Vector.ZERO_VECTOR;
    }

    @Override
    public int getColor() {
        return defaultColor;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius){
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        paint.setColor(currentColor);

        Point offsetCenter = center.add(renderOffset);
        canvas.drawCircle(offsetCenter.getX(), offsetCenter.getY(), radius, paint);
    }

    @Override
    public void setColor(int color) {
        this.currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        this.currentColor = defaultColor;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(center.add(-1 * radius, -1 * radius), center.add(radius, radius));
    }

    @Override
    public String getName() {
        return "CIRCLE";
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.CIRCLE;
    }

    @Override
    public void translate(Vector movementVector) {
        center.add(movementVector.getRelativeToTailPoint());
    }
}
