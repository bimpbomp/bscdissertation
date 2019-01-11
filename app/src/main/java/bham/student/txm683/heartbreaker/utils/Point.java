package bham.student.txm683.heartbreaker.utils;

import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    public Point add(Point p){
        return new Point(x + p.x, y + p.y);
    }

    public Point addVector(Vector v){
        return new Point(x + v.getX(), y + v.getY());
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
}
