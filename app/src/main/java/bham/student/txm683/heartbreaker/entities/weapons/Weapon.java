package bham.student.txm683.heartbreaker.entities.weapons;

import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.GameTickTimer;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class Weapon {

    private String owner;
    private UniqueID uniqueID;

    private int afterFiringCooldownInTicks;
    private int currentCooldown;

    private GameTickTimer timer;

    private AmmoType ammoType;

    private float speed;

    public Weapon(String owner, int afterFiringCooldownInTicks, AmmoType ammoType, float bulletSpeed){
        this.owner = owner;
        this.uniqueID = new UniqueID();

        this.afterFiringCooldownInTicks = afterFiringCooldownInTicks;
        this.currentCooldown = 0;

        this.timer = new GameTickTimer(25);

        this.ammoType = ammoType;

        this.speed = bulletSpeed;
    }

    public float getSpeed() {
        return speed;
    }

    public AmmoType getAmmoType() {
        return ammoType;
    }

    public abstract Projectile[] shoot(Vector shootVector);

    void tickCooldown(){
        if (inCooldown()) {
            currentCooldown -= timer.tick();

            if (currentCooldown <= 0)
                timer.stop();
        }
    }

    void startCooldown(){
        timer.start();
        currentCooldown = afterFiringCooldownInTicks;
    }

    boolean inCooldown(){
        return currentCooldown > 0;
    }

    public abstract int getAmmo();

    public abstract void addAmmo(int amountToAdd);

    int getNextID(){
        return uniqueID.id();
    }

    public abstract int getSymbolisingColor();

    public String getOwner() {
        return owner;
    }

    public abstract float getBulletRadius();

    void setAfterFiringCooldownInTicks(int ticks){
        this.afterFiringCooldownInTicks = ticks;
    }
}
