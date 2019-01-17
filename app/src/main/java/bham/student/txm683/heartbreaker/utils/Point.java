package bham.student.txm683.heartbreaker.utils;

import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import bham.student.txm683.heartbreaker.SaveableState;
import org.json.JSONException;
import org.json.JSONObject;

public class Point implements SaveableState {

    private static final String DELIMITER = ",";

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

    public Point(JSONObject jsonObject) throws JSONException{
        this.x = (float) jsonObject.getDouble("x");
        this.y = (float) jsonObject.getDouble("y");
    }

    public Point add(Point p){
        return new Point(x + p.x, y + p.y);
    }

    public Point addVector(Vector v){
        return new Point(x + v.getXRelativeToTail(), y + v.getYRelativeToTail());
    }

    public Point smult(float scalar){
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
    @NonNull
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public static Path getPathWithPoints(Point[] points){
        Path path = new Path();

        if (points.length > 0) {
            path.moveTo(points[0].x, points[0].y);

            for (Point point : points) {
                path.lineTo(point.x, point.y);
            }
        }
        path.close();
        return path;
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("x", x);
        jsonObject.put("y", y);

        return jsonObject;
    }

    /*private static Point createPointFromStateString(String stateString) throws ParseException {
        String[] split = stateString.split(",");

        Point point;
        try {
            float sX = Float.parseFloat(split[0]);
            float sY = Float.parseFloat(split[1]);
            point = new Point(sX, sY);
        } catch (IndexOutOfBoundsException e){
            throw new ParseException("Error parsing String for creation of Point object: String cannot be" +
                    " split with " + DELIMITER, 0);
        } catch (NumberFormatException e){
            throw new ParseException("Error parsing String for creation of Point object: error parsing floats",0);
        }
        return point;
    }*/
}
