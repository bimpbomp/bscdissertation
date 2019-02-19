package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;

public class Octagon extends Polygon implements Renderable {

    private Paint paint;
    private int currentColor;
    private int defaultColor;

    //22.5 degrees
    private static float primaryAngle = 0.392699f;

    public Octagon(Point center, int size, int color){
        super(center, generateVertexVectors(center, size/2, 0.785398f, 8), ShapeIdentifier.OCTAGON);

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
