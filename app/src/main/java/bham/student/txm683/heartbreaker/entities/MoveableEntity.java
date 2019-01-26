package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.EntityShape;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class MoveableEntity extends Entity {
    private Vector inputtedMovementVector;
    private float maxSpeed;
    private float maxAcc;
    private Point oldCenter;

    public MoveableEntity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, int colorValue, float maxSpeed) {
        super(name, spawnCoordinates, shapeIdentifier, width, height, colorValue);
        this.maxSpeed = maxSpeed;

        this.maxAcc = maxSpeed / 3f;
        this.oldCenter = spawnCoordinates;

        this.inputtedMovementVector = new Vector();

        moveable = true;
    }

    public MoveableEntity(String name, EntityShape shape, float maxSpeed) {
        super(name, shape);
        this.maxSpeed = maxSpeed;
        this.inputtedMovementVector = new Vector();

        this.maxAcc = maxSpeed / 3f;
        this.oldCenter = shape.getCenter();

        moveable = true;
    }

    public MoveableEntity(String stateString) throws ParseException, JSONException {
        super(stateString);
        JSONObject jsonObject = new JSONObject(stateString);

        this.maxSpeed = Float.parseFloat(jsonObject.getString("maxspeed"));
        this.inputtedMovementVector = new Vector();

        this.maxAcc = maxSpeed / 3f;
        this.oldCenter = shape.getCenter();

        moveable = true;
    }

    /**
     * Updates the shape position and rotation based on time passed and current movement vector.
     * @param secondsSinceLastGameTick time since last game tick, in seconds
     */
    public void move(float secondsSinceLastGameTick){
        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);
        //Log.d(TAG, "movement vector: " + movementVector.relativeToString());

        if (!movementVector.equals(new Vector())) {
            //moves and translates the shape by the calculated amounts
            shape.move(movementVector);
        }
    }


    /**
     * Draws the entity to the given canvas
     * @param canvas Canvas to draw to.
     * @param secondsSinceLastGameTick time since last game tick in seconds for interpolation of position
     * @param renderOffset Point to offset vertices by for rendering on scrolling screen. Provided as negative.
     */
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastGameTick, boolean renderName){

        if (collided){
            shape.setColor(Color.GREEN);
            collided = false;
        } else {
            shape.resetToDefaultColor();
        }

        Vector interpolationVector = calculateMovementVector(secondsSinceLastGameTick);

        shape.draw(canvas, renderOffset, interpolationVector);

        //Log.d(TAG, name + " push: " + pushVector.toString());
        Vector tpushVector = pushVector.translate(shape.getCenter());
        tpushVector = new Vector(tpushVector.getTail().add(renderOffset), tpushVector.getHead().add(renderOffset));
        canvas.drawLine(tpushVector.getTail().getX(),
                tpushVector.getTail().getY(),
                tpushVector.getHead().getX(),
                tpushVector.getHead().getY(),
                textPaint);

        if (renderName) {
            Point interpolatedCenter = shape.getInterpolatedCenter(interpolationVector, renderOffset);
            RenderingTools.renderCenteredText(canvas, textPaint, name, interpolatedCenter);
        }
    }

    private Vector calculateMovementVector(float secondsSinceLastGameTick){
        /*if (Float.compare(secondsSinceLastGameTick, 0f) != 0) {
            if (!inputtedMovementVector.equals(new Vector())) {
                Log.d(TAG, shape.getCenter().toString());
                Vector velocity = new Vector(oldCenter, shape.getCenter());
                Vector accVector = inputtedMovementVector.sMult(50 * secondsSinceLastGameTick);
                oldCenter = shape.getCenter();


                return velocity.vAdd(accVector);
            }
        }
        return new Vector();*/

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