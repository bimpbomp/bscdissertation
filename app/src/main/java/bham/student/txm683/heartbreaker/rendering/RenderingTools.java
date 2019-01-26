package bham.student.txm683.heartbreaker.rendering;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import bham.student.txm683.heartbreaker.utils.Point;

public class RenderingTools {

    private RenderingTools(){

    }

    //creates a black paint object for use with any text on screen.
    public static Paint initPaintForText(int color, float size, Paint.Align alignment){
        Paint textPaint;
        textPaint = new Paint();
        textPaint.setColor(color);
        textPaint.setStrokeWidth(2f);
        textPaint.setTextSize(size);
        textPaint.setTextAlign(alignment);

        return textPaint;
    }

    public static void renderCenteredText(Canvas canvas, Paint textPaint, String text, Point center){
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, center.getX(), center.getY() - textBounds.exactCenterY(), textPaint);
    }
}
