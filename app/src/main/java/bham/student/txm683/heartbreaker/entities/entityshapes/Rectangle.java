package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Rectangle extends Polygon implements Renderable {
    private float primaryAngle;

    private Paint paint;
    private int defaultColor;
    private int currentColor;

    public Rectangle(Point center, Vector[] vertexVectors, int colorValue){
        super(center, vertexVectors, ShapeIdentifier.RECTANGLE);

        this.primaryAngle = Vector.calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);

        this.paint = new Paint();

        this.defaultColor = colorValue;
        this.currentColor = colorValue;
    }

    public Rectangle(Point center, float width, float height, int colorValue){
        this(center, new Vector[]{new Vector(center, center.add(new Point(-width/2f, -height/2f))),
                new Vector(center, center.add(new Point(width/2f, -height/2f))),
                new Vector(center, center.add(new Point(width/2f, height/2f))),
                new Vector(center, center.add(new Point(-width/2f, height/2f)))}, colorValue);
    }

    @Override
    void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle),
                (float) Math.sin(primaryAngle));
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        /*
         * Ignoring interpolationVector since it's not implemented yet,
         * also ignoring the renderEntityName boolean as it doesn't have a name
         * */

        paint.setColor(currentColor);
        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public void revertToDefaultColor() {
        this.currentColor = defaultColor;
    }

    @Override
    public void setColor(int color) {
        this.currentColor = color;
    }

    @Override
    public BoundingBox getRenderingVertices() {
        return new BoundingBox(getVertices());
    }
}