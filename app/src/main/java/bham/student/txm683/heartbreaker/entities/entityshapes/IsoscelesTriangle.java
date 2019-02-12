package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class IsoscelesTriangle extends Polygon implements Renderable {

    private int currentColor;
    private int defaultColor;
    private Paint paint;

    public IsoscelesTriangle(Point center, Vector[] vertexVectors, int colorValue){
        super(center, vertexVectors, ShapeIdentifier.TRIANGLE);

        this.currentColor = colorValue;
        this.defaultColor = colorValue;

        this.paint = new Paint();
    }

    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].getUnitVector();
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        paint.setColor(currentColor);

        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public BoundingBox getRenderingVertices() {
        return new BoundingBox(getVertices());
    }

    @Override
    public void setColor(int color) {
        this.currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        this.currentColor = defaultColor;
    }
}