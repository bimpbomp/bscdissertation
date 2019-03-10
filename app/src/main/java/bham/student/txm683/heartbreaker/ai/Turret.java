package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Tasks;
import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTriangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Turret extends AIEntity {

    private int health;
    private IsoscelesTriangle shape;

    private int width;

    private Weapon weapon;

    private BNode behaviourTreeRoot;

    public Turret(String name, Point center, int size, int colorValue, int initialHealth) {
        super(name, 0);

        shape = new IsoscelesTriangle(center, Polygon.createTriangle(center, size, size).toArray(new Vector[0]), colorValue);

        health = initialHealth;

        this.weapon=  new BasicWeapon(name, 10, 30);

        this.width = size;

        this.behaviourTreeRoot =
                Tasks.checkLineOfSight();

        context.addPair(BKeyType.VIEW_RANGE, 600);
        context.addPair(BKeyType.CONTROLLED_ENTITY, this);
        context.addPair(BKeyType.TIME_PER_IDLE, 25);
    }

    public Turret(String name, Point center){
        this(name ,center, 100, ColorScheme.CHASER_COLOR, 100);
    }

    public static Turret build(JSONObject jsonObject) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("center"));
        String name = jsonObject.getString("name");
        return new Turret(name, center);
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

        /*if (context.containsKeys(SIGHT_BLOCKED) && context.getValue(SIGHT_BLOCKED) instanceof  Boolean){
            boolean sightBlocked = (boolean) context.getValue(SIGHT_BLOCKED);

            if (!sightBlocked){
                rotate(new Vector(getCenter(), levelState.getPlayer().getCenter()));
                levelState.addBullet(weapon.shoot(getForwardUnitVector()));
            }
        }*/
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
