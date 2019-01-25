package bham.student.txm683.heartbreaker.rendering;

import android.graphics.Paint;

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
}
