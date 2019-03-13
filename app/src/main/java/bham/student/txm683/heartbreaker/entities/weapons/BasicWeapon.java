package bham.student.txm683.heartbreaker.entities.weapons;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.Vector;

public class BasicWeapon extends Weapon {

    private float bulletRadius;
    private static float speed = 1000;
    private int damage;
    private int symbolisingColor;

    private int bulletLife;

    public BasicWeapon(String owner){
        super(owner, 3, AmmoType.BULLET, speed);

        this.bulletRadius = 20f;
        this.damage = 10;

        this.symbolisingColor = Color.MAGENTA;

        this.bulletLife = 25;
    }

    public BasicWeapon(String owner, int cooldownTicks, int damage, int radiusModifier){
        this(owner);
        setAfterFiringCooldownInTicks(cooldownTicks);
        this.damage = damage;

        this.bulletRadius *= radiusModifier;
    }

    public BasicWeapon(String owner, int cooldownTicks){
        this(owner);
        setAfterFiringCooldownInTicks(cooldownTicks);
    }

    @Override
    public Projectile[] shoot(Vector shootVector) {

        if (!inCooldown()) {
            Projectile bullet = new Projectile(getOwner()+getNextID(), getOwner(), shootVector.getHead(), bulletRadius,
                    speed, damage, bulletLife, symbolisingColor);
            bullet.setRequestedMovementVector(shootVector.getUnitVector());

            startCooldown();
            return new Projectile[]{bullet};
        }

        tickCooldown();
        return new Projectile[0];
    }

    @Override
    public int getAmmo() {
        return 1;
    }

    @Override
    public void addAmmo(int amountToAdd) {

    }

    @Override
    public float getBulletRadius() {
        return bulletRadius;
    }

    @Override
    public int getSymbolisingColor() {
        return symbolisingColor;
    }
}
