package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Kite;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Player extends MoveableEntity implements Damageable, Renderable, Collidable {

    private Kite shape;

    private int health;

    public Player(String name, Point center, int size, float maxSpeed, int upperTriColor, int lowerTriColor, int initialHealth) {
        super(name, center, maxSpeed);

        float width = size * 0.75f;
        float centerToLowerTri = size * 0.25f;

        this.shape = new Kite(new Vector[]{
                new Vector(center, center.add(new Point(0, -0.5f * size))),
                new Vector(center, center.add(new Point(width/2f, centerToLowerTri))),
                new Vector(center, center.add(new Point(width/-2f, centerToLowerTri))),
                new Vector(center, center.add(new Point(0, 0.5f * size))),
        }, upperTriColor, lowerTriColor);

        this.health = initialHealth;
    }

    @Override
    public void move(float secondsSinceLastGameTick) {
        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);
        shape.translateShape(movementVector);
        shape.rotateShape(movementVector);
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
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        if (renderEntityName)
            drawName(canvas, renderOffset);
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
}
    /*private void resetMeleeCharges(){
        this.currentMeleeCharge = 0;

        this.lastChargeTime = 0;
        this.currentChargeTime = 0;
    }

    public void chargeMelee(){
        currentChargeTime = System.currentTimeMillis();

        if (currentMeleeCharge < maxCharge && attackCooldown == 0) {
            if (currentChargeTime - lastChargeTime > TIME_BETWEEN_CHARGES) {
                currentMeleeCharge += 1;
                lastChargeTime = currentChargeTime;
                Log.d(TAG, "adding charge, charge now at: " + currentMeleeCharge);

                if (currentMeleeCharge == 1 || currentMeleeCharge == maxCharge){
                    shape.contractWidth(1.1f);
                }
            } else {
                Log.d(TAG, "not enough time passed for charge (" + (currentChargeTime - lastChargeTime) + ")");
            }
        } else {
            Log.d(TAG, "already at max charge");
        }
    }

    public void meleeAttack(){
        Log.d(TAG, "melee attack with charge of " + currentMeleeCharge);
        shape.returnToNormal();
        shape.contractWidth(0.8f);
        shape.contractHeight(1.4f);

        if (currentMeleeCharge > 0 && currentMeleeCharge <= maxCharge / 2){
            damageDealt = 1;
        } else if (currentMeleeCharge > 0){
            damageDealt = 3;
        }
        resetMeleeCharges();

        attackCooldown = 5;
        launchedAttack = true;
    }
    public void resetAttack(){
        launchedAttack = false;
        damageDealt = 0;

    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public void tickCooldown(){
        attackCooldown -= 1;

        if (attackCooldown <= 0){
            attackCooldown = 0;
            shape.returnToNormal();
        }
    }

    public int getDamageFromMeleeAttack(){
        return damageDealt;
    }*/