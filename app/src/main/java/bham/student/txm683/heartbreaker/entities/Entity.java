package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.EntityShape;
import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTriangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Entity {
    String TAG;

    private String name;
    private EntityShape shape;
    private Paint textPaint;

    private Vector movementUnitVector;
    private float movementMaxSpeed;

    private boolean collided;

    public Entity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, float movementMaxSpeed, int colorValue){
        this.name = name;
        this.movementMaxSpeed = movementMaxSpeed;

        switch (shapeIdentifier){

            case ISO_TRIANGLE:
                this.shape = new IsoscelesTriangle(spawnCoordinates, width, height, colorValue);
                break;
            case EQU_TRIANGLE:
                break;
            case SQUARE:
                break;
            case RECT:
                break;
        }

        initTextPaint();
    }

    public Entity(String name, float movementMaxSpeed, EntityShape shape){
        this.name = name;
        this.movementMaxSpeed = movementMaxSpeed;
        this.shape = shape;

        initTextPaint();
    }

    private void initTextPaint(){

        /*int colorToInvert = shape.getDefaultColor();
        //credit: https://stackoverflow.com/a/27487587/3478664
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(colorToInvert), Color.green(colorToInvert),
                Color.blue(colorToInvert), hsv);
        hsv[0] = (hsv[0] + 180) % 360;
        //end of credit*/

        textPaint = new Paint();
        //textPaint.setColor(Color.HSVToColor(hsv));
        textPaint.setColor(Color.GRAY);
        textPaint.setStrokeWidth(15f);
        textPaint.setTextSize(28f);
    }

    /**
    * Draws the entity to the given canvas
    * @param canvas Canvas to draw to.
    * @param secondsSinceLastGameTick time since last game tick in seconds for interpolation of position
    */
    public void draw(Canvas canvas, float secondsSinceLastGameTick){

        if (collided){
            shape.setColor(Color.RED);
            collided = false;
        } else {
            shape.resetToDefaultColor();
        }

        Vector interpolationVector = getMovementVector(secondsSinceLastGameTick);

        shape.draw(canvas, interpolationVector);

        Point interpolatedCenter = shape.getInterpolatedCenter(interpolationVector);
        canvas.drawText(name, interpolatedCenter.getX(), interpolatedCenter.getY(), textPaint);
    }

    /**
    * Updates the position based on time passed and current movement unit vector.
    * @param secondsSinceLastGameTick time since last game tick, in seconds
    */
    public void move(float secondsSinceLastGameTick){
        shape.setCenter(getNextPosition(secondsSinceLastGameTick));
    }

    /**
    * Returns the new position for this entity based on time passed since last update.
    * This method does NOT update the position with the new value, only returns it.
    * @param secondsSinceLastGameTick time passed since last game tick in seconds
    * @return New position = oldPosition + (maxSpeed*time)*movementUnitVector
    */
    private Point getNextPosition(float secondsSinceLastGameTick){
        if (Float.compare(secondsSinceLastGameTick, 0f) == 0){
            return shape.getCenter();
        }
        return shape.getCenter().addVector(getMovementVector(secondsSinceLastGameTick));
    }

    private Vector getMovementVector(float secondsSinceLastGameTick){
        return Vector.sMult(movementUnitVector, secondsSinceLastGameTick*movementMaxSpeed);
    }

    public Point getCurrentPosition(){
        return this.shape.getCenter();
    }

    public String getName(){
        return this.name;
    }

    public void setMovementUnitVector(Vector movementUnitVector){
        this.movementUnitVector = movementUnitVector;
    }

    public void setMovementMaxSpeed(float movementMaxSpeed){
        this.movementMaxSpeed = movementMaxSpeed;
    }

    /**
    * Generates a String in JSON for saving state
    * @return String in JSON format
    */
    public String getSaveString(){
        //TODO implement state saving to file
        return "";
    }

    public boolean isCollided() {
        return collided;
    }

    public void setCollided(boolean collided) {
        this.collided = collided;
    }

    public EntityShape getShape(){
        return this.shape;
    }
}