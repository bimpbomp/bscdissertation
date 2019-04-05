package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.framework.entities.entityshapes.Polygon;
import bham.student.txm683.framework.entities.entityshapes.Shape;
import bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

import java.util.ArrayList;
import java.util.List;

public class TankShape extends Polygon implements Shape {

    private int defaultColor;
    private int currentColor;
    private Paint paint;

    private TankShape(Point center, Vector[] vertexVectors, int color){
        super(center, vertexVectors, ShapeIdentifier.TRAPEZIUM);

        this.paint = new Paint();

        this.currentColor = color;
        this.defaultColor = color;
    }

    public static TankShape build(Point center, int topwidth, int bottomWidth, int mainHeight, int pointHeight, int color){
        List<Vector> vertexVectors = new ArrayList<>();

        topwidth = topwidth/2;
        bottomWidth = bottomWidth/2;
        mainHeight = mainHeight/2;

        float cx = center.getX();
        float cy = center.getY();
        Point p;

        p = new Point(cx - topwidth, cy - mainHeight);
        vertexVectors.add(new Vector(center, p));

        p = new Point(cx, cy - mainHeight - pointHeight);
        vertexVectors.add(new Vector(center, p));

        p = new Point(cx + topwidth, cy - mainHeight);
        vertexVectors.add(new Vector(center, p));

        p = new Point(cx + bottomWidth, cy + mainHeight);
        vertexVectors.add(new Vector(center, p));

        p = new Point(cx - bottomWidth, cy + mainHeight);
        vertexVectors.add(new Vector(center, p));

        return new TankShape(center, vertexVectors.toArray(new Vector[0]), color);
    }

    public int getDefaultColor(){
        return defaultColor;
    }

    @Override
    void setForwardUnitVector() {
        forwardUnitVector = vertexVectors[1].getUnitVector();
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        paint.setColor(currentColor);

        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public void setColor(int color) {
        currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        currentColor = defaultColor;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(getVertices());
    }

    @Override
    public int getColor() {
        return currentColor;
    }
}
