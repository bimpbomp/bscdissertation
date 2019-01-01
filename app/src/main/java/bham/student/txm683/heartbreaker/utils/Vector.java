package bham.student.txm683.heartbreaker.utils;

public class Vector {
    private float x,y;

    public Vector(){
        this.x = 0f;
        this.y = 0f;
    }

    public Vector(float x, float y){
        this.x = x;
        this.y = y;
    }

    public float getX(){
        return this.x;
    }

    public float getY(){
        return this.y;
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

        if (x != 0f && y != 0f)
            return (float) Math.sqrt((x*x) + (y*y));
        else
            return 0;
    }

    public static Vector vAdd(Vector vector1, Vector vector2){
        return new Vector(vector1.x + vector2.x, vector1.y+vector2.y);
    }

    public static Vector sMult(Vector vector, float scalar){
        return new Vector(vector.x*scalar, vector.y*scalar);
    }


    public String toString(){
        return "[" + x + "," + y + "]";
    }
}
