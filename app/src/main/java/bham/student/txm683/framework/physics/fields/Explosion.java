package bham.student.txm683.framework.physics.fields;

import android.graphics.Canvas;
import bham.student.txm683.framework.entities.entityshapes.Circle;
import bham.student.txm683.framework.entities.entityshapes.ICircle;
import bham.student.txm683.framework.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

public class Explosion extends InteractionField implements Renderable, ICircle {

    private Circle shape;
    private int damage;

    public Explosion(String name, String owner, Point center, float radius, int damage, int color) {
        super(owner, name);

        this.shape = new Circle(center, radius, color);

        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public float getRadius() {
        return shape.getRadius();
    }

    @Override
    public Circle getCircle() {
        return shape;
    }

    @Override
    public Point[] getCollisionVertices() {
        return getBoundingBox().getCollisionVertices();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        this.shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        this.shape.revertToDefaultColor();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return this.shape.getBoundingBox();
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translate(new Vector(shape.getCenter(), newCenter));
    }
}
