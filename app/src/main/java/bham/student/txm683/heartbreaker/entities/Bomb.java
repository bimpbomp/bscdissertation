package bham.student.txm683.heartbreaker.entities;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.utils.Point;

public class Bomb extends Projectile {

    public Bomb(String name, String owner, Point center, float radius, float maxSpeed, int damage, int lifeInTicks, int color) {
        super(name, owner, center, radius, maxSpeed, damage, lifeInTicks, color);
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        if (getLifeLeft() % 4 == 0)
            setColor(Color.RED);
        else
            revertToDefaultColor();

        super.tick(secondsSinceLastGameTick);
    }

    public Explosion explode(){
        return new Explosion(getName(), getOwner(), getCenter(), getRadius()*5, getDamage(), Color.RED);
    }
}
