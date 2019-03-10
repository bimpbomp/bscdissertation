package bham.student.txm683.heartbreaker.entities.weapons;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.Bomb;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.Vector;

public class BombThrower extends Weapon {

    private float bulletRadius;
    private float speed;
    private int damage;
    private int symbolisingColor;
    private int fuseLengthInTicks;

    private int ammo;

    public BombThrower(String owner) {
        super(owner, 10, AmmoType.BOMB, 0f);

        bulletRadius = 30f;
        speed = 0f;
        damage = 200;
        symbolisingColor = Color.BLACK;
        fuseLengthInTicks = 50;

        ammo = 4;
    }

    @Override
    public Projectile[] shoot(Vector shootVector) {
        if (!inCooldown() && ammo > 0) {
            ammo--;
            Bomb bullet = new Bomb(getOwner()+getNextID(), getOwner(), shootVector.getHead(), bulletRadius,
                    speed, damage,fuseLengthInTicks, symbolisingColor);
            bullet.setRequestedMovementVector(shootVector.getUnitVector());

            startCooldown();
            return new Projectile[]{bullet};
        }

        tickCooldown();
        return new Projectile[0];
    }

    @Override
    public int getAmmo() {
        return ammo;
    }

    @Override
    public void addAmmo(int amountToAdd) {
        ammo += amountToAdd;
    }

    @Override
    public int getSymbolisingColor() {
        return 0;
    }

    @Override
    public float getBulletRadius() {
        return 0;
    }
}
