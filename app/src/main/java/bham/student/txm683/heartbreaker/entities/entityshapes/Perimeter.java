package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

/**
 * Un-rotatable shape for defining a region as a room
 */
public class Perimeter implements Renderable, Collidable, Shape {

    private Point[] vertices;
    private int currentColor;
    private int defaultColor;
    private Paint paint;

    /**
     *
     * @param vertices Vertices of perimeter defined in a clockwise manner, starting at the top left vertex
     */
    public Perimeter(Point[] vertices, int colorValue) {
        this.vertices = vertices;

        this.defaultColor = colorValue;
        this.currentColor = colorValue;

        this.paint = new Paint();
    }

    public void convertToGlobal(int tileSize){
        for (int i = 0; i < vertices.length; i++){
            vertices[i] = vertices[i].smult(tileSize);
        }
    }

    @Override
    public Point[] getCollisionVertices() {
        return this.vertices;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        this.paint.setColor(currentColor);

        canvas.drawPath(Polygon.getPathWithPoints(Polygon.offsetVertices(vertices, renderOffset)), paint);
    }

    @Override
    public void setColor(int color) {
        this.currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        this.currentColor = defaultColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.PERIMETER;
    }

    @Override
    public Point getCenter() {
        return null;
    }

    @Override
    public void translateShape(Vector movementVector) {

    }

    @Override
    public void setCenter(Point newCenter) {

    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.PERIMETER;
    }
}
