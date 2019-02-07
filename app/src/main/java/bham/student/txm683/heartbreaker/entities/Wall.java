package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;

public class Wall {

    private String name;

    private Point[] collisionVertices;
    private ShapeIdentifier shapeIdentifier;

    private Paint paint;
    private int wallColor;
    private int textColor;

    private Point topLeft;
    private Point bottomRight;
    private Point center;


    private Point offsetTopLeft;
    private Point offsetBottomRight;

    public Wall(Point[] collisionVertices, Point[] renderingVertices, int colorValue){
        this.collisionVertices = collisionVertices;
        shapeIdentifier = ShapeIdentifier.RECT;

        if (renderingVertices.length == 2) {
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

        this.wallColor = colorValue;
        this.textColor = Color.GRAY;

        paint = RenderingTools.initPaintForText(wallColor, 28f, Paint.Align.CENTER);
    }

    public void draw(Canvas canvas, Point renderOffset, boolean renderEntityName){
        offsetTopLeft = topLeft.add(renderOffset);
        offsetBottomRight = bottomRight.add(renderOffset);

        canvas.drawRect(offsetTopLeft.getX()-2, offsetTopLeft.getY()-2, offsetBottomRight.getX()+2, offsetBottomRight.getY()+2, paint);

        if (renderEntityName) {

            paint.setColor(textColor);
            RenderingTools.renderCenteredText(canvas, paint, name, center.add(renderOffset));
            paint.setColor(wallColor);
        }
    }

    public Point[] getRenderableVertices() {
        return new Point[]{topLeft, bottomRight};
    }

    public Point[] getCollisionVertices(){
        return collisionVertices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
