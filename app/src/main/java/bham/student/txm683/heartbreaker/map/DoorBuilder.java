package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.utils.Point;

public class DoorBuilder {
    private boolean locked;
    private boolean vertical;
    private Point center;
    private int color;

    public DoorBuilder(boolean locked, boolean vertical, Point center, int color) {
        this.locked = locked;
        this.vertical = vertical;
        this.center = center;
        this.color = color;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getKeyColor() {
        return color;
    }

    public boolean isVertical() {
        return vertical;
    }

    public Point getCenter() {
        return center;
    }
}
