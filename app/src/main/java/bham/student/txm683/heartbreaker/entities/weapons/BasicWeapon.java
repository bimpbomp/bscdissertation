package bham.student.txm683.heartbreaker.entities.weapons;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.Vector;

public class BasicWeapon extends Weapon {

    private float bulletRadius;
    private float speed;
    private int damage;
    private int symbolisingColor;

    private int bulletLife;

    public BasicWeapon(String owner){
        super(owner, 3);

        this.bulletRadius = 20f;
        this.speed = 300;
        this.damage = 10;

        this.symbolisingColor = Color.MAGENTA;

        this.bulletLife = 25;
    }

    @Override
    public Projectile[] shoot(Vector shootVector) {

        if (!inCooldown()) {
            Projectile bullet = new Projectile(getOwner(), shootVector.getHead(), bulletRadius,
                    speed, damage, bulletLife, symbolisingColor);
            bullet.setRequestedMovementVector(shootVector.getUnitVector());

            startCooldown();
            return new Projectile[]{bullet};
        }

        tickCooldown();
        return new Projectile[0];
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
