package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Behaviour;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.entities.Shooter;
import bham.student.txm683.heartbreaker.entities.entityshapes.Kite;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Drone extends AIEntity implements Shooter {

    private Kite shape;
    private int health;

    private Point spawn;

    private int currentTargetNodeInPath;
    private boolean atDestination;

    private Weapon weapon;
    private BNode behaviourTreeRoot;

    private int width;

    public Drone(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, center, size, maxSpeed);

        this.spawn = center;
        List<Vector> vertices = Polygon.createTriangle(center, size*0.9f, size * 0.75f);

        this.shape = new Kite(center, new Vector[]{
                vertices.get(0),
                vertices.get(1),
                new Vector(center, center.add(new Point(0, 0.5f * size))),
                vertices.get(2)
        }, colorValue, colorValue);

        this.width = size;

        this.health = initialHealth;
        this.atDestination = false;
        this.currentTargetNodeInPath = 0;

        this.weapon = new BasicWeapon(getName(), 7);

        this.behaviourTreeRoot = Behaviour.droneTree();

        context.addPair(BKeyType.VIEW_RANGE, 600);
        context.addPair(BKeyType.CONTROLLED_ENTITY, this);
        context.addPair(BKeyType.TIME_PER_IDLE, 25);
        context.addPair(BKeyType.HEALTH_BOUND, 50);
        context.addPair(BKeyType.ROT_DAMP, 0.5f);
    }

    public Drone(String name, Point center){
        this(name, center, 100, ColorScheme.CHASER_COLOR, 600, 100);
    }

    @Override
    public Point getFront() {
        return shape.getVertices()[0];
    }

    public static Drone build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);
        String name = jsonObject.getString("name");

        Drone drone = new Drone(name, center);

        if (jsonObject.has("osr")){
            Vector osr = new Vector(new Point(jsonObject.getJSONObject("osr")));
            drone.shape.rotateShape(osr);

            drone.getContext().addVariable("osr", osr);
        }

        if (jsonObject.has("drops")){
            PickupType drops = PickupType.valueOf(jsonObject.getString("drops"));
            drone.setDrops(drops);
        }

        return drone;
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public void rotate(Vector rotationVector) {
        shape.rotateShape(rotationVector);
    }

    @Override
    public void rotateBy(float angle) {
        shape.rotateBy(angle);
    }

    /*public void update() {
        if (path == null || path.length == 0) {

            //path = applyAStar(getName(), levelState.getGraph().getNode(new Tile(1900, 500)), levelState.getGraph().getNode(new Tile(600, 2000)), 10);
            Node<Tile> startNode = getClosestNode(new Tile(getCenter()), levelState.getGraph());
            Node<Tile> targetNode = getClosestNode(new Tile(levelState.getPlayer().getCenter()), levelState.getGraph());

            if (!levelState.getGraph().containsNode(targetNode) || !levelState.getGraph().containsNode(startNode))
                return;

            path = applyAStar(getName(), startNode, targetNode, 10);

            if (path.length > 1){
                atDestination = false;
                currentTargetNodeInPath = 1;
                setRequestedMovementVector(new Vector(getCenter(), new Point(path[currentTargetNodeInPath])).getUnitVector());
            } else {
                atDestination = true;
                currentTargetNodeInPath = 0;
            }
        }
    }*/

    @Override
    public void tick(float secondsSinceLastGameTick) {

        behaviourTreeRoot.process(context);

        move(secondsSinceLastGameTick, shape,(float) context.getValue(BKeyType.ROT_DAMP));

        if (!getRotationVector().equals(Vector.ZERO_VECTOR))
            rotate(secondsSinceLastGameTick, shape, (float)context.getValue(BKeyType.ROT_DAMP));
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public Vector getForwardUnitVector() {
        return shape.getForwardUnitVector();
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
        Vector bulletPlacement = new Vector(getCenter(), shape.getVertices()[0]);
        return bulletPlacement.sMult((bulletPlacement.getLength() + bulletRadius + (calculateMovementVector(1/25f).getLength()) + 5f)/ bulletPlacement.getLength());
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
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
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;
        return health <= 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
    }

    @Override
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public void setColor(int color) {
        shape.setColor(color);
        shape.setUpperTriColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        shape.revertToDefaultColor();
        shape.revertUppertriToDefaultColor();
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return shape.getShapeIdentifier();
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translateShape(new Vector(shape.getCenter(), newCenter));
    }
}
