package bham.student.txm683.framework.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.framework.entities.entityshapes.Circle;
import bham.student.txm683.framework.entities.entityshapes.ICircle;
import bham.student.txm683.framework.entities.entityshapes.Rectangle;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

public class Projectile extends MoveableEntity implements Renderable, ICircle {
    private Point currentCenter;
    private Point nextTickCenter;

    private int damage;
    private int lifeInTicks;

    private Rectangle r;

    private String owner;

    public Projectile(String name, String owner, Point center, float radius, float maxSpeed, int damage, int lifeInTicks, int color){
        super(name, center, (int) radius*2, maxSpeed, 1, new Circle(center, radius, color));

        this.owner = owner;

        this.currentCenter = center;
        this.nextTickCenter = center;

        this.damage = damage;

        this.lifeInTicks = lifeInTicks;

        r = null;
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
        float radius = getCircle().getRadius();
        return new BoundingBox(currentCenter.add(-1 * radius, -1 * radius), currentCenter.add(radius, radius));
    }

    @Override
    public Point[] getCollisionVertices() {
        float width = getRadius() * 2;
        Vector v = new Vector (currentCenter, nextTickCenter);
        float length = v.getLength() + getRadius() * 2;

        Point center = v.sMult(0.5f).getHead();

        r = new Rectangle(center, width, length, Color.BLACK);

        r.rotate(v.getUnitVector());

        return r.getVertices();
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
    public Point getCenter() {
        return currentCenter;
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
