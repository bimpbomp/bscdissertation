package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.InteractionField;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Door extends Entity implements Renderable, Collidable {

    private Rectangle tileBackground;

    private Rectangle doorShape;

    private boolean primaryLocked;
    private boolean secondaryLocked;

    private boolean open;

    private InteractionField primaryField;
    private InteractionField secondaryField;

    private static final int LOCKED_COLOR = Color.RED;
    private static final int UNLOCKED_COLOR = Color.GREEN;

    public Door(int doorID, Point center, int width, int height, float fieldWidth, boolean primaryLocked,
                boolean secondaryLocked, boolean vertical, int doorColor){

        super(doorID+"");

        this.doorShape = new Rectangle(center, width, height, doorColor);

        this.primaryLocked = primaryLocked;
        this.secondaryLocked = secondaryLocked;

        this.open = false;

        Point[] doorCorners = doorShape.getVertices();

        Point fieldTopLeft;
        Point fieldBottomRight;

        Point fieldCenter;
        if (vertical) {
            //door is vertical so create fields on left and right of it

            //init the topleft and bottomright corners of the left door field
            fieldTopLeft = doorCorners[0].add(new Point(-1*fieldWidth, 0));
            fieldBottomRight = doorCorners[3];

            //center is halfway between topleft and bottom right points
            fieldCenter = new Point(fieldBottomRight.getX()-fieldTopLeft.getX(),
                    fieldBottomRight.getY()-fieldTopLeft.getY()).smult(0.5f);


            this.primaryField = new InteractionField(fieldCenter, new Vector[]{
                    new Vector(fieldCenter, fieldTopLeft),
                    new Vector(fieldCenter, doorCorners[0]),
                    new Vector(fieldCenter, fieldBottomRight),
                    new Vector(fieldCenter, fieldBottomRight.add(new Point(-1* fieldWidth, 0)))
            }, primaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);


            //init topleft and bottom right of the right side door field
            fieldTopLeft = doorCorners[1];
            fieldBottomRight = doorCorners[2].add(new Point(fieldWidth, 0));

            fieldCenter = new Point(fieldBottomRight.getX()-fieldTopLeft.getX(),
                    fieldBottomRight.getY()-fieldTopLeft.getY()).smult(0.5f);

            this.secondaryField = new InteractionField(fieldCenter, new Vector[]{
                    new Vector(fieldCenter, fieldTopLeft),
                    new Vector(fieldCenter, fieldTopLeft.add(new Point(fieldWidth, 0))),
                    new Vector(fieldCenter, fieldBottomRight),
                    new Vector(fieldCenter, doorCorners[2])
            }, secondaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);

        } else {
            //door is horizontal so create fields above and below it

            //init the topleft and bottomright corners of the top door field
            fieldTopLeft = doorCorners[0].add(new Point(0, -1*fieldWidth));
            fieldBottomRight = doorCorners[1];

            //center is halfway between topleft and bottom right points
            fieldCenter = new Point(fieldBottomRight.getX()-fieldTopLeft.getX(),
                    fieldBottomRight.getY()-fieldTopLeft.getY()).smult(0.5f);


            this.primaryField = new InteractionField(fieldCenter, new Vector[]{
                    new Vector(fieldCenter, fieldTopLeft),
                    new Vector(fieldCenter, doorCorners[1].add(new Point(0, -1*fieldWidth))),
                    new Vector(fieldCenter, fieldBottomRight),
                    new Vector(fieldCenter, doorCorners[0])
            }, primaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);


            //init topleft and bottom right of the bottom door field
            fieldTopLeft = doorCorners[3];
            fieldBottomRight = doorCorners[2].add(new Point(0, fieldWidth));

            fieldCenter = new Point(fieldBottomRight.getX()-fieldTopLeft.getX(),
                    fieldBottomRight.getY()-fieldTopLeft.getY()).smult(0.5f);

            this.secondaryField = new InteractionField(fieldCenter, new Vector[]{
                    new Vector(fieldCenter, fieldTopLeft),
                    new Vector(fieldCenter, doorCorners[2]),
                    new Vector(fieldCenter, fieldBottomRight),
                    new Vector(fieldCenter, doorCorners[3].add(new Point(0, fieldWidth)))
            }, secondaryLocked ? LOCKED_COLOR : UNLOCKED_COLOR);
        }
    }

    public void setTileBackground(int tileSize, int backgroundColor) {
        this.tileBackground = new Rectangle(getCenter(), tileSize, tileSize, backgroundColor);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        tileBackground.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        primaryField.draw(canvas, renderOffset, interpolationVector, renderEntityName);
        secondaryField.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        doorShape.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
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

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.RECTANGLE;
    }

    @Override
    public Point getCenter() {
        return doorShape.getCenter();
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
            primaryField.setColor(LOCKED_COLOR);
        else
            primaryField.setColor(UNLOCKED_COLOR);
    }

    public boolean isSecondaryLocked() {
        return secondaryLocked;
    }

    public void setSecondaryLocked(boolean secondaryLocked) {
        this.secondaryLocked = secondaryLocked;

        if (secondaryLocked)
            secondaryField.setColor(LOCKED_COLOR);
        else
            secondaryField.setColor(UNLOCKED_COLOR);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;

        if (open){
            doorShape.setColor(Color.TRANSPARENT);
        } else {
            doorShape.revertToDefaultColor();
        }
    }

    public int getDoorID(){
        return Integer.parseInt(getName());
    }
}