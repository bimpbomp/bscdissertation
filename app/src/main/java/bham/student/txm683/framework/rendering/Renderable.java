package bham.student.txm683.framework.rendering;

import android.graphics.Canvas;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;

public interface Renderable {
    void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName);

    void setColor(int color);

    void revertToDefaultColor();

    BoundingBox getBoundingBox();

    String getName();
}
