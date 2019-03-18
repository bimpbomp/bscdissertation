package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ICircle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Projectile extends MoveableEntity implements Renderable, ICircle {
    private Point currentCenter;
    private Point nextTickCenter;

    private float radius;

    private Paint paint;
    private int defaultColor;
    private int currentColor;

    private int damage;
    private int lifeInTicks;

    private String owner;

    public Projectile(String name, String owner, Point center, float radius, float maxSpeed, int damage, int lifeInTicks, int color){
        super(name, center, (int) radius*2, maxSpeed);

        this.owner = owner;

        this.paint = new Paint();
        this.currentColor = color;
        this.defaultColor = color;

        this.currentCenter = center;
        this.nextTickCenter = center;

        this.radius = radius;

        this.damage = damage;

        this.lifeInTicks = lifeInTicks;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public Circle getCircle() {
        return new Circle(currentCenter, radius, currentColor);
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        currentCenter = nextTickCenter;

        nextTickCenter = currentCenter.add(movementVector.getRelativeToTailPoint());

        lifeInTicks--;
    }

    private Point interpolateCenter(float secondsSinceLastGameTick){
        return new Vector(currentCenter, nextTickCenter).sMult(secondsSinceLastGameTick).getHead();
    }

    public boolean outOfLife(){
        return lifeInTicks < 1;
    }

    public int getLifeLeft(){
        return lifeInTicks;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName){
        paint.setColor(currentColor);

        Point offsetCenter = interpolateCenter(secondsSinceLastRender).add(renderOffset);

        canvas.drawCircle(offsetCenter.getX(), offsetCenter.getY(), radius, paint);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(currentCenter.add(-1 * radius, -1 * radius), currentCenter.add(radius, radius));
    }

    @Override
    public void setColor(int color) {
        currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        currentColor = defaultColor;
    }

    @Override
    public Point[] getCollisionVertices() {
        BoundingBox bb = getBoundingBox();
        return new Point[]{
                bb.getTopLeft(),
                bb.getTopRight(),
                bb.getBottomRight(),
                bb.getBottomLeft()
        };
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
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.CIRCLE;
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
    }


    public int getDamage() {
        return damage;
    }
}
