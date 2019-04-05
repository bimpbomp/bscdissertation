package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.framework.entities.Projectile;
import bham.student.txm683.framework.entities.weapons.Weapon;
import bham.student.txm683.framework.physics.Damageable;
import bham.student.txm683.framework.rendering.HealthBar;
import bham.student.txm683.framework.rendering.Renderable;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.rendering.ColorScheme;
import org.json.JSONException;
import org.json.JSONObject;

public class Player extends TankMoveableEntity implements Damageable, Renderable {

    private HealthBar healthBar;

    private Weapon primaryWeapon;

    private Player(String name, Point center, int size, float maxSpeed, int color, int initialHealth) {
        super(name, center, size, maxSpeed, 1, new TankBody(center, size, color));

        this.healthBar = new HealthBar(initialHealth);

        this.primaryWeapon = new BasicWeapon(name, 1000, 40, 20, getShape().getColor());
    }

    private Player(Point center){
        this("player", center, 100, 600, ColorScheme.PLAYER_COLOR, 4000000);
    }

    public static Player build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);

        Player player = new Player(center);

        if (jsonObject.has("osr")){
            Vector osr = new Vector(new Point(jsonObject.getJSONObject("osr")));
            player.getShape().rotate(osr);
            ((TankBody)player.getShape()).rotateTurret(osr);
        }

        return player;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        super.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        healthBar.draw(canvas, getCenter().add(renderOffset).add(0, 75));
    }

    public Projectile[] shoot(){
        return primaryWeapon.shoot(((TankBody)getShape()).getShootingVector());
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getHealth() {
        return this.healthBar.getHealth();
    }

    @Override
    public void setHealth(int health) {
        this.healthBar.setHealth(health);
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        return healthBar.inflictDamage(damageToInflict);
    }

    public int getInitialHealth() {
        return healthBar.getInitialHealth();
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        healthBar.restoreHealth(healthToRestore);
    }

    @Override
    public void setColor(int color) {
        getShape().setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        getShape().revertToDefaultColor();
    }
}