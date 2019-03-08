package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

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
    public int getColor() {
        return defaultColor;
    }

    public float getRadius() {
        return radius;
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
    public void translateShape(Vector movementVector) {
        center.add(movementVector.getRelativeToTailPoint());
    }
}
