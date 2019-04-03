package bham.student.txm683.heartbreaker.entities.weapons;

import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.GameTickTimer;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class Weapon {

    private String owner;
    private UniqueID uniqueID;

    private GameTickTimer timer;

    private float speed;
    private int damage;
    private float bulletRadius;
    private int bulletLife;

    private int ammo;

    private int color;

    public Weapon(String owner, int cooldownBetweenShotsInMillis, int damage, float speed, float bulletRadius, int bulletLife, int color){
        this.owner = owner;
        this.uniqueID = new UniqueID();

        this.timer = new GameTickTimer(cooldownBetweenShotsInMillis);

        this.damage = damage;
        this.speed = speed;
        this.bulletRadius = bulletRadius;
        this.bulletLife = bulletLife;

        this.color = color;
    }

    public int getAmmo(){
        return ammo;
    }

    public void addAmmo(int amountToAdd){
        ammo += amountToAdd;
    }

    public Projectile[] shoot(Vector shootVector) {
        tickCooldown();

        if (!inCooldown()) {
            Projectile bullet = new Projectile(getOwner()+getNextID(), getOwner(), shootVector.getHead(), bulletRadius,
                    speed, getDamage(), bulletLife, color);
            bullet.setRequestedMovementVector(shootVector.getUnitVector());

            startCooldown();

            if (ammo != Integer.MAX_VALUE)
                ammo--;

            return new Projectile[]{bullet};
        }
        return new Projectile[0];
    }

    public int getDamage() {
        return damage;
    }

    public float getSpeed() {
        return speed;
    }

    public void tickCooldown(){
        if (inCooldown() && timer.tick() > 0) {
            timer.stop();
        }
    }

    public void startCooldown(){
        timer.start();
    }

    public boolean inCooldown(){
        return timer.isActive();
    }

    public int getNextID(){
        return uniqueID.id();
    }

    public String getOwner() {
        return owner;
    }
}
