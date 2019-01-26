package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Button implements InputUIElement {
    private Paint paint;
    private int radius;
    private Point center;

    private int pointerID;

    private int defaultColor;
    private int pressedColor = Color.BLACK;

    private Click onFire;

    public Button(Point center, int radius, int color, Click onFire){
        this.center = center;
        this.radius = radius;

        this.paint = new Paint();
        this.paint.setColor(color);

        this.defaultColor = color;

        this.pointerID = MotionEvent.INVALID_POINTER_ID;

        this.onFire = onFire;
    }

    @Override
    public void setPointerID(int id) {
        if (id >= 0)
            this.paint.setColor(pressedColor);
    }

    public void deactivate() {
        this.pointerID = MotionEvent.INVALID_POINTER_ID;
        this.paint.setColor(defaultColor);
        Log.d("hh", "button firing");
        this.onFire.onClick();
        Log.d("hh", "button fired");
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

    @Override
    public int getID() {
        return pointerID;
    }

    public Point getCenter() {
        return center;
    }
}
