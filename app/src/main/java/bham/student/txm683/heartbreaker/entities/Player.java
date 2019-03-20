package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.BombThrower;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.pickups.Key;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Player extends MoveableEntity implements Damageable, Renderable {

    private int health;

    private Weapon primaryWeapon;
    private Weapon secondaryWeapon;

    private List<Key> keys;

    private Player(String name, Point center, int size, float maxSpeed, int color, int initialHealth) {
        //super(name, center, size, maxSpeed, Kite.constructKite(center, size, upperTriColor));
        super(name, center, size, maxSpeed, new TankBody(center, size, color));

        this.health = initialHealth;

        this.primaryWeapon = new BasicWeapon(name);
        this.secondaryWeapon = new BombThrower(name);

        this.keys = new ArrayList<>();
    }

    private Player(Point center){
        this("player", center, 150, 600, ColorScheme.PLAYER_COLOR, 100000);
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

    public void addKey(Key key){
        this.keys.add(key);
    }

    public List<Key> getKeys(){
        return keys;
    }

    public int getAmmo() {
        return primaryWeapon.getAmmo();
    }

    public int getSecondaryAmmo() {
        return secondaryWeapon.getAmmo();
    }

    public void addAmmo(int amountToAdd){
        this.primaryWeapon.addAmmo(amountToAdd);
    }

    public void addSecondaryAmmo(int amountToAdd){
        this.secondaryWeapon.addAmmo(amountToAdd);
    }

    public AmmoType getAmmoType() {
        return primaryWeapon.getAmmoType();
    }

    public AmmoType getSecondaryAmmoType() {
        return secondaryWeapon.getAmmoType();
    }

    public Projectile[] shoot(){
        return primaryWeapon.shoot(((TankBody)getShape()).getShootingVector());
    }

    public Projectile[] shootSecondary(){
        return secondaryWeapon.shoot(calcBulletPlacement(secondaryWeapon.getBulletRadius()));
    }

    private Vector calcBulletPlacement(float bulletRadius){
         return new Vector(getCenter(), getForwardUnitVector().getHead());
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;

        return health <= 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        this.health += healthToRestore;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {

        getShape().draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
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