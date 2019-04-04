package bham.student.txm683.heartbreaker.entities.weapons;

public class BasicWeapon extends Weapon {
    private static final float SPEED = 1500;
    private static final int BULLET_LIFE = 25;

    public BasicWeapon(String owner, int cooldownMillis, int damage, float radius, int color){
        super(owner, cooldownMillis, damage, SPEED, radius, BULLET_LIFE, color);
        setAmmo(Integer.MAX_VALUE);
    }
}
