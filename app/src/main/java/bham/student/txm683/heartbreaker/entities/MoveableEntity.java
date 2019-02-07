package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class MoveableEntity extends Entity {
    private Vector requestedMovementVector;
    private float maxSpeed;

    public MoveableEntity(String name, Point position, float maxSpeed){
        super(name, position);

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = new Vector();
    }

    public abstract void move(float secondsSinceLastGameTick);

    public Vector calculateMovementVector(float secondsSinceLastGameTick){
        return requestedMovementVector.equals(new Vector()) ?
                new Vector() : requestedMovementVector.sMult(secondsSinceLastGameTick * maxSpeed);
    }

    public void setRequestedMovementVector(Vector requestedMovementVector) {
        this.requestedMovementVector = requestedMovementVector;
    }

    @Override
    public boolean canMove() {
        return true;
    }
}

/*private Vector requestedMovementVector;
    private float maxSpeed;

    private int roomID;
    private int health;

    public MoveableEntity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, int colorValue, float maxSpeed) {
        super(name, spawnCoordinates, shapeIdentifier, width, height, colorValue);

        init(maxSpeed);
    }

    public MoveableEntity(String name, Polygon shape, float maxSpeed) {
        super(name, shape);

        init(maxSpeed);
    }

    public MoveableEntity(String stateString) throws ParseException, JSONException {
        super(stateString);
        JSONObject jsonObject = new JSONObject(stateString);

        init(Float.parseFloat(jsonObject.getString("maxspeed")));
    }

    private void init(float maxSpeed){


        this.maxSpeed = maxSpeed;

        this.requestedMovementVector = new Vector();

        moveable = true;

        health = 5;
    }

    public void move(float secondsSinceLastGameTick){
        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);
        //Log.d(TAG, "movement vector: " + movementVector.relativeToString());

        if (!movementVector.equals(new Vector())) {
            //moves and translates the shape by the calculated amounts
            shape.move(movementVector);
        }
    }

    public boolean damage(int damageDealt){
        boolean died = false;

        health -= damageDealt;
        Log.d("hb::I", name + " health now at " + health);

        if (health <= 0)
            died = true;

        return died;
    }

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
        *//*if (Float.compare(secondsSinceLastGameTick, 0f) != 0) {
            if (!requestedMovementVector.equals(new Vector())) {
                Log.d(TAG, shape.getCenter().toString());
                Vector velocity = new Vector(oldCenter, shape.getCenter());
                Vector accVector = requestedMovementVector.sMult(50 * secondsSinceLastGameTick);
                oldCenter = shape.getCenter();


                return velocity.vAdd(accVector);
            }
        }
        return new Vector();*//*

        return requestedMovementVector != null ? requestedMovementVector.sMult(secondsSinceLastGameTick * maxSpeed) : new Vector();
    }

    public void setMovementVector(Vector movementVector){
        this.requestedMovementVector = movementVector;
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

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }*/