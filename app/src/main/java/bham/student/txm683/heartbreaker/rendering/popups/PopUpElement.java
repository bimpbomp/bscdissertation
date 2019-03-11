package bham.student.txm683.heartbreaker.rendering.popups;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.utils.Point;

public interface PopUpElement {

    int getHeight();
    int getWidth();
    void setCenter(Point point);
    int getVerticalPosition();
    void draw(Canvas canvas, Point point, Paint textPaint);

    static int boundVerticalPosition(int verticalPosition){
        if (verticalPosition < 0)
            verticalPosition = 0;
        else if (verticalPosition > 100)
            verticalPosition = 100;

        return verticalPosition;
    }
}
