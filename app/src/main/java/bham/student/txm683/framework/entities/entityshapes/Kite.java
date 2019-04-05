package bham.student.txm683.framework.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

import java.util.List;

public class Kite extends Polygon implements Renderable {
    private int defaultUpperTriColor;
    private int currentUpperTriColor;
    private int defaultLowerTriColor;
    private int currentLowerTriColor;

    private Paint paint;

    public Kite(Point center, Vector[] vertexVectors, int upperTriColor, int lowerTriColor) throws IllegalArgumentException {
        super(center, vertexVectors, ShapeIdentifier.KITE);

        if (vertexVectors.length != 4){
            throw new IllegalArgumentException("Incorrect Number of vertices given to Kite class." +
                    " 4 vertices are needed, " + vertexVectors.length + " were provided");
        }

        this.defaultUpperTriColor = upperTriColor;
        this.currentUpperTriColor = upperTriColor;

        this.defaultLowerTriColor = lowerTriColor;
        this.currentLowerTriColor = lowerTriColor;

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
        }, color, color);
    }

    @Override
    void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].getUnitVector();
    }

    @Override
    public int getColor() {
        return defaultUpperTriColor;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        /*
        * Ignoring interpolationVector since it's not implemented yet,
        * also ignoring the renderEntityName boolean as it doesn't have a name
        * */

        paint.setColor(currentUpperTriColor);
        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(getVertices());
    }

    @Override
    public void setColor(int color) {
        this.currentLowerTriColor = color;
        this.currentUpperTriColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        this.currentLowerTriColor = defaultLowerTriColor;
        this.currentUpperTriColor = defaultUpperTriColor;
    }

    public int getCurrentLowerTriColor() {
        return currentLowerTriColor;
    }

    private Point[] upperTriangleVertices(){
        return new Point[]{
                vertexVectors[0].getHead(),
                vertexVectors[1].getHead(),
                vertexVectors[3].getHead()
        };
    }

    private Point[] lowerTriangleVertices(){
        return new Point[]{
                vertexVectors[1].getHead(),
                vertexVectors[2].getHead(),
                vertexVectors[3].getHead()
        };
    }
}