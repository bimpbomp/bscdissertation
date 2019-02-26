package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class MoveableEntity extends Entity {
    private Vector requestedMovementVector;
    private float maxSpeed;

    private Vector rotationVector;

    public MoveableEntity(String name, float maxSpeed){
        super(name);

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = Vector.ZERO_VECTOR;
        this.rotationVector = Vector.ZERO_VECTOR;
    }

    public Vector getRotationVector() {
        return rotationVector;
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

    @Override
    public boolean canMove() {
        return true;
    }
}