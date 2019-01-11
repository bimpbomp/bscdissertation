package bham.student.txm683.heartbreaker.utils;

import android.support.annotation.Nullable;

public class Vector {

    private final Point tail;
    private final Point head;

    private final float x;
    private final float y;

    public Vector(){
        tail = new Point();
        head = new Point();

        x = 0f;
        y = 0f;
    }

    public Vector(float headX, float headY){
        tail = new Point();
        head = new Point(headX, headY);

        x = headX;
        y = headY;
    }

    public Vector(Point head){
        this(head.getX(), head.getY());
    }

    public Vector(Point tail, Point head){
        this.tail = tail;
        this.head = head;

        x = head.getX() - tail.getX();
        y = head.getY() - tail.getY();
    }

    public float getLength(){

        if ((Float.compare(0f, x)== 0) && (Float.compare(0f, y) == 0))
            return 0;
        else {
            //float a = (x * x) + (y * y);
            //System.out.println("x: " + x + ", y: " + y + ", x^2: " + x * x + ", y^2: " + y * y + ", x^2 + y^2: " + a + ", double: " + (double) (a));
            return (float)Math.sqrt((x * x) + (y * y));
        }
    }

    public Vector getUnitVector(){

        float length = this.getLength();
        Vector unitVector;

        if (length != 0f){
            unitVector = new Vector(x/length, y/length);
        } else {
            unitVector = new Vector();
        }

        return unitVector;
    }

    public Vector vAdd(Vector vector){
        return new Vector(vector.x + x, vector.y+y);
    }

    public Vector sMult(float scalar){

        if (scalar == 0){
            return new Vector();
        }
        return new Vector((x*scalar), (y*scalar));
    }

    public Vector directionTo(Vector vector){
        return vAdd(sMult(this, -1), vector);
    }

    public float dot(Vector vector){
        return x*vector.x + y*vector.y;
    }

    public boolean isFromOrigin(){
        return head.equals(new Point());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this){
            return true;
        }

        if (!(obj instanceof Vector)){
            return false;
        }

        Vector v = (Vector) obj;

        return (v.tail.equals(tail)) && (v.head.equals(head));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public static Vector vAdd(Vector vector1, Vector vector2){
        return new Vector(vector1.x + vector2.x, vector1.y+vector2.y);
    }

    public static Vector sMult(Vector vector, float scalar){

        if (scalar == 0){
            return new Vector();
        }
        return new Vector((vector.x*scalar), (vector.y*scalar));
    }

    public static Vector fromAtoB(Vector a, Vector b){
        return vAdd(sMult(a,-1), b);
    }
}