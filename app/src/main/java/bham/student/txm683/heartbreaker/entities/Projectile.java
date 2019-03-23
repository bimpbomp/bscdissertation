package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ICircle;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Projectile extends MoveableEntity implements Renderable, ICircle {
    private Point currentCenter;
    private Point nextTickCenter;

    private int damage;
    private int lifeInTicks;

    private String owner;

    public Projectile(String name, String owner, Point center, float radius, float maxSpeed, int damage, int lifeInTicks, int color){
        super(name, center, (int) radius*2, maxSpeed, 1, new Circle(center, radius, color));

        this.owner = owner;

        this.currentCenter = center;
        this.nextTickCenter = center;

        this.damage = damage;

        this.lifeInTicks = lifeInTicks;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public Circle getCircle() {
        return (Circle) getShape();
    }

    public float getRadius() {
        return ((Circle) getShape()).getRadius();
    }

    @Override
    public void setColor(int color) {
        getShape().setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        getShape().revertToDefaultColor();
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        currentCenter = nextTickCenter;

        nextTickCenter = currentCenter.add(movementVector.getRelativeToTailPoint());

        getShape().translate(currentCenter);

        lifeInTicks--;
    }

    @Override
    public Vector getForwardUnitVector() {
        return Vector.ZERO_VECTOR;
    }

    private Point interpolateCenter(float secondsSinceLastGameTick){
        return new Vector(currentCenter, nextTickCenter).sMult(secondsSinceLastGameTick).getHead();
    }

    public boolean outOfLife(){
        return lifeInTicks < 1;
    }

    int getLifeLeft(){
        return lifeInTicks;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName){

        Point offsetCenter = interpolateCenter(secondsSinceLastRender);

        getShape().translate(offsetCenter);

        getShape().draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public BoundingBox getBoundingBox() {
        float radius = ((Circle) getShape()).getRadius();
        return new BoundingBox(currentCenter.add(-1 * radius, -1 * radius), currentCenter.add(radius, radius));
    }

    @Override
    public Point[] getCollisionVertices() {
        return getBoundingBox().getCollisionVertices();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.PROJECTILE;
    }

    @Override
    public Point getCenter() {
        return currentCenter;
    }

    public Point getPreviousCenter() {
        return nextTickCenter;
    }

    @Override
    public void setCenter(Point newCenter) {
        this.currentCenter = newCenter;
        this.nextTickCenter = newCenter;
    }

    public int getDamage() {
        return damage;
    }
}
