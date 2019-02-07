package bham.student.txm683.heartbreaker.rendering;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public interface Renderable {
    void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName);

    void setColor(int color);

    void revertToDefaultColor();
}
