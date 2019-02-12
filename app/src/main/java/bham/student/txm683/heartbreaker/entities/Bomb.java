package bham.student.txm683.heartbreaker.entities;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.utils.Point;

public class Bomb extends Projectile {

    public Bomb(String name, Point center, float radius, float maxSpeed, int damage, int lifeInTicks, int color) {
        super(name, center, radius, maxSpeed, damage, lifeInTicks, color);
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        if (getLifeLeft() % 4 == 0)
            setColor(Color.RED);
        else
            revertToDefaultColor();

        super.tick(secondsSinceLastGameTick);
    }
}