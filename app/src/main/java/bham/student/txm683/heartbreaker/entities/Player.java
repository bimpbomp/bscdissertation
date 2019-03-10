package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.util.Log;
import bham.student.txm683.heartbreaker.entities.entityshapes.Kite;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.BombThrower;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.pickups.Key;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.List;

//TODO fix collision error between Wall 0 and player on the RHS of Wall 0.
public class Player extends MoveableEntity implements Damageable, Renderable {

    private Kite shape;

    private int health;

    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;

    private Vector velocity;

    private List<Key> keys;

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

        this.keys = new ArrayList<>();

        this.velocity = Vector.ZERO_VECTOR;
    }

    public void addKey(Key key){
        this.keys.add(key);
    }

    public List<Key> getKeys(){
        return keys;
    }

    public int getAmmo() {
        return primaryWeapon.getAmmo();
    }

    public int getSecondaryAmmo() {
        return secondaryWeapon.getAmmo();
    }

    public void addAmmo(int amountToAdd){
        this.primaryWeapon.addAmmo(amountToAdd);
    }

    public void addSecondaryAmmo(int amountToAdd){
        this.secondaryWeapon.addAmmo(amountToAdd);
    }

    public AmmoType getAmmoType() {
        return primaryWeapon.getAmmoType();
    }

    public AmmoType getSecondaryAmmoType() {
        return secondaryWeapon.getAmmoType();
    }

    public Projectile[] shoot(){
        return primaryWeapon.shoot(calcBulletPlacement(primaryWeapon.getBulletRadius()));
    }

    public Projectile[] shootSecondary(){
        return secondaryWeapon.shoot(calcBulletPlacement(secondaryWeapon.getBulletRadius()));
    }

    private Vector calcBulletPlacement(float bulletRadius){
         return shape.getForwardUnitVector();
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {


        if (!getRequestedMovementVector().equals(Vector.ZERO_VECTOR)) {
            Vector movementForce = getRequestedMovementVector().sMult(4);

            float dot = movementForce.dot(shape.getForwardUnitVector());

            movementForce = movementForce.sMult((float) Math.pow(dot, 2));

            Vector acc = movementForce.sMult(secondsSinceLastGameTick);

            velocity = velocity.vAdd(acc);

            float max = getMaxSpeed() * secondsSinceLastGameTick;

            if (velocity.getLength() > max)
                velocity = velocity.setLength(max);

            Log.d("VELOCITY", "vel: " + velocity.relativeToString() + " sped: " + velocity.getLength() + " acc: " + acc.relativeToString() + " f: " + movementForce.relativeToString() + " mV: " + getRequestedMovementVector().relativeToString() + " dot: " + dot);

            shape.translateShape(velocity);

        } else {
            velocity = velocity.sMult(0.25f);

            shape.translateShape(velocity);
        }

        Vector force;
        if (!getRotationVector().equals(Vector.ZERO_VECTOR)){
            force = getRotationVector();

        } else
            force = getRequestedMovementVector();

        if (!force.equals(Vector.ZERO_VECTOR)) {
            Vector momArm = new Vector(getCenter(), getCenter().add(shape.getForwardUnitVector().sMult(10f).getRelativeToTailPoint()));
            Vector parCom = momArm.sMult(force.dot(momArm) / momArm.getLength());

            Vector angF = force.vSub(parCom);

            float angularAcc = momArm.det(angF);

            float angularVelocity = angularAcc * secondsSinceLastGameTick;

            shape.rotateBy(angularVelocity);
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

        return health <= 0;
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
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
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