package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.pickups.Key;
import bham.student.txm683.heartbreaker.rendering.HealthBar;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Player extends MoveableEntity implements Damageable, Renderable {

    private HealthBar healthBar;

    private Weapon primaryWeapon;

    private List<Key> keys;

    private int mesh;

    private Player(String name, Point center, int size, float maxSpeed, int color, int initialHealth) {
        super(name, center, size, maxSpeed, 1, new TankBody(center, size, color));

        this.healthBar = new HealthBar(initialHealth);

        this.primaryWeapon = new BasicWeapon(name, 20, 40, 20, getShape().getColor());

        this.keys = new ArrayList<>();

        mesh = -1;
    }

    private Player(Point center){
        this("player", center, 100, 600, ColorScheme.PLAYER_COLOR, 400);
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

    public int getMesh() {
        return mesh;
    }

    public void setMesh(int mesh) {
        this.mesh = mesh;
    }

    public void addKey(Key key){
        this.keys.add(key);
    }

    public List<Key> getKeys(){
        return keys;
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

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.PLAYER;
    }
}