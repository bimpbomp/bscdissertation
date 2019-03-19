package bham.student.txm683.heartbreaker.entities;

import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class MoveableEntity extends Entity {
    private Vector requestedMovementVector;
    private float maxSpeed;

    private Vector rotationVector;

    private Vector velocity;

    private Point spawn;

    private int maxDimension;

    private List<Vector> extraForces;

    public MoveableEntity(String name, Point spawn, int maxDimension, float maxSpeed){
        super(name);

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = Vector.ZERO_VECTOR;
        this.rotationVector = Vector.ZERO_VECTOR;

        this.velocity = Vector.ZERO_VECTOR;

        this.spawn = spawn;

        this.maxDimension = maxDimension;

        this.extraForces = new ArrayList<>();
    }

    public abstract Vector getForwardUnitVector();

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

    public void addForce(Vector force){
        this.extraForces.add(force);
    }

    protected void move(float secondsSinceLastGameTick, Shape shape, float rotationalDamping){
        if (velocity.getLength() < 5f)
            velocity = Vector.ZERO_VECTOR;

        if (!getRequestedMovementVector().equals(Vector.ZERO_VECTOR) || this instanceof AIEntity) {
            Vector movementForce = Vector.ZERO_VECTOR;

            if (!(this instanceof AIEntity))
                movementForce = getRequestedMovementVector().sMult(200);

            float dot = velocity.getUnitVector().dot(movementForce.getUnitVector());

            float angle = (float) Math.acos(Math.min(dot, 1f));

            Log.d("MOVEMENT", getName() + ": dot: " + dot + " angle: " + angle + " prop: " + (angle/ (float) Math.PI));
            Log.d("MOVEMENT:", getName() + ": vel: " + velocity.relativeToString() + " move: " + movementForce.relativeToString());

            if (angle > 0.175f)
                movementForce = movementForce.sMult(angle/ (float) Math.PI);

            for (Vector force : extraForces){
                Log.d("MOVEMENT", getName() + " extra force: " + force.relativeToString());
                movementForce = movementForce.vAdd(force);
            }
            extraForces.clear();

            Vector acc = movementForce;

            int maxAcc = 300;
            if (acc.getLength() > maxAcc){
                acc = acc.setLength(maxAcc);
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