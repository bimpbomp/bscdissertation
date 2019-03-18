package bham.student.txm683.heartbreaker.entities;

import android.util.Log;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class MoveableEntity extends Entity {
    private Vector requestedMovementVector;
    private float maxSpeed;

    private Vector rotationVector;

    private Vector velocity;

    private Point spawn;

    private int maxDimension;

    public MoveableEntity(String name, Point spawn, int maxDimension, float maxSpeed){
        super(name);

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = Vector.ZERO_VECTOR;
        this.rotationVector = Vector.ZERO_VECTOR;

        this.velocity = Vector.ZERO_VECTOR;

        this.spawn = spawn;

        this.maxDimension = maxDimension;
    }

    public int getMaxDimension() {
        return maxDimension;
    }

    public Point getSpawn() {
        return spawn;
    }

    public Vector getRotationVector() {
        return rotationVector;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setRotationVector(Vector rotationVector) {
        this.rotationVector = rotationVector;
    }

    public abstract void tick(float secondsSinceLastGameTick);

    public Vector calculateMovementVector(float secondsSinceLastGameTick){
        return requestedMovementVector.equals(Vector.ZERO_VECTOR) ?
                Vector.ZERO_VECTOR : requestedMovementVector.sMult(secondsSinceLastGameTick * maxSpeed);
    }

    public void setRequestedMovementVector(Vector requestedMovementVector) {
        this.requestedMovementVector = requestedMovementVector;
    }

    public Vector getRequestedMovementVector() {
        return requestedMovementVector;
    }

    public void setVelocity(Vector v){
        this.velocity = v;
    }

    public Vector getVelocity(){
        return velocity;
    }

    protected void move(float secondsSinceLastGameTick, Shape shape, float rotationalDamping){
        if (velocity.getLength() < 5f)
            velocity = Vector.ZERO_VECTOR;

        if (!getRequestedMovementVector().equals(Vector.ZERO_VECTOR)) {
            Vector movementForce = getRequestedMovementVector().sMult(100);

            float dot = 0f;

            dot = velocity.getUnitVector().dot(movementForce.getUnitVector());

            float angle = (float) Math.acos(Math.min(dot, 1f));

            Log.d("MOVEMENT", "dot: " + dot + "angle: " + angle + " prop: " + (angle/ (float) Math.PI));
            Log.d("MOVEMENT:", "vel: " + velocity.relativeToString() + " move: " + movementForce.relativeToString());

            if (angle > 5)
                movementForce = movementForce.sMult(angle/ (float) Math.PI);

            Vector acc = movementForce;

            if (acc.getLength() > 300){
                acc = acc.setLength(300);
            }

            velocity = velocity.vAdd(acc);

            float max = getMaxSpeed();

            if (velocity.getLength() > max)
                velocity = velocity.setLength(max);

            Log.d("VELOCITY", "vel: " + velocity.relativeToString() + " sped: " + velocity.getLength() + " acc: " +
                    acc.relativeToString() + " f: " + movementForce.relativeToString() + " mV: " +
                    getRequestedMovementVector().relativeToString() + " dot: " + dot);


        } else {
            velocity = velocity.sMult(0.25f);
        }

        Vector v = velocity.sMult(secondsSinceLastGameTick);

        shape.translateShape(v);
    }

    public void rotate(float secondsSinceLastGameTick, Shape shape, float rotationalDamping){
        Vector force;
        if (!getRotationVector().equals(Vector.ZERO_VECTOR)){
            force = getRotationVector();

        } else
            force = getRequestedMovementVector();

        if (!force.equals(Vector.ZERO_VECTOR)) {
            float dot = Math.min(shape.getForwardUnitVector().det(force.getUnitVector()), 1f);
            float det = shape.getForwardUnitVector().det(force.getUnitVector());

            Vector momArm = new Vector(getCenter(), getCenter().add(shape.getForwardUnitVector().sMult(20f).getRelativeToTailPoint()));

            momArm.rotate(dot, det);

            Vector parCom = momArm.sMult(force.dot(momArm) / momArm.getLength());

            Vector angF = force.vSub(parCom).sMult(rotationalDamping);

            float angularAcc = momArm.det(angF);

            float angularVelocity = angularAcc * secondsSinceLastGameTick;

            shape.rotateBy(angularVelocity);
        }
    }

    @Override
    public boolean canMove() {
        return true;
    }
}