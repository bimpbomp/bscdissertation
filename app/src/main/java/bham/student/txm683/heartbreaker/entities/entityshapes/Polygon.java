package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Path;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;

public abstract class Polygon implements Shape{

    private ShapeIdentifier shapeIdentifier;
    Vector[] vertexVectors;
    Vector forwardUnitVector;
    Point center;

    Polygon(Point center, Vector[] vertexVectors, ShapeIdentifier shapeIdentifier){
        this.vertexVectors = vertexVectors;
        this.center = center;

        this.forwardUnitVector = new Vector(0,-1);

        this.shapeIdentifier = shapeIdentifier;
    }

    /**
     * Sets the forwardUnitVector member of this class.
     * Is used when the object is rotated
     */
    abstract void setForwardUnitVector();

    /**
     * Rotates the shape to the direction of the given vector
     * @param movementVector Vector to align direction with
     **/
    public void rotateShape(Vector movementVector){

        float angle = calculateAngleBetweenVectors(forwardUnitVector, movementVector.getUnitVector());

        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        if (!(Math.abs(cosAngle - 1f) < 0.0001f)) {

            for (int i = 0; i < vertexVectors.length; i++){
                vertexVectors[i] = vertexVectors[i].rotate(cosAngle, sinAngle);
            }
        }

        setForwardUnitVector();
    }

    /**
     * Translates the vertices of this shape by the given movement vector.
     * @param movementVector Direction and magnitude to translate each vertex by
     */
    public void translateShape(Vector movementVector){
        Point amountToTranslate = movementVector.getRelativeToTailPoint();

        center = center.add(amountToTranslate);

        for (int i = 0; i < vertexVectors.length; i++){
            vertexVectors[i] = vertexVectors[i].translate(amountToTranslate);
        }
    }

    /**
     * Calculates the angle from the primary vector to the movement vector
     * @param primaryVector Vector of the shape
     * @param movementVector Vector to know angle to
     * @return The angle between the given vectors
     */
    float calculateAngleBetweenVectors(Vector primaryVector, Vector movementVector){
        float dot = primaryVector.dot(movementVector);
        float det = primaryVector.det(movementVector);

        return (float) Math.atan2(det, dot);
    }

    /**
     * Creates a path between the given points
     * @param points Points to join
     * @return Created path
     */
    static Path getPathWithPoints(Point[] points){
        Path path = new Path();

        if (points.length > 0) {
            path.moveTo(points[0].getX(), points[0].getY());

            for (Point point : points) {
                path.lineTo(point.getX(), point.getY());
            }
            path.close();
        }
        return path;
    }

    /**
     *
     * @param offset Amount to offset each vertex
     * @return The vertices of this shape in clockwise order starting at the top leftmost vertex offset by the given amount
     */
    public Point[] getVertices(Point offset) {
        return offsetVertices(getVertices(), offset);
    }

    /**
     *
     * @return The vertices of this shape in clockwise order starting at the top leftmost vertex
     */
    public Point[] getVertices() {
        Point[] vertices = new Point[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead();
        }
        return vertices;
    }

    public Point getCenter(){
        return center;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return this.shapeIdentifier;
    }

    /**
     * Adds the given offset to each element in the given array
     * @param vertices The vertices to be offset
     * @param offset The amount to offset the vertices
     * @return The offset vertices
     */
    static Point[] offsetVertices(Point[] vertices, Point offset){
        Point[] v = new Point[vertices.length];
        for (int i = 0; i < vertices.length; i++){
            v[i] = vertices[i].add(offset);
        }
        return v;
    }

    public static ArrayList<Vector> createTriangle(Point center, float width, float height){
        float twoThirdsHeight = height * 0.667f;
        ArrayList<Vector> vertices = new ArrayList<>();

        vertices.add(new Vector(center, new Point(center.getX(), center.getY() - twoThirdsHeight)));

        float baseY = center.getY() + (height-twoThirdsHeight);

        vertices.add(new Vector(center, new Point(center.getX() + (width / 2), baseY)));
        vertices.add(new Vector(center, new Point(center.getX() - (width / 2), baseY)));

        return vertices;
    }
}