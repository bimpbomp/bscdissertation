package bham.student.txm683.heartbreaker.input;

import bham.student.txm683.heartbreaker.utils.Point;

public interface InputUIElement {
    boolean containsPoint(Point eventPosition);

    void setPointerID(int id);

    boolean hasID(int id);
}
