package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Door extends Entity {
    private int doorID;

    private boolean locked;

    private Rectangle tileBackground;

    public Door(int doorID, Point spawnCoordinates, int width, int height, boolean locked, int doorColor){
        super(doorID+"", spawnCoordinates, ShapeIdentifier.RECT, width, height, doorColor);
        this.doorID = doorID;
        this.locked = locked;
    }

    public void setTileBackground(int tileSize, int backgroundColor) {
        this.tileBackground = new Rectangle(spawnCoordinates, tileSize, tileSize, backgroundColor);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, boolean renderEntityName) {
        tileBackground.draw(canvas, renderOffset, new Vector());
        super.draw(canvas, renderOffset, renderEntityName);
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
