package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Button implements InputUIElement {
    private Paint paint;
    private int radius;
    private Point center;

    private int pointerID;

    private int defaultColor;
    private int pressedColor;

    private int currentColor;

    private String label;

    private Click buttonFunction;

    public Button(String label, Point center, int radius, int color, Click buttonFunction){
        this.center = center;
        this.radius = radius;

        this.paint = new Paint();
        this.paint.setColor(color);
        this.pressedColor = Color.GRAY;

        this.defaultColor = color;

        this.pointerID = MotionEvent.INVALID_POINTER_ID;

        this.buttonFunction = buttonFunction;

        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void setPointerID(int id) {
        if (id >= 0)
            this.paint.setColor(pressedColor);

        this.pointerID = id;
    }

    public void onClick() {
        this.pointerID = MotionEvent.INVALID_POINTER_ID;
        this.paint.setColor(defaultColor);

        if (this.buttonFunction != null)
            this.buttonFunction.click();
    }

    @Override
    public boolean hasID(int id) {
        return pointerID == id;
    }

    public void draw(Canvas canvas, Paint textPaint){
        canvas.drawCircle(center.getX(), center.getY(), radius, paint);
        RenderingTools.renderCenteredText(canvas, textPaint, label, center);
    }

    public boolean containsPoint(Point touchEventPosition){
        return new Vector(center, touchEventPosition).getLength() <= radius;
    }

    @Override
    public void cancel() {
        this.pointerID = MotionEvent.INVALID_POINTER_ID;
        this.paint.setColor(defaultColor);
    }

    @Override
    public int getID() {
        return pointerID;
    }

    public Point getCenter() {
        return center;
    }
}
