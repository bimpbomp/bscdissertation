package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.SaveableState;
import bham.student.txm683.heartbreaker.entities.entityshapes.EntityShape;
import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTriangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class Entity implements SaveableState {
    String TAG;

    private String name;
    private EntityShape shape;
    private Paint textPaint;

    private Vector inputtedMovementVector;
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

    public Entity(String stateString) throws ParseException, JSONException {
        JSONObject jsonObject = new JSONObject(stateString);

        this.name = jsonObject.getString("shape");
        this.movementMaxSpeed = Float.parseFloat(jsonObject.getString("maxspeed"));

        switch (ShapeIdentifier.fromInt(jsonObject.getInt("shapeidentifier"))){
            case ISO_TRIANGLE:
                this.shape = new IsoscelesTriangle(stateString);
                break;
            case RECT:
                this.shape = new Rectangle(stateString);
                break;
            default:
                throw new ParseException("Invalid shape identifier",0);
        }
    }

    private void initTextPaint(){
        textPaint = new Paint();
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

        Vector interpolationVector = calculateMovementVector(secondsSinceLastGameTick);

        shape.draw(canvas, interpolationVector);

        Point interpolatedCenter = shape.getInterpolatedCenter(interpolationVector);
        canvas.drawText(name, interpolatedCenter.getX(), interpolatedCenter.getY(), textPaint);
    }

    /**
    * Updates the shape position and rotation based on time passed and current movement vector.
    * @param secondsSinceLastGameTick time since last game tick, in seconds
    */
    public void move(float secondsSinceLastGameTick){
        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        if (!movementVector.equals(new Vector()))
            shape.move(movementVector);
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
        //Log.d(TAG, "calculate movement vector: " + inputtedMovementVector.sMult(secondsSinceLastGameTick*movementMaxSpeed));
        return inputtedMovementVector != null ? inputtedMovementVector.sMult(secondsSinceLastGameTick*movementMaxSpeed) : new Vector();
    }

    public Point getCurrentPosition(){
        return this.shape.getCenter();
    }

    public String getName(){
        return this.name;
    }

    public void setMovementVector(Vector movementVector){
        //Log.d(TAG, "set movement vector: "+movementVector.toString());
        this.inputtedMovementVector = movementVector;
    }

    public void setMovementMaxSpeed(float movementMaxSpeed){
        this.movementMaxSpeed = movementMaxSpeed;
    }

    /**
    * Generates a String in JSON for saving state
    * @return String in JSON format
    */
    @Override
    public String getStateString() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", name);
        jsonObject.put("shape", shape.getStateString());
        jsonObject.put("maxspeed", movementMaxSpeed);
        jsonObject.put("shapeidentifier", shape.getShapeIdentifier().getId());

        return jsonObject.toString();
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