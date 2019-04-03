package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.util.Log;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class MoveableEntity extends Entity implements Renderable {
    private Vector requestedMovementVector;
    private float maxSpeed;

    private Vector rotationVector;

    private Vector velocity;

    private Point spawn;

    private Shape shape;

    private int maxDimension;

    private int mass;

    private List<Vector> extraForces;

    protected float maxAngularVelocity;
    protected float maxAcceleration;

    public MoveableEntity(String name, Point spawn, int maxDimension, float maxSpeed, int mass, Shape shape){
        super(name);

        this.shape = shape;
        this.mass = mass;

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = Vector.ZERO_VECTOR;
        this.rotationVector = Vector.ZERO_VECTOR;

        this.velocity = Vector.ZERO_VECTOR;

        this.spawn = spawn;

        this.maxDimension = maxDimension;

        this.extraForces = new ArrayList<>();
    }

    public void setMaxAngularVelocity(float maxAngularVelocity) {
        this.maxAngularVelocity = maxAngularVelocity;
    }

    public void setMaxAcceleration(float maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        shape.revertToDefaultColor();
    }

    public Shape getShape() {
        return shape;
    }

    public Vector getForwardUnitVector(){
        return shape.getForwardUnitVector();
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

    public void tick(float secondsSinceLastGameTick){
        applyMovementForces(secondsSinceLastGameTick);

        extraForces.clear();
    }

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
    public void setVelocity(Vector v){
        this.velocity = v;
    }

    @Override
    public Vector getVelocity(){
        return velocity;
    }

    public void addForce(Vector force){
        this.extraForces.add(force);
    }

    public void applyMovementForces(float secondsSinceLastGameTick){
        if (velocity.getLength() < 3f)
            velocity = Vector.ZERO_VECTOR;

        if (!getRequestedMovementVector().equals(Vector.ZERO_VECTOR) || this instanceof AIEntity) {
            Vector movementForce = Vector.ZERO_VECTOR;

            if (!(this instanceof AIEntity)) {
                movementForce = getRequestedMovementVector().sMult(100);


                float dot = velocity.getUnitVector().dot(movementForce.getUnitVector());

                float angle = (float) Math.acos(Math.min(dot, 1f));

                if (angle > 0.785f)
                    movementForce = movementForce.sMult(angle/ (float) Math.PI);

            }


            for (Vector force : extraForces){
                Log.d("MOVEMENT", getName() + " extra force: " + force.relativeToString());
                movementForce = movementForce.vAdd(force);
            }

            if (movementForce.equals(Vector.ZERO_VECTOR)) {
                velocity = velocity.sMult(0.75f);
            } else {

                Log.d("MOVEMENT:", getName() + ": vel: " + velocity.relativeToString() + " applyMovementForces: " + movementForce.relativeToString());

                Vector acc = movementForce.sMult(1f/mass);

                int maxAcc = 300;
                if (acc.getLength() > maxAcc) {
                    acc = acc.setLength(maxAcc);
                }

                velocity = velocity.vAdd(acc);

                float max = getMaxSpeed();

                if (velocity.getLength() > max)
                    velocity = velocity.setLength(max);

                Log.d("VELOCITY", "vel: " + velocity.relativeToString() + " sped: " + velocity.getLength() + " acc: " +
                        acc.relativeToString() + " f: " + movementForce.relativeToString());
            }

        } else {
            velocity = velocity.sMult(0.25f);
        }

        Vector v = velocity.sMult(secondsSinceLastGameTick);

        shape.translate(v);

        //rotate body
        float angularVelocity = getAngularVelocity(v.getUnitVector(), secondsSinceLastGameTick, getShape().getForwardUnitVector());

        float maxAngularVel = 0.5f;

        if (angularVelocity > maxAngularVel)
            angularVelocity = maxAngularVel;

        getShape().rotate(angularVelocity);
    }

    public float getAngularVelocity(Vector force, float secondsSinceLastGameTick, Vector forwardUnitVector){
        Vector momArm = new Vector(getCenter(), getCenter().add(forwardUnitVector.sMult(20f).getRelativeToTailPoint()));

        Vector parCom = momArm.sMult(force.dot(momArm) / momArm.getLength());

        Vector angF = force.vSub(parCom);

        float angularAcc = momArm.det(angF);

        return angularAcc * secondsSinceLastGameTick;
    }

    public int getMass() {
        return mass;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translate(newCenter);
    }


}