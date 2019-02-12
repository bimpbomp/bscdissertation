package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Kite;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.BombThrower;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.List;

//TODO fix collision error between Wall 0 and player on the RHS of Wall 0.
public class Player extends MoveableEntity implements Damageable, Renderable, Collidable {

    private Kite shape;

    private int health;

    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;

    public Player(String name, Point center, int size, float maxSpeed, int upperTriColor, int lowerTriColor, int initialHealth) {
        super(name, maxSpeed);

        float width = size * 0.9f;

        List<Vector> vertices = Polygon.createTriangle(center, width, size * 0.75f);

        this.shape = new Kite(center, new Vector[]{
                vertices.get(0),
                vertices.get(1),
                new Vector(center, center.add(new Point(0, 0.5f * size))),
                vertices.get(2)
        }, upperTriColor, lowerTriColor);

        this.health = initialHealth;

        this.primaryWeapon = new BasicWeapon(name);
        this.secondaryWeapon = new BombThrower(name);
    }

    public Projectile[] shootPrimary(){
        return primaryWeapon.shoot(calcBulletPlacement(primaryWeapon.getBulletRadius()));
    }

    public Projectile[] shootSecondary(){
        return secondaryWeapon.shoot(calcBulletPlacement(secondaryWeapon.getBulletRadius()));
    }

    private Vector calcBulletPlacement(float bulletRadius){
        Vector bulletPlacement = new Vector(getCenter(), shape.getVertices()[0]);
        return bulletPlacement.sMult((bulletPlacement.getLength() + bulletRadius + (calculateMovementVector(1/25f).getLength()) + 5f)/ bulletPlacement.getLength());
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {
        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        if (!movementVector.equals(Vector.ZERO_VECTOR)) {
            shape.translateShape(movementVector);
            shape.rotateShape(movementVector);
        }

    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;

        return health < 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        this.health += healthToRestore;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        shape.setColor(primaryWeapon.getSymbolisingColor());

        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
    }

    @Override
    public BoundingBox getRenderingVertices() {
        return shape.getRenderingVertices();
    }

    @Override
    public void setColor(int color) {
        shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        shape.revertToDefaultColor();
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
        shape.translateShape(new Vector (shape.getCenter(), newCenter));
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.PLAYER;
    }
}