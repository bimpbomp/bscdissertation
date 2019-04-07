package bham.student.txm683.framework.entities;

import android.graphics.Canvas;
import bham.student.txm683.framework.ai.IAIEntity;
import bham.student.txm683.framework.entities.entityshapes.Shape;
import bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class MoveableEntity extends Entity implements Renderable {
    private Vector requestedMovementVector;
    private float maxSpeed;

    private Vector rotationVector;

    private Vector velocity;

    private Point spawn;

    private Shape shape;

    private int width;

    private int mass;

    private int mesh;

    private List<Vector> extraForces;

    protected float maxAngularVelocity;
    protected float maxAcceleration;

    public MoveableEntity(String name, Point spawn, int width, float maxSpeed, int mass, Shape shape){
        super(name);

        this.shape = shape;
        this.mass = mass;

        this.maxSpeed = maxSpeed;
        this.requestedMovementVector = Vector.ZERO_VECTOR;
        this.rotationVector = Vector.ZERO_VECTOR;

        this.velocity = Vector.ZERO_VECTOR;

        this.spawn = spawn;

        this.width = width;

        this.extraForces = new ArrayList<>();

        this.maxAngularVelocity = 0.5f;
        this.maxAcceleration = 300f;

        this.mesh = -1;
    }

    public int getMesh() {
        return mesh;
    }

    public void setMesh(int mesh) {
        this.mesh = mesh;
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

    public int getWidth() {
        return width;
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

    public void setVelocity(Vector v){
        this.velocity = v;
    }

    public Vector getVelocity(){
        return velocity;
    }

    public void addForce(Vector force){
        this.extraForces.add(force);
    }

    public void applyMovementForces(float secondsSinceLastGameTick){
        //if the speed drops below a value of 3, then set it to zero.
        //A speed of 3 in this context means 3 pixels per second which is basically not moving.
        if (velocity.getLength() < 3f)
            velocity = Vector.ZERO_VECTOR;

        //the requested moement vector is only used for the player, but this segment should also execute
        //if the entitiy is an AI
        if (!getRequestedMovementVector().equals(Vector.ZERO_VECTOR) || this instanceof IAIEntity) {
            Vector movementForce = Vector.ZERO_VECTOR;

            //if this is not an ai entity, then apply the requested movement vector force scaled up an amount
            if (!(this instanceof IAIEntity)) {
                movementForce = getRequestedMovementVector().sMult(100);

            }
            //add any external forces applied on this entity to the movement force
            for (Vector force : extraForces){
                movementForce = movementForce.vAdd(force);
            }

            if (movementForce.equals(Vector.ZERO_VECTOR)) {
                //if no force is being applied, decay the existing velocity
                velocity = velocity.sMult(0.25f);
            } else {

                //newtons second law F = ma or in this case rearranged to a= F/m
                Vector acc = movementForce.sMult(1f/mass);

                //truncate the acceleration
                if (acc.getLength() > maxAcceleration) {
                    acc = acc.setLength(maxAcceleration);
                }

                //add acceleration to velocity
                velocity = velocity.vAdd(acc);

                float max = getMaxSpeed();

                //truncate velocity
                if (velocity.getLength() > max)
                    velocity = velocity.setLength(max);
            }

        } else {
            //no inputted movement vector so decay speed
            velocity = velocity.sMult(0.25f);
        }

        //scale the velocity based on the time passed since the last tick
        Vector v = velocity.sMult(secondsSinceLastGameTick);

        //translate the shape
        shape.translate(v);

        //rotate body in line with movement

        float angularVelocity = getAngularVelocity(v.getUnitVector(), secondsSinceLastGameTick, getShape().getForwardUnitVector());

        //truncate angular velocity
        if (angularVelocity > maxAngularVelocity)
            angularVelocity = maxAngularVelocity;

        //rotate the shape
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