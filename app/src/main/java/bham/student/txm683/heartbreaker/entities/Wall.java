package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Wall {

    private String name;
    private Rectangle renderableShape;
    private Point[] collisionVertices;
    private Paint textPaint;

    public Wall(Point[] collisionVertices, int colorValue){
        this.collisionVertices = collisionVertices;

        float width;
        float height;
        Point center;
        if (collisionVertices.length > 4) {
            width = collisionVertices[collisionVertices.length - 1].getX() - collisionVertices[0].getX();
            height = collisionVertices[collisionVertices.length - 1].getY() - collisionVertices[0].getY();
        } else{
            width = collisionVertices[1].getX() - collisionVertices[0].getX();
            height = collisionVertices[2].getY() - collisionVertices[1].getY();
        }

        center = collisionVertices[0].add(new Point(width / 2f, height / 2f));
        this.renderableShape = new Rectangle(center, width + 5, height + 5, colorValue);

        textPaint = RenderingTools.initPaintForText(Color.BLACK, 28f, Paint.Align.CENTER);
    }

    public void draw(Canvas canvas, Point renderOffset, boolean renderEntityName){
        renderableShape.draw(canvas, renderOffset, null);

        if (renderEntityName) {
            Point interpolatedCenter = renderableShape.getInterpolatedCenter(new Vector(), renderOffset);
            RenderingTools.renderCenteredText(canvas, textPaint, name, interpolatedCenter);
        }
    }

    public Rectangle getRenderableShape() {
        return renderableShape;
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
