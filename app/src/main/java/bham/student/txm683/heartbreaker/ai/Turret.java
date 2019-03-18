package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Behaviour;
import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTriangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Turret extends AIEntity {

    private int health;
    private IsoscelesTriangle shape;

    private Point spawn;

    private int width;

    private Weapon weapon;

    private BNode behaviourTreeRoot;

    public Turret(String name, Point center, int size, int colorValue, int initialHealth) {
        super(name, center, size, 0);

        shape = new IsoscelesTriangle(center, Polygon.createTriangle(center, size, size).toArray(new Vector[0]), colorValue);

        health = initialHealth;

        this.weapon=  new BasicWeapon(name, 25, 30, 1.5f);

        this.width = size;

        this.spawn = center;

        //this.behaviourTreeRoot = Behaviour.stationaryShootBehaviour();
        this.behaviourTreeRoot = Behaviour.turretTree();

        context.addPair(BKeyType.VIEW_RANGE, 600);
        context.addPair(BKeyType.CONTROLLED_ENTITY, this);
        context.addPair(BKeyType.TIME_PER_IDLE, 25);
    }

    public Turret(String name, Point center){
        this(name ,center, 200, ColorScheme.CHASER_COLOR, 100);
    }

    @Override
    public Point getFront() {
        return shape.getVertices()[0];
    }

    public static Turret build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);
        String name = jsonObject.getString("name");

        Turret turret = new Turret(name, center);

        if (jsonObject.has("osr")){
            Vector initialRotation = new Vector(new Point(jsonObject.getJSONObject("osr")));
            turret.getContext().addVariable("osr", initialRotation);

            turret.shape.rotateShape(initialRotation);
        }

        if (jsonObject.has("rotation_lock")){
            turret.getContext().addVariable("rotation_lock", (float) jsonObject.getDouble("rotation_lock"));
        }

        if (jsonObject.has("drops")){
            PickupType drops = PickupType.valueOf(jsonObject.getString("drops"));
            turret.setDrops(drops);
        }

        return new Turret(name, center);
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public Vector getForwardUnitVector() {
        return shape.getForwardUnitVector();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public void rotate(Vector rotationVector) {
        shape.rotateShape(rotationVector);
    }

    @Override
    public void rotateBy(float angle) {
        shape.rotateBy(angle);
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        behaviourTreeRoot.process(context);

        setRequestedMovementVector(Vector.ZERO_VECTOR);
        rotate(secondsSinceLastGameTick, shape, (float) context.getValue(BKeyType.ROT_DAMP));
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.TRIANGLE;
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translateShape(new Vector(shape.getCenter(), newCenter));
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
        return health < 1;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        shape.revertToDefaultColor();
    }
}
