package bham.student.txm683.framework.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class Point {

    private final float x;
    private final float y;

    public Point(){
        this.x = 0f;
        this.y = 0f;
    }

    public Point(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Point(Tile tile){
        this.x = tile.getX();
        this.y = tile.getY();
    }

    public Point(JSONObject jsonObject) throws JSONException{
        this.x = (float) jsonObject.getDouble("x");
        this.y = (float) jsonObject.getDouble("y");
    }

    public Point add(Point p){
        return new Point(x + p.x, y + p.y);
    }

    public Point add(float x, float y){
        return new Point(this.x + x, this.y + y);
    }

    public Point sub(Point p) {
        return add(p.sMult(-1f));
    }

    public Point sMult(float scalar){
        return new Point(x*scalar, y*scalar);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this){
            return true;
        }

        if (!(obj instanceof Point)){
            return false;
        }

        Point p = (Point) obj;

        float delta = 0.0001f;

        return (Math.abs(p.x-x)<delta) && (Math.abs(p.y-y)<delta);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13,31).append(x).append(y).toHashCode();
    }

    @Override
    @NonNull
    public String toString() {
        return "P(" + x +
                "," + y +
                ')';
    }
}
