package bham.student.txm683.heartbreaker.rendering;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;

public interface Renderable {
    void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName);

    void setColor(int color);

    void revertToDefaultColor();

    BoundingBox getBoundingBox();

    String getName();
}
