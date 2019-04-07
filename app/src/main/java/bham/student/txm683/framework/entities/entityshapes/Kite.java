package bham.student.txm683.framework.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

import java.util.List;

public class Kite extends Polygon implements Renderable {
    private int currentColor;
    private int defaultColor;

    private Paint paint;

    public Kite(Point center, Vector[] vertexVectors, int color) throws IllegalArgumentException {
        super(center, vertexVectors, ShapeIdentifier.KITE);

        if (vertexVectors.length != 4){
            throw new IllegalArgumentException("Incorrect Number of vertices given to Kite class." +
                    " 4 vertices are needed, " + vertexVectors.length + " were provided");
        }

        this.defaultColor = color;
        this.currentColor = color;

        this.paint = new Paint();
        this.paint.setAntiAlias(true);
    }

    public static Kite constructKite(Point center, int size, int color){
        List<Vector> vertices = Polygon.createTriangle(center, size, size * 0.75f);

        center = new Point(center.getX(), vertices.get(1).getHead().getY());

        return new Kite(center, new Vector[]{
                vertices.get(0),
                vertices.get(1),
                new Vector(center, center.add(new Point(0, 0.5f * size))),
                vertices.get(2)
        }, color);
    }

    @Override
    protected void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].getUnitVector();
    }

    @Override
    public int getColor() {
        return currentColor;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {

        paint.setColor(currentColor);
        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
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