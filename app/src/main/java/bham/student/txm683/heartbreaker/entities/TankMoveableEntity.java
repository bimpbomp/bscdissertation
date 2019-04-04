package bham.student.txm683.heartbreaker.entities;

import android.util.Log;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class TankMoveableEntity extends MoveableEntity {

    public TankMoveableEntity(String name, Point spawn, int maxDimension, float maxSpeed, int mass, Shape shape) {
        super(name, spawn, maxDimension, maxSpeed, mass, shape);
    }

    @Override
    public void applyMovementForces(float secondsSinceLastGameTick) {
        super.applyMovementForces(secondsSinceLastGameTick);
        //rotate turret
        Vector turretFUnit = ((TankBody) getShape()).getTurretFUnit();

        Log.d("TANK", "rotating turret");
        float angularVelocity;

        if (getRotationVector().equals(Vector.ZERO_VECTOR)) {
            //no input on turret stick, so rotate turret inline with body
            Vector v = getVelocity().sMult(secondsSinceLastGameTick);
            angularVelocity = getAngularVelocity(v.getUnitVector(), secondsSinceLastGameTick, getShape().getForwardUnitVector());
            ((TankBody) getShape()).rotateTurret(angularVelocity);

        } else {
            angularVelocity = getAngularVelocity(getRotationVector(), secondsSinceLastGameTick,
                    turretFUnit);

            if (angularVelocity > maxAngularVelocity)
                angularVelocity = maxAngularVelocity;

            ((TankBody) getShape()).rotateTurret(angularVelocity);
        }
    }
}
