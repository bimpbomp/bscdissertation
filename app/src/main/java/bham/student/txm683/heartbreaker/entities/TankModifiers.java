package bham.student.txm683.heartbreaker.entities;

public class TankModifiers {

    private float barrelLengthModifier;
    private float barrelWidthModifier;
    private float turretSizeModifier;

    public TankModifiers(){
        barrelLengthModifier = 1;
        barrelWidthModifier = 1;
        turretSizeModifier = 1;
    }

    public float getBarrelLengthModifier() {
        return barrelLengthModifier;
    }

    public void setBarrelLengthModifier(float barrelLengthModifier) {
        this.barrelLengthModifier = barrelLengthModifier;
    }

    public float getBarrelWidthModifier() {
        return barrelWidthModifier;
    }

    public void setBarrelWidthModifier(float barrelWidthModifier) {
        this.barrelWidthModifier = barrelWidthModifier;
    }

    public float getTurretSizeModifier() {
        return turretSizeModifier;
    }

    public void setTurretSizeModifier(float turretSizeModifier) {
        this.turretSizeModifier = turretSizeModifier;
    }
}
