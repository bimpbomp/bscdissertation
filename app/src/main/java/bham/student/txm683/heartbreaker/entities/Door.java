package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Door extends Entity implements Renderable, Collidable {

    private Rectangle tileBackground;

    private Rectangle door;

    private boolean side1Locked;
    private boolean side2Locked;

    private boolean open;


    public Door(int doorID, Point spawnCoordinates, int width, int height, boolean side1Locked, boolean side2Locked, int doorColor){
        super(doorID+"", spawnCoordinates);

        this.door = new Rectangle(getPosition(), width, height, doorColor);

        this.side1Locked = side1Locked;
        this.side2Locked = side2Locked;

        this.open = false;
    }

    public void setTileBackground(int tileSize, int backgroundColor) {
        this.tileBackground = new Rectangle(getPosition(), tileSize, tileSize, backgroundColor);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        tileBackground.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        door.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        if (renderEntityName)
            drawName(canvas, renderOffset);
    }

    @Override
    public void setColor(int color) {

    }

    @Override
    public void revertToDefaultColor() {

    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public Point[] getCollisionVertices() {
        return door.getVertices();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    public boolean isSide1Locked() {
        return side1Locked;
    }

    public void setSide1Locked(boolean side1Locked) {
        this.side1Locked = side1Locked;
    }

    public boolean isSide2Locked() {
        return side2Locked;
    }

    public void setSide2Locked(boolean side2Locked) {
        this.side2Locked = side2Locked;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;

        if (open){

        } else {

        }
    }
}

/*private int doorID;

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
    }*/