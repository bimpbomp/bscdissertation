package bham.student.txm683.heartbreaker.entities.weapons;

import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.GameTickTimer;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class Weapon {

    String owner;
    UniqueID uniqueID;

    private int afterFiringCooldownInTicks;
    private int currentCooldown;

    private GameTickTimer timer;

    public Weapon(String owner, int afterFiringCooldownInTicks){
        this.owner = owner;
        this.uniqueID = new UniqueID();

        this.afterFiringCooldownInTicks = afterFiringCooldownInTicks;
        this.currentCooldown = 0;

        this.timer = new GameTickTimer(25);
    }

    public abstract Projectile[] shoot(Vector shootVector);

    void tickCooldown(){
        if (inCooldown() && timer.hasTicked()) {
            currentCooldown--;

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

    int getNextID(){
        return uniqueID.id();
    }

    public abstract int getSymbolisingColor();

    public String getOwner() {
        return owner;
    }

    public abstract float getBulletRadius();
}
