package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class MoveableEntity extends Entity {
    private Vector requestedMovementVector;
    private float maxSpeed;

    private Vector rotationVector;

    private Vector velocity;

    public MoveableEntity(String name, float maxSpeed){
        super(name);

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = Vector.ZERO_VECTOR;
        this.rotationVector = Vector.ZERO_VECTOR;

        this.velocity = Vector.ZERO_VECTOR;
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

    }

    public Vector getVelocity(){
        return new Vector(1,1);
    }

    public void move(float secondsSinceLastGameTick, Shape shape){
        if (!getRequestedMovementVector().equals(Vector.ZERO_VECTOR)) {
            Vector movementForce = getRequestedMovementVector().sMult(4);

            float dot = movementForce.dot(shape.getForwardUnitVector());

            movementForce = movementForce.sMult((float) Math.pow(dot, 2));

            Vector acc = movementForce;

            velocity = velocity.vAdd(acc);

            float max = getMaxSpeed();

            if (velocity.getLength() > max)
                velocity = velocity.setLength(max);

            /*Log.d("VELOCITY", "vel: " + velocity.relativeToString() + " sped: " + velocity.getLength() + " acc: " +
                    acc.relativeToString() + " f: " + movementForce.relativeToString() + " mV: " +
                    getRequestedMovementVector().relativeToString() + " dot: " + dot);*/

            shape.translateShape(velocity.sMult(secondsSinceLastGameTick));

        } else {
            velocity = velocity.sMult(0.25f);

            shape.translateShape(velocity.sMult(secondsSinceLastGameTick));
        }

        Vector force;
        if (!getRotationVector().equals(Vector.ZERO_VECTOR)){
            force = getRotationVector();

        } else
            force = getRequestedMovementVector();

        if (!force.equals(Vector.ZERO_VECTOR)) {
            Vector momArm = new Vector(getCenter(), getCenter().add(shape.getForwardUnitVector().sMult(10f).getRelativeToTailPoint()));
            Vector parCom = momArm.sMult(force.dot(momArm) / momArm.getLength());

            Vector angF = force.vSub(parCom);

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