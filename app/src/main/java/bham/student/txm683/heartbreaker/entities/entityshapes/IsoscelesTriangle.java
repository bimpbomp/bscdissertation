package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class IsoscelesTriangle extends EntityShape {
    private static final float TWO_THIRDS_CONSTANT = 0.6667f;

    private float twoThirdsHeight;

    //relative to the geometricCenter at zero rotation
    private Point topVertex;
    private Point rightBaseVertex;
    private Point leftBaseVertex;

    public IsoscelesTriangle(Point geometricCenter, float baseWidth, float height, int colorValue){
        super(geometricCenter, baseWidth, height, colorValue);

        calculateTwoThirdsHeight();
        calculateVertices();
    }

    private void calculateVertices(){
        topVertex = new Point(geometricCenter.getX(), geometricCenter.getY() - twoThirdsHeight);

        float baseY = geometricCenter.getY() + twoThirdsHeight;

        rightBaseVertex = new Point(geometricCenter.getX() + (width /2), baseY);
        leftBaseVertex = new Point(geometricCenter.getX() - (width /2), baseY);
    }

    @Override
    public Point getInterpolatedCenter(Vector interpolationVector){
        return this.geometricCenter.addVector(interpolationVector);
    }

    @Override
    public Point[] getInterpolatedVertices(Vector interpolationVector){
        return new Point[] {topVertex.addVector(interpolationVector), rightBaseVertex.addVector(interpolationVector), leftBaseVertex.addVector(interpolationVector)};
    }

    private void calculateTwoThirdsHeight(){
        this.twoThirdsHeight = this.height * TWO_THIRDS_CONSTANT;
    }

    @Override
    public void setCenter(Point geometricCenter){
        this.geometricCenter = geometricCenter;

        calculateVertices();
    }

    public Point getCenter(){
        return this.geometricCenter;
    }

    @Override
    public Point[] getVertices() {
        return new Point[] {topVertex, rightBaseVertex, leftBaseVertex};
    }

    //TODO: implement rotation
    @Override
    public void rotateByAngle(float angleToRotateBy) {

    }

    @Override
    public void setHeight(float newHeight) {
        this.height = newHeight;

        calculateTwoThirdsHeight();
        calculateVertices();
    }

    @Override
    public void setWidth(float newWidth) {
        this.width = newWidth;

        calculateVertices();
    }

    @Override
    public void draw(Canvas canvas, Vector interpolationVector) {
        Path path;

        if (interpolationVector.equals(new Vector())) {
            path = Point.getPathWithPoints(getVertices());
        } else {
            path = Point.getPathWithPoints(getInterpolatedVertices(interpolationVector));
        }

        canvas.drawPath(path, this.paint);
    }

    @Override
    @NonNull
    public String toString() {
        return "IsoscelesTriangle{" +
                "geometricCenter=" + geometricCenter +
                ", topVertex=" + topVertex +
                ", rightBaseVertex=" + rightBaseVertex +
                ", leftBaseVertex=" + leftBaseVertex +
                '}';
    }
}
