package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.utils.Point;

public class Healer extends AIEntity {

    public Healer(String name, Point spawn, int maxDimension, float maxSpeed, int mass, Shape shape) {
        super(name, spawn, maxDimension, maxSpeed, mass, shape);
    }

    @Override
    public Weapon getWeapon() {
        return null;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public int getHealth() {
        return 0;
    }

    @Override
    public void setHealth(int health) {

    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        return false;
    }

    @Override
    public void restoreHealth(int healthToRestore) {

    }

    @Override
    public int getInitialHealth() {
        return 0;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {

    }

    @Override
    public void setColor(int color) {

    }

    @Override
    public void revertToDefaultColor() {

    }
}
