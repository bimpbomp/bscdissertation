package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;

public class Hexagon extends Polygon implements Renderable {

    private Paint paint;
    private int currentColor;
    private int defaultColor;

    //30 degrees
    private static float primaryAngle = 0.523599f;

    public Hexagon(Point center, int size, int color){
        super(center, generateVertexVectors(center, size/2, 1.0472f, 6), ShapeIdentifier.HEXAGON);

        paint = new Paint();
        currentColor = color;
        defaultColor = color;
    }

    public static float getPrimaryAngle() {
        return primaryAngle;
    }

    @Override
    void setForwardUnitVector() {
        forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle), (float) Math.sin(primaryAngle));
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        paint.setColor(currentColor);
        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public void setColor(int color) {
        currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        currentColor = defaultColor;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(getVertices());
    }
}
