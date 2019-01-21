package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Button implements InputUIElement {
    private Paint paint;
    private int radius;
    private Point center;

    private int pointerID;

    public Button(Point center, int radius, int color){
        this.center = center;
        this.radius = radius;

        this.paint = new Paint();
        this.paint.setColor(color);
    }

    @Override
    public void setPointerID(int id) {
        this.pointerID = id;
    }

    @Override
    public boolean hasID(int id) {
        return pointerID == id;
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(center.getX(), center.getY(), radius, paint);
    }

    public boolean containsPoint(Point touchEventPosition){
        return new Vector(center, touchEventPosition).getLength() <= radius;
    }
}
