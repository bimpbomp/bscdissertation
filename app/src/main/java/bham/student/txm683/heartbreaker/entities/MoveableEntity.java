package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.EntityShape;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class MoveableEntity extends Entity {
    private Vector inputtedMovementVector;
    private float maxSpeed;

    public MoveableEntity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, int colorValue, float maxSpeed) {
        super(name, spawnCoordinates, shapeIdentifier, width, height, colorValue);
        this.maxSpeed = maxSpeed;
        this.inputtedMovementVector = new Vector();

        moveable = true;
    }

    public MoveableEntity(String name, EntityShape shape, float maxSpeed) {
        super(name, shape);
        this.maxSpeed = maxSpeed;
        this.inputtedMovementVector = new Vector();

        moveable = true;
    }

    public MoveableEntity(String stateString) throws ParseException, JSONException {
        super(stateString);
        JSONObject jsonObject = new JSONObject(stateString);

        this.maxSpeed = Float.parseFloat(jsonObject.getString("maxspeed"));
        this.inputtedMovementVector = new Vector();

        moveable = true;
    }

    /**
     * Updates the shape position and rotation based on time passed and current movement vector.
     * @param secondsSinceLastGameTick time since last game tick, in seconds
     */
    public void move(float secondsSinceLastGameTick){
        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        if (!movementVector.equals(new Vector())) {
            shape.move(movementVector);
        }
    }


    /**
     * Draws the entity to the given canvas
     * @param canvas Canvas to draw to.
     * @param secondsSinceLastGameTick time since last game tick in seconds for interpolation of position
     * @param renderOffset Point to offset vertices by for rendering on scrolling screen. Provided as negative.
     */
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastGameTick){

        if (collided){
            shape.setColor(Color.RED);
            collided = false;
        } else {
            shape.resetToDefaultColor();
        }

        Vector interpolationVector = calculateMovementVector(secondsSinceLastGameTick);

        shape.draw(canvas, renderOffset, interpolationVector);

        Point interpolatedCenter = shape.getInterpolatedCenter(interpolationVector, renderOffset);
        canvas.drawText(name, interpolatedCenter.getX()-shape.getWidth()/2, interpolatedCenter.getY(), textPaint);
    }

    /**
     * Returns the new position for this entity based on time passed since last update.
     * This method does NOT update the position with the new value, only returns it.
     * @param secondsSinceLastGameTick time passed since last game tick in seconds
     * @return New position = oldPosition + (maxSpeed*time)*inputtedMovementVector
     */
    private Point getNextPosition(float secondsSinceLastGameTick){
        if (Float.compare(secondsSinceLastGameTick, 0f) == 0){
            return shape.getCenter();
        }
        return shape.getCenter().addVector(calculateMovementVector(secondsSinceLastGameTick));
    }

    /**
     * Calculates the movement vector by multipling the inputtedVector by maxSpeed
     * @param secondsSinceLastGameTick seconds since last game tick
     * @return complete movement vector
     */
    private Vector calculateMovementVector(float secondsSinceLastGameTick){
        return inputtedMovementVector != null ? inputtedMovementVector.sMult(secondsSinceLastGameTick * maxSpeed) : new Vector();
    }

    public void setMovementVector(Vector movementVector){
        this.inputtedMovementVector = movementVector;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = super.getStateObject();
        jsonObject.put("maxspeed", maxSpeed);
        return jsonObject;
    }
}
