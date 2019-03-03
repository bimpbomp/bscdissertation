package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.map.LockDoor;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.fields.DoorField;
import bham.student.txm683.heartbreaker.physics.fields.InteractionField;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Door extends Entity implements Renderable {

    private Rectangle tileBackground;

    private Rectangle doorShape;
    private Point center;

    private Rectangle primaryShape;
    private Rectangle secondaryShape;

    private boolean primaryLocked;
    private boolean secondaryLocked;

    private boolean open;

    private DoorField primaryField;
    private DoorField secondaryField;

    private LockDoor side1LockFun;
    private LockDoor side2LockFun;

    private static final int LOCKED_COLOR = Color.argb(150, 255, 0 ,0);
    private static final int UNLOCKED_COLOR = Color.argb(150, 0, 255, 0);

    private static final float DOOR_RATIO = 0.5f;
    private static final float TRANSLATION_RATIO = 0.25f;

    public Door(int doorID, Point center, int width, int height, boolean primaryLocked,
                boolean secondaryLocked, boolean vertical, int doorColor, LockDoor side1, LockDoor side2){

        super(doorID+"");

        this.primaryLocked = primaryLocked;
        this.secondaryLocked = secondaryLocked;

        this.open = false;
        this.center = center;

        if (vertical) {

            this.doorShape = new Rectangle(center, width * DOOR_RATIO, height, doorColor);

            this.primaryShape = new Rectangle(center, width * DOOR_RATIO, height, primaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
            this.primaryShape.translateShape(new Vector(width * -1 * TRANSLATION_RATIO, 0));

            this.secondaryShape = new Rectangle(center, width * DOOR_RATIO, height, secondaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
            this.secondaryShape.translateShape(new Vector(width * TRANSLATION_RATIO, 0));

            this.primaryField = new DoorField(doorID+"", "P"+doorID, center.add(new Point(-0.5f * width, 0)), width, height, Color.GRAY);
            this.secondaryField = new DoorField(doorID+"", "S"+doorID, center.add(new Point(0.5f * width, 0)), width, height, Color.LTGRAY);

        } else {

            this.doorShape = new Rectangle(center, width, height * DOOR_RATIO, doorColor);

            this.primaryShape = new Rectangle(center, width, height * DOOR_RATIO, primaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
            this.primaryShape.translateShape(new Vector(0, height * -1 * TRANSLATION_RATIO));

            this.secondaryShape = new Rectangle(center, width, height * DOOR_RATIO, secondaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
            this.secondaryShape.translateShape(new Vector(0, height * TRANSLATION_RATIO));

            this.primaryField = new DoorField(doorID+"", "P", center.add(new Point(0, -0.5f * height)), width, height, Color.GRAY);
            this.secondaryField = new DoorField(doorID+"", "S", center.add(new Point(0, 0.5f * height)), width, height, Color.LTGRAY);
        }

        this.side1LockFun = side1;
        this.side2LockFun = side2;
    }

    public void setTileBackground(int tileSize, int backgroundColor) {
        this.tileBackground = new Rectangle(getCenter(), tileSize, tileSize, backgroundColor);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        tileBackground.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        primaryShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        secondaryShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        doorShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
    }

    @Override
    public BoundingBox getBoundingBox() {
        return tileBackground.getBoundingBox();
    }

    @Override
    public void setColor(int color) {
        doorShape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        doorShape.revertToDefaultColor();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public Point[] getCollisionVertices() {
        return doorShape.getVertices();
    }

    public InteractionField getPrimaryField() {
        return primaryField;
    }

    public InteractionField getSecondaryField() {
        return secondaryField;
    }

    @Override
    public boolean isSolid() {
        return !open;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.RECTANGLE;
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.DOOR;
    }

    @Override
    public Point getCenter() {
        return center;
    }

    @Override
    public void setCenter(Point newCenter) {

    }

    public boolean isPrimaryLocked() {
        return primaryLocked;
    }

    public void setPrimaryLocked(boolean primaryLocked) {
        this.primaryLocked = primaryLocked;

        if (this.primaryLocked)
            primaryShape.setColor(LOCKED_COLOR);
        else
            primaryShape.setColor(UNLOCKED_COLOR);
    }

    public boolean isSecondaryLocked() {
        return secondaryLocked;
    }

    public void setSecondaryLocked(boolean secondaryLocked) {
        this.secondaryLocked = secondaryLocked;

        if (secondaryLocked)
            secondaryShape.setColor(LOCKED_COLOR);
        else
            secondaryShape.setColor(UNLOCKED_COLOR);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;

        if (open){
            doorShape.setColor(Color.TRANSPARENT);
            primaryShape.setColor(Color.TRANSPARENT);
            secondaryShape.setColor(Color.TRANSPARENT);
        } else {
            doorShape.revertToDefaultColor();
            primaryShape.setColor(primaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
            secondaryShape.setColor(secondaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
        }
    }

    /**
     * checks if the door if the fieldName provided is valid and that side is unlocked
     * @param fieldName P for primary side or S for secondary side
     */
    public boolean isSideUnlocked(String fieldName){
        if (fieldName.contains("P") && !primaryLocked)
            return true;
        return fieldName.contains("S") && !secondaryLocked;
    }

    public void close(){
        setOpen(false);
    }

    public int getDoorID(){
        return Integer.parseInt(getName());
    }
}