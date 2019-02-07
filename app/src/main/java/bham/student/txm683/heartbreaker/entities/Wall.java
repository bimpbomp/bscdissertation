package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Wall extends Entity implements Collidable, Renderable {

    private Point[] collisionVertices;

    private Rectangle shape;

    public Wall(String name, Point[] collisionVertices, Point topLeft, Point bottomRight, Point center, int colorValue){
        super(name, center);
        this.collisionVertices = collisionVertices;

        this.shape = new Rectangle(new Vector[]{
                new Vector(center, topLeft),
                new Vector(center, new Point(bottomRight.getX(), topLeft.getY())),
                new Vector(center, bottomRight),
                new Vector(center, new Point(topLeft.getX(), bottomRight.getY()))
        }, colorValue);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName){

        shape.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        if (renderEntityName)
            drawName(canvas, renderOffset);
    }

    @Override
    public void setColor(int color) {
        this.shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        this.shape.revertToDefaultColor();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    public Point[] getRenderableVertices() {
        return shape.getVertices();
    }

    public Point[] getCollisionVertices(){
        return collisionVertices;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }
}

/*
private Point[] collisionVertices;

    private Rectangle shape;

    private Paint paint;
    private int wallColor;
    private int textColor;

    private Point topLeft;
    private Point bottomRight;

    private Point offsetTopLeft;
    private Point offsetBottomRight;

    public Wall(String name, Point[] collisionVertices, Point topLeft, Point bottomRight, Point center, int colorValue){
        super(name, center);
        this.collisionVertices = collisionVertices;

        this.shape = new Rectangle(, colorValue);

        this.topLeft = topLeft;
        this.bottomRight = bottomRight;

        this.wallColor = colorValue;
        this.textColor = Color.GRAY;

        paint = RenderingTools.initPaintForText(wallColor, 28f, Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName){
        offsetTopLeft = topLeft.add(renderOffset);
        offsetBottomRight = bottomRight.add(renderOffset);

        canvas.drawRect(offsetTopLeft.getX()-2, offsetTopLeft.getY()-2, offsetBottomRight.getX()+2, offsetBottomRight.getY()+2, paint);

        if (renderEntityName) {
            paint.setColor(textColor);
            RenderingTools.renderCenteredText(canvas, paint, getName(), getPosition().add(renderOffset));
            paint.setColor(wallColor);
        }
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    public Point[] getRenderableVertices() {
        return new Point[]{topLeft, bottomRight};
    }

    public Point[] getCollisionVertices(){
        return collisionVertices;
    }

*/

/*if (renderingVertices.length == 2) {
            topLeft = renderingVertices[0];
            bottomRight = renderingVertices[1];

            float width = bottomRight.getX() - topLeft.getX();
            float height = bottomRight.getY() - topLeft.getY();

            center = topLeft.add(new Point(width/2f, height/2f));
        } else {
            center = new Point();
            topLeft = new Point();
            bottomRight = new Point();
        }
        */