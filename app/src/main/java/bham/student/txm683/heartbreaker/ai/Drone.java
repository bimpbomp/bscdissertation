package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Behaviour;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.entities.Shooter;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.rendering.HealthBar;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class Drone extends AIEntity implements Shooter {
    private int health;
    private int initialHealth;
    private HealthBar healthBar;

    private Weapon weapon;
    private BNode behaviourTreeRoot;
    private BNode shootTreeRoot;

    private int width;

    public Drone(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, center, size, maxSpeed, 1, constructShape(center, size, colorValue));

        this.width = size;

        this.health = initialHealth;
        this.initialHealth = initialHealth;
        healthBar = new HealthBar(this);

        this.weapon = new BasicWeapon(getName());

        this.behaviourTreeRoot = Behaviour.droneTree();
        this.shootTreeRoot = Behaviour.stationaryShootBehaviour();

        context.addPair(BKeyType.VIEW_RANGE, 600);
        context.addPair(BKeyType.CONTROLLED_ENTITY, this);
        context.addPair(BKeyType.TIME_PER_IDLE, 25);
        context.addPair(BKeyType.HEALTH_BOUND, 50);
        context.addPair(BKeyType.ROT_DAMP, 0.5f);
    }

    public Drone(String name, Point center){
        this(name, center, 100, ColorScheme.CHASER_COLOR, 400, 100);
    }

    private static Shape constructShape(Point center, int size, int colorValue){
        /*List<Vector> vertices = Polygon.createTriangle(center, size*0.9f, size * 0.75f);

        return new Kite(center, new Vector[]{
                vertices.get(0),
                vertices.get(1),
                new Vector(center, center.add(new Point(0, 0.5f * size))),
                vertices.get(2)
        }, colorValue, colorValue);*/

        return new TankBody(center, size, colorValue);
    }

    public static Drone build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);
        String name = jsonObject.getString("name");

        Drone drone = new Drone(name, center);

        if (jsonObject.has("osr")){
            Vector osr = new Vector(new Point(jsonObject.getJSONObject("osr")));
            drone.getShape().rotate(osr);

            drone.getContext().addVariable("osr", osr);
        }

        if (jsonObject.has("drops")){
            PickupType drops = PickupType.valueOf(jsonObject.getString("drops"));
            drone.setDrops(drops);
        }

        return drone;
    }

    @Override
    public int getInitialHealth() {
        return initialHealth;
    }

    @Override
    public void onDeath() {
        Random r = new Random();
        int i = r.nextInt(100);

        if (i < 65){
            levelState.getPickups().add(new Pickup("HEALTH" + i, PickupType.HEALTH, getCenter()));
        } else {
            levelState.addExplosion(new Explosion("EXPLOSION" + i, getName(), getCenter(), 150, 50, Color.RED));
        }
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        behaviourTreeRoot.process(context);
        shootTreeRoot.process(context);

        super.tick(secondsSinceLastGameTick);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getAmmo() {
        return weapon.getAmmo();
    }

    @Override
    public void addAmmo(int amountToAdd) {
        weapon.addAmmo(amountToAdd);
    }

    @Override
    public AmmoType getAmmoType() {
        return weapon.getAmmoType();
    }

    @Override
    public Projectile[] shoot() {
        return weapon.shoot(calcBulletPlacement(weapon.getBulletRadius()));
    }

    @Override
    public Vector calcBulletPlacement(float bulletRadius) {
        return getForwardUnitVector();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;

        if (this.health > initialHealth)
            this.health = initialHealth;
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;
        return health <= 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;

        if (health > initialHealth)
            health = initialHealth;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        getShape().draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        healthBar.draw(canvas, getCenter().add(0,50));

        drawPath(canvas, renderOffset);
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
