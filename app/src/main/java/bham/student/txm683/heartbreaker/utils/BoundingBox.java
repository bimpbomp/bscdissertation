package bham.student.txm683.heartbreaker.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class BoundingBox {
    private int top;
    private int bottom;
    private int left;
    private int right;

    public BoundingBox(int left, int top, int right, int bottom) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public BoundingBox(Point topLeft, Point bottomRight){
        this.top = (int) topLeft.getY();
        this.left = (int) topLeft.getX();
        this.bottom = (int) bottomRight.getY();
        this.right = (int) bottomRight.getX();
    }

    public BoundingBox(Point[] vertices){
        this.top = Integer.MAX_VALUE;
        this.bottom = Integer.MIN_VALUE;
        this.left = Integer.MAX_VALUE;
        this.right = Integer.MIN_VALUE;

        for (Point point : vertices){
            if (point.getX() < this.getLeft())
                this.setLeft(point.getX());

            if (point.getX() > this.getRight())
                this.setRight(point.getX());

            if (point.getY() < this.getTop())
                this.setTop(point.getY());

            if (point.getY() > this.getBottom())
                this.setBottom(point.getY());
        }
    }

    public boolean intersecting(BoundingBox bb){
        return this.left < bb.right && this.right > bb.left && this.top < bb.bottom && this.bottom > bb.top;
    }

    public boolean intersecting(Point p){
        Log.d("hb::BoundingBox", left + ", " + right + ", " + top + ", " + bottom + ": " + p.getX() + ", " + p.getY());
        Log.d("hb::BoundingBox", (this.left < p.getX()) + "," + (this.right > p.getX()) + ", " + (this.top < p.getY()) + "," + (this.bottom > p.getY()));
        return this.left < p.getX() && this.right > p.getX() && this.top < p.getY() && this.bottom > p.getY();
    }

    public Point[] getCollisionVertices(){
        return new Point[]{
                getTopLeft(),
                getTopRight(),
                getBottomRight(),
                getBottomLeft()
        };
    }

    public void draw(Canvas canvas, Point renderOffset, Paint paint){
        Point tl = getTopLeft().add(renderOffset);
        Point br = getBottomRight().add(renderOffset);
        canvas.drawRect(tl.getX(),tl.getY(),br.getX(),br.getY(),paint);
    }

    public Point getTopLeft(){
        return new Point(left, top);
    }

    public Point getTopRight(){
        return new Point(right, top);
    }

    public Point getBottomLeft(){
        return new Point(left, bottom);
    }

    public Point getBottomRight(){
        return new Point(right, bottom);
    }

    public int getTop() {
        return top;
    }

    public void setTop(float top) {
        this.top = (int) top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(float bottom) {
        this.bottom = (int) bottom;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(float left) {
        this.left = (int) left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(float right) {
        this.right = (int) right;
    }

    public String toString(){
        return "l: " + left + ", t: " + top + ", r: " + right + ", b: " + bottom;
    }
}
