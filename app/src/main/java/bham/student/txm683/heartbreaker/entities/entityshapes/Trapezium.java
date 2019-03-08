package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Trapezium extends Polygon implements Renderable {
    private float primaryAngle;

    private int currentColor;
    private int defaultColor;
    private Paint paint;

    public Trapezium(Vector[] vertexVectors, int color){
        super(vertexVectors[0].getTail(), vertexVectors, ShapeIdentifier.TRAPEZIUM);

        this.primaryAngle = Vector.calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);

        this.currentColor = color;
        this.defaultColor = color;

        this.paint = new Paint();
    }

    @Override
    public int getColor() {
        return defaultColor;
    }

    @Override
    void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle), (float) Math.sin(primaryAngle)).getUnitVector();
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        this.paint.setColor(currentColor);

        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public BoundingBox getBoundingBox() {
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