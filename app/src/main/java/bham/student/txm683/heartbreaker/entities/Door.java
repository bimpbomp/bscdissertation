package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;

public class Door extends Entity {
    private int doorID;

    private boolean locked;

    public Door(int doorID, Point spawnCoordinates, int width, int height, boolean locked, int colorValue){
        super(doorID+"", spawnCoordinates, ShapeIdentifier.RECT, width, height, colorValue);
        this.doorID = doorID;
        this.locked = locked;
    }

    public int getDoorID() {
        return doorID;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
