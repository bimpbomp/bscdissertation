package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Hexagon;
import bham.student.txm683.heartbreaker.entities.entityshapes.Octagon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Core extends AIEntity implements Damageable {
    private Octagon innerShape;
    private Hexagon outerShape;

    private int health;

    public Core(String name, Point center, int size, float maxSpeed) {
        super(name, maxSpeed);
        health = 500;

        this.innerShape = new Octagon(center, size/2, Color.WHITE);
        this.outerShape = new Hexagon(center, size, Color.BLACK);
    }

    @Override
    void update() {

    }

    @Override
    void chase(MoveableEntity entityToChase) {

    }

    @Override
    void halt() {

    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;
        return health <= 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {
        float angle = 0.261799f;

        innerShape.rotateBy(-1 * angle);
        outerShape.rotateBy(angle/2);
    }

    @Override
    public Point[] getCollisionVertices() {
        return outerShape.getVertices();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return innerShape.getShapeIdentifier();
    }

    @Override
    public Point getCenter() {
        return innerShape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        innerShape.translateShape(new Vector(innerShape.getCenter(), newCenter));
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        outerShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        innerShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        innerShape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        innerShape.revertToDefaultColor();
    }

    @Override
    public BoundingBox getRenderingVertices() {
        return outerShape.getRenderingVertices();
    }
}
