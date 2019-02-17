package bham.student.txm683.heartbreaker.entities;

import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.utils.Vector;

public interface Shooter {

    int getAmmo();
    void addAmmo(int amountToAdd);
    AmmoType getAmmoType();
    Projectile[] shoot();
    Vector calcBulletPlacement(float bulletRadius);
}
