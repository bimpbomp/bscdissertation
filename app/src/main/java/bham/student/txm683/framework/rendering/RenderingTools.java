package bham.student.txm683.framework.rendering;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import bham.student.txm683.framework.utils.Point;

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

    public static void renderCenteredTextWithBoundingBox(Canvas canvas, Paint textPaint, String text, Point center, int boxColor, int padding){
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        int textColor = textPaint.getColor();
        textPaint.setColor(boxColor);


        Rect bg = new Rect((int) (center.getX() - textBounds.width()/2) - padding, (int) (center.getY() - textBounds.height()/2) - padding, (int) (center.getX() + textBounds.width()/2)+padding, (int) (center.getY() + textBounds.height()/2)+padding);
        canvas.drawRect(bg, textPaint);

        textPaint.setColor(textColor);
        canvas.drawText(text, center.getX(), center.getY() - textBounds.exactCenterY(), textPaint);
    }
}