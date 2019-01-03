package bham.student.txm683.heartbreaker.utils;

import android.support.annotation.NonNull;

public class Vector {
    private float x,y;

    public Vector(){
        this.x = 0;
        this.y = 0;
    }

    public Vector(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
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

    public float getLength(){

        if ((Float.compare(0f, x)== 0) && (Float.compare(0f, y) == 0))
            return 0;
        else {
            //float a = (x * x) + (y * y);
            //System.out.println("x: " + x + ", y: " + y + ", x^2: " + x * x + ", y^2: " + y * y + ", x^2 + y^2: " + a + ", double: " + (double) (a));
            return (float)Math.sqrt((x * x) + (y * y));
        }
    }

    public boolean equals(Vector vector) {
        float delta = 0.0001f;

        return (Math.abs(vector.x-x)<delta) && (Math.abs(vector.y-y)<delta);
    }

    @NonNull
    public String toString(){
        return "[" + x + "," + y + "]";
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