package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.utils.Point;

public interface InputUIElement {
    boolean containsPoint(Point eventPosition);

    void setPointerID(int id);

    boolean hasID(int id);

    int getID();

    void cancel();

    void draw(Canvas canvas, Paint textPaint);
}
