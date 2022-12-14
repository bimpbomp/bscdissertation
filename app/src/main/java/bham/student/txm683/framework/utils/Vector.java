package bham.student.txm683.framework.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class Vector {

    public static final Vector ZERO_VECTOR;

    static {
        ZERO_VECTOR = new Vector();
    }

    private final Point tail;
    private final Point head;

    private final float xRelativeToTail;
    private final float yRelativeToTail;

    public Vector(){
        //affine space definition
        tail = new Point();
        head = new Point();

        //vector space definition
        xRelativeToTail = 0f;
        yRelativeToTail = 0f;
    }

    /**
     * Constructs a new Vector object with tail at the origin and head at the Point constructed
     * from the given components.
     *
     * @param headX x component of new vector relative to origin
     * @param headY y component of new vector relative to origin
     */
    public Vector(float headX, float headY){
        tail = new Point();
        head = new Point(headX, headY);

        xRelativeToTail = headX;
        yRelativeToTail = headY;
    }

    /**
     * Constructs a new Vector object with tail at the origin and head at the given Point.
     * @param head Point to place head of vector
     */
    public Vector(Point head){
        this(head.getX(), head.getY());
    }

    /**
     * Constructs a new Vector object with tail at the given position and head at the given position.
     * @param tail Position of the new vector's tail
     * @param head Position of the new vector's head
     */
    public Vector(Point tail, Point head){
        this.tail = tail;
        this.head = head;

        xRelativeToTail = head.getX() - tail.getX();
        yRelativeToTail = head.getY() - tail.getY();
    }

    public Vector(JSONObject jsonObject) throws JSONException{
        this(new Point((JSONObject)jsonObject.get("tail")), new Point((JSONObject)jsonObject.get("head")));
    }

    /**
     * Calculates this vector's length
     * @return vector length
     */
    public float getLength(){

        if ((Float.compare(0f, xRelativeToTail)== 0) && (Float.compare(0f, yRelativeToTail) == 0))
            return 0;
        else {
            return (float)Math.sqrt((xRelativeToTail * xRelativeToTail) + (yRelativeToTail * yRelativeToTail));
        }
    }

    public Vector setLength(float desiredLength){
        return this.getUnitVector().sMult(desiredLength);
    }

    /**
     * Calculates the unit vector for this vector.
     * @return Unit vector, with tail position preserved.
     */
    public Vector getUnitVector(){

        float length = this.getLength();
        Vector unitVector;

        if (!(Float.compare(length, 0f) == 0)){
            if (isFromOrigin())
                unitVector = new Vector(xRelativeToTail / length, yRelativeToTail / length);
            else {
                unitVector = this.sMult(1/length);
            }
        } else {
            unitVector = Vector.ZERO_VECTOR;
        }

        return unitVector;
    }

    /**
     * VECTOR SPACE OPERATION
     *
     * Returns the sum vector of this vector and the given vector.
     *
     * @param vector Vector to add on to this vector object
     * @return Sum vector with tail position of this vector preserved
     */
    public Vector vAdd(Vector vector){
        Vector sumVectorFromOrigin = new Vector(vector.xRelativeToTail + xRelativeToTail,
                vector.yRelativeToTail + yRelativeToTail);
        return sumVectorFromOrigin.translate(tail);
    }

    public Vector vSub(Vector v){
        return this.vAdd(v.sMult(-1));
    }

    /**
     * AFFINE SPACE OPERATION
     *
     * Translates this vector by the given point (treated as a vector with tail at origin)
     * @param point coordinates to add onto vector
     * @return translated vector
     */
    public Vector translate(Point point){
        return new Vector(tail.add(point), head.add(point));
    }

    public Point getRelativeToTailPoint(){
        return new Point(this.xRelativeToTail, this.yRelativeToTail);
    }

    /**
     *  VECTOR SPACE OPERATION
     *
     * Multiplies the length of this vector without changing direction.
     * Tail position is preserved.
     *
     * @param scalar Scalar to multiply length by.
     * @return new Vector object with length equal to oldLength*scalar, with tail position preserved).
     */
    public Vector sMult(float scalar){

        if (Float.compare(scalar, 0f) == 0){
            return new Vector(tail, tail);
        }
        return new Vector((xRelativeToTail * scalar), (yRelativeToTail * scalar)).translate(tail);
    }

    /**
     * Returns the dot product of the two vectors
     * @param vector Vector to dot with
     * @return Dot product
     */
    public float dot(Vector vector){
        return (xRelativeToTail * vector.xRelativeToTail) + (yRelativeToTail * vector.yRelativeToTail);
    }

    public float det(Vector vector){
        return (xRelativeToTail * vector.yRelativeToTail) - (yRelativeToTail * vector.xRelativeToTail);
    }

    /**
     * Calculates the angle from the primary vector to the movement vector
     * @param primaryVector Vector of the shape
     * @param movementVector Vector to know angle to
     * @return The angle between the given vectors
     */
    public static float calculateAngleBetweenVectors(Vector primaryVector, Vector movementVector){
        primaryVector = primaryVector.getUnitVector();
        movementVector = movementVector.getUnitVector();

        float dot = primaryVector.dot(movementVector);
        float det = primaryVector.det(movementVector);

        return (float) Math.atan2(det, dot);
    }

    /**
     * Rotates this vector by an angle. Rotation is relative this vector's tail position.
     * @param cosAngle Cos of angle to rotate by
     * @param sinAngle Sin of angle to rotate by
     * @return The rotated vector.
     */
    public Vector rotate(float cosAngle, float sinAngle){
        float newX = xRelativeToTail * cosAngle - yRelativeToTail * sinAngle;
        float newY = xRelativeToTail * sinAngle + yRelativeToTail * cosAngle;
        return new Vector(tail, new Point(newX + tail.getX(), newY + tail.getY()));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Vector rotateAntiClockwise90(){
        return new Vector(yRelativeToTail, -1f * xRelativeToTail);
    }

    public Vector rotateClockwise90(){
        return new Vector(-1 * yRelativeToTail, xRelativeToTail);
    }

    public static Vector proj(Vector u, Vector v){
        float dot = u.dot(v);

        float v2 = (float) Math.pow(v.getLength(), 2);

        return v.sMult(dot/v2);
    }

    public boolean isFromOrigin(){
        return tail.equals(new Point());
    }

    /**
     * VECTOR SPACE OPERATION
     *
     * Evaluates whether two vectors are equal in direction and magnitude.
     *
     * @param obj Object to be compared
     * @return True if vectors are equal
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this){
            return true;
        }

        if (!(obj instanceof Vector)){
            return false;
        }

        Vector v = (Vector) obj;

        return (Float.compare(xRelativeToTail,v.xRelativeToTail)==0) && (Float.compare(yRelativeToTail, v.yRelativeToTail)==0);
    }

    /**
     *
     * @return Vector's x component relative to origin
     */
    public float getXRelativeToTail() {
        return xRelativeToTail;
    }

    /**
     *
     * @return Vector's y component relative to origin
     */
    public float getYRelativeToTail() {
        return yRelativeToTail;
    }

    public Point getHead(){
        return this.head;
    }

    public Point getTail() {
        return this.tail;
    }

    @NonNull
    @Override
    public String toString() {
        return "tail: " + this.tail.toString() + ", head: " + this.head.toString() + ", x: " + xRelativeToTail + ", y: " + yRelativeToTail;
    }

    public String relativeToString(){
        return "x: " + xRelativeToTail + ", y: " + yRelativeToTail;
    }
}