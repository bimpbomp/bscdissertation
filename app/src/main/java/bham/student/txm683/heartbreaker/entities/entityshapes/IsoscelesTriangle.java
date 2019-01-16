package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class IsoscelesTriangle extends EntityShape {

    private static final String TAG = "hb::TRIANGLE";
    private static final float TWO_THIRDS_CONSTANT = 0.6667f;

    //relative to the geometricCenter at zero rotation
    private Vector topVector;
    private Vector rightBaseVector;
    private Vector leftBaseVector;

    public IsoscelesTriangle(Point geometricCenter, float baseWidth, float height, int colorValue){
        super(geometricCenter, baseWidth, height, colorValue);
        init();
    }

    /**
     * Initialises the triangle in zero rotation
     */
    private void init(){

        float twoThirdsHeight = this.height * TWO_THIRDS_CONSTANT;

        topVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY() - twoThirdsHeight));

        float baseY = geometricCenter.getY() + (height-twoThirdsHeight);

        rightBaseVector = new Vector(geometricCenter, new Point(geometricCenter.getX() + (width / 2), baseY));
        leftBaseVector = new Vector(geometricCenter, new Point(geometricCenter.getX() - (width / 2), baseY));
    }

    /**
     * Translates the vertices of this shape by the given movement vector.
     * @param movementVector The vector to move the vertices.
     */
    private void translateShape(Vector movementVector){
        Point amountToTranslate = movementVector.getRelativeToTailPoint();

        this.geometricCenter = this.geometricCenter.add(amountToTranslate);

        topVector = topVector.translate(amountToTranslate);
        rightBaseVector = rightBaseVector.translate(amountToTranslate);
        leftBaseVector = leftBaseVector.translate(amountToTranslate);
    }

    private void rotateVertices(Vector movementVector){

        float angle = calculateAngleBetweenVectors(topVector, movementVector);

        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);


        if (!(Math.abs(cosAngle - 1f) < 0.0001f)) {

            topVector = rotateVertexVector(topVector, cosAngle, sinAngle);

            rightBaseVector = rotateVertexVector(rightBaseVector, cosAngle, sinAngle);

            leftBaseVector = rotateVertexVector(leftBaseVector, cosAngle, sinAngle);
        }
    }

    private float calculateAngleBetweenVectors(Vector primaryVector, Vector movementVector){
        float dot = primaryVector.dot(movementVector);
        float det = primaryVector.det(movementVector);

        return (float) Math.atan2(det, dot);
    }

    @Override
    public void move(Vector movementVector){
        rotateVertices(movementVector);
        translateShape(movementVector);
    }

    @Override
    public Point getInterpolatedCenter(Vector interpolationVector){
        return this.geometricCenter.add(interpolationVector.getRelativeToTailPoint());
    }

    @Override
    public Point[] getInterpolatedVertices(Vector interpolationVector){
        if (!interpolationVector.equals(new Vector())) {
            Point amountToAdd = interpolationVector.getRelativeToTailPoint();

            float angle = calculateAngleBetweenVectors(topVector, interpolationVector);

            float cosAngle = (float) Math.cos(angle);

            float sinAngle = (float) Math.sin(angle);

            return new Point[]{rotateVertexVector(topVector, cosAngle, sinAngle).translate(amountToAdd).getHead(),
                    rotateVertexVector(rightBaseVector, cosAngle, sinAngle).translate(amountToAdd).getHead(),
                    rotateVertexVector(leftBaseVector, cosAngle, sinAngle).translate(amountToAdd).getHead()};
        }
        return new Point[] {topVector.getHead(), rightBaseVector.getHead(), leftBaseVector.getHead()};
    }

    @Override
    public void setCenter(Point geometricCenter){
        translateShape(new Vector(this.geometricCenter, geometricCenter));
    }

    public Point getCenter(){
        return this.geometricCenter;
    }

    @Override
    public Point[] getVertices() {
        return new Point[] {topVector.getHead(), rightBaseVector.getHead(), leftBaseVector.getHead()};
    }

    //Explanation in notebook (1)
    @Override
    public void setHeight(float newHeight) {
        float proportionOfHeightChange = newHeight/height;

        this.topVector = this.topVector.sMult(proportionOfHeightChange);

        float oneThirdHeightSquared = (this.height/3f)*(this.height/3f);
        float newThirdHeightSquared = (newHeight/3f)/(newHeight/3f);
        float widthSquaredOverFour = (width*width)/4f;

        updateBaseLengths(widthSquaredOverFour, oneThirdHeightSquared,
                widthSquaredOverFour, newThirdHeightSquared);

        this.height = newHeight;
    }

    //explanation in notebook (1)
    @Override
    public void setWidth(float newWidth) {
        float oneThirdHeightSquared = (this.height/3f)*(this.height/3f);
        float widthSquaredOverFour = (width*width)/4f;
        float newWidthSquaredOverFour = (newWidth*newWidth)/4f;

        updateBaseLengths(widthSquaredOverFour, oneThirdHeightSquared, newWidthSquaredOverFour, oneThirdHeightSquared);

        this.width = newWidth;
    }

    private void updateBaseLengths(float oldWidthSquaredOverFour, float oldOneThirdHeightSquared, float newWidthSquaredOverFour, float newOneThirdHeightSquared){

        float proportionOfBaseVectorLengthChange = (float) Math.sqrt(newOneThirdHeightSquared + newWidthSquaredOverFour)/
                (float) Math.sqrt(oldOneThirdHeightSquared + oldWidthSquaredOverFour);

        this.rightBaseVector = this.rightBaseVector.sMult(proportionOfBaseVectorLengthChange);
        this.leftBaseVector = this.leftBaseVector.sMult(proportionOfBaseVectorLengthChange);
    }

    @Override
    public void draw(Canvas canvas, Vector interpolationVector) {
        Path path;

        if (interpolationVector.equals(new Vector())) {
            //Log.d(TAG+":draw", "zero interpol vector");
            path = Point.getPathWithPoints(getVertices());
        } else {
            //Log.d(TAG+":draw", interpolationVector.toString());
            path = Point.getPathWithPoints(getInterpolatedVertices(interpolationVector));
        }

        canvas.drawPath(path, this.paint);
    }

    @Override
    @NonNull
    public String toString() {
        return "IsoscelesTriangle{" +
                "geometricCenter=" + geometricCenter +
                ", topVector=" + topVector +
                ", rightBaseVector=" + rightBaseVector +
                ", leftBaseVector=" + leftBaseVector +
                '}';
    }
}
