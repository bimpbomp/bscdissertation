package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.utils.Point;

public class KeyBuilder {

    private int unlocks;
    private Point center;

    public KeyBuilder(int unlocks, Point center) {
        this.unlocks = unlocks;
        this.center = center;
    }

    public int getUnlocks() {
        return unlocks;
    }

    public Point getCenter() {
        return center;
    }
}
