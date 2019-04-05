package bham.student.txm683.framework.ai;

import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.entities.weapons.Weapon;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

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
