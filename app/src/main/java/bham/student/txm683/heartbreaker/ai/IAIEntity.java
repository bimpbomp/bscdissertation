package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public interface IAIEntity {
    Weapon getWeapon();
    Vector getShootingVector();
    Point getCenter();
    Vector getForwardUnitVector();
    BoundingBox getBoundingBox();
    String getName();
    Vector getVelocity();
    void setVelocity(Vector newVelocity);
    float getMaxSpeed();
    BContext getContext();
    int getWidth();
    void setRotationVector(Vector v);
}
