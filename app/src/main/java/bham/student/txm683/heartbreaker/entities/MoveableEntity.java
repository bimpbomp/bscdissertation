package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
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

    private static final int TIME_BETWEEN_CHARGES = 25;

    private int maxCharge;
    private int currentMeleeCharge;
    private long lastChargeTime;
    private long currentChargeTime;

    private int attackCooldown;

    private int damageDealt;
    private int health;

    public MoveableEntity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, int colorValue, float maxSpeed, int maxCharge) {
        super(name, spawnCoordinates, shapeIdentifier, width, height, colorValue);

        init(maxSpeed, maxCharge);
    }

    public MoveableEntity(String name, EntityShape shape, float maxSpeed, int maxCharge) {
        super(name, shape);

        init(maxSpeed, maxCharge);
    }

    public MoveableEntity(String stateString) throws ParseException, JSONException {
        super(stateString);
        JSONObject jsonObject = new JSONObject(stateString);

        init(Float.parseFloat(jsonObject.getString("maxspeed")),
                Integer.parseInt(jsonObject.getString("maxcharge")));
    }

    private void init(float maxSpeed, int maxCharge){
        this.maxCharge = maxCharge;

        this.maxSpeed = maxSpeed;

        this.inputtedMovementVector = new Vector();

        moveable = true;

        health = 5;

        damageDealt = 0;

        attackCooldown = 0;

        resetMeleeCharges();
    }

    private void resetMeleeCharges(){
        this.currentMeleeCharge = 0;

        this.lastChargeTime = 0;
        this.currentChargeTime = 0;
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
     * Used to deal damage to this entity
     * @param damageDealt damage to deal
     * @return True if entity died
     */
    public boolean damage(int damageDealt){
        boolean died = false;

        health -= damageDealt;
        Log.d("hb::I", name + " health now at " + health);

        if (health <= 0)
            died = true;

        return died;
    }

    public void chargeMelee(){
        currentChargeTime = System.currentTimeMillis();

        if (currentMeleeCharge < maxCharge && attackCooldown == 0) {
            if (currentChargeTime - lastChargeTime > TIME_BETWEEN_CHARGES) {
                currentMeleeCharge += 1;
                lastChargeTime = currentChargeTime;
                Log.d(TAG, "adding charge, charge now at: " + currentMeleeCharge);

                if (currentMeleeCharge == 1 || currentMeleeCharge == maxCharge){
                    shape.contractWidth(1.1f);
                }
            } else {
                Log.d(TAG, "not enough time passed for charge (" + (currentChargeTime - lastChargeTime) + ")");
            }
        } else {
            Log.d(TAG, "already at max charge");
        }
    }

    /**
     * Called when the melee button is released.
     */
    public void meleeAttack(){
        Log.d(TAG, "melee attack with charge of " + currentMeleeCharge);
        shape.returnToNormal();
        shape.contractWidth(0.8f);
        shape.contractHeight(1.4f);

        if (currentMeleeCharge > 0 && currentMeleeCharge <= maxCharge / 2){
            damageDealt = 1;
        } else if (currentMeleeCharge > 0){
            damageDealt = 3;
        }
        resetMeleeCharges();

        attackCooldown = 5;
        launchedAttack = true;
    }

    /**
     * Called by collision manager at end of checks to mark collision as checked
     */
    public void resetAttack(){
        launchedAttack = false;
        damageDealt = 0;

    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public void tickCooldown(){
        attackCooldown -= 1;

        if (attackCooldown <= 0){
            attackCooldown = 0;
            shape.returnToNormal();
        }
    }

    public int getDamageFromMeleeAttack(){
        return damageDealt;
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
        jsonObject.put("maxCharge", maxCharge);
        return jsonObject;
    }
}