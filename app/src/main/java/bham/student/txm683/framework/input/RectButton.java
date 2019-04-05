package bham.student.txm683.framework.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.framework.entities.entityshapes.Rectangle;
import bham.student.txm683.framework.rendering.RenderingTools;
import bham.student.txm683.framework.rendering.popups.PopUpElement;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

public class RectButton extends Button implements PopUpElement {

    private Rectangle shape;
    private int width;
    private int height;

    private int verticalPosition;

    public RectButton(String label, Point center, float width, float height, int color, int verticalPosition, Click buttonFunction) {
        super(label, center, color, buttonFunction);

        shape = new Rectangle(center, width, height, color);

        this.width = (int) width;
        this.height = (int) height;

        this.verticalPosition = PopUpElement.boundVerticalPosition(verticalPosition);
    }

    public RectButton(RectButtonBuilder builder, Point center){
        this(builder.getLabel(), center, 300, 100, Color.GRAY, builder.getVerticalPosition(), builder.getButtonFunction());
    }

    @Override
    public int getVerticalPosition() {
        return verticalPosition;
    }

    @Override
    public void setCenter(Point point) {
        this.shape.translate(new Vector(shape.getCenter(), point));
        super.setCenter(point);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setPointerID(int id) {
        super.setPointerID(id);
        shape.setColor(Color.DKGRAY);
    }

    @Override
    public void cancel() {
        super.cancel();
        shape.revertToDefaultColor();
    }

    @Override
    public void onClick() {
        super.onClick();
        shape.revertToDefaultColor();
    }

    @Override
    public void draw(Canvas canvas, Point point, Paint textPaint) {
        shape.draw(canvas, point, 0, false);
        RenderingTools.renderCenteredText(canvas, textPaint, getLabel(), getCenter());
    }

    @Override
    public void draw(Canvas canvas, Paint textPaint) {
        shape.draw(canvas, new Point(), 0, false);
        RenderingTools.renderCenteredText(canvas, textPaint, getLabel(), getCenter());
    }

    @Override
    public boolean containsPoint(Point touchEventPosition) {
        return shape.getBoundingBox().intersecting(touchEventPosition);
    }
}
