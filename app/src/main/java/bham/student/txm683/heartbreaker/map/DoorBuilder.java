package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

public class DoorBuilder {
    private Tile liesOn;
    private Point center;
    private boolean vertical;
    private boolean locked;
    private String name;

    public DoorBuilder(String name, Tile tile, boolean locked){
        this.name = name;
        this.liesOn = tile;
        this.locked = locked;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public Point getCenter() {
        return center;
    }

    public Tile getLiesOn() {
        return liesOn;
    }

    public boolean isVertical() {
        return vertical;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}