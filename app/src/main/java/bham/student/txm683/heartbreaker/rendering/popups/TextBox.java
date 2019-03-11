package bham.student.txm683.heartbreaker.rendering.popups;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;

public class TextBox implements PopUpElement {
    private int width;
    private int height;
    private Point center;
    private String label;
    private int color;
    private float textSize;

    private int verticalPosition;

    public TextBox(String label, Point center, int width, int height, int color, int verticalPosition, int textSize){
        this.label = label;
        this.width = width;
        this.height = height;
        this.center = center;
        this.color = color;

        this.textSize = textSize;

        this.verticalPosition = PopUpElement.boundVerticalPosition(verticalPosition);
    }

    @Override
    public int getVerticalPosition() {
        return verticalPosition;
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
    public void setCenter(Point point) {
        this.center = point;
    }

    @Override
    public void draw(Canvas canvas, Point point, Paint textPaint) {
        float oldSize = textPaint.getTextSize();
        textPaint.setTextSize(textSize);
        RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, label, center, color, 10);
        textPaint.setTextSize(oldSize);

        Log.d("TEXTBOX", "center: " + center);
    }
}
