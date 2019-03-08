package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.IsTargetVisible;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.FireAtTarget;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Idle;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.entities.Shooter;
import bham.student.txm683.heartbreaker.entities.entityshapes.Kite;
import bham.student.txm683.heartbreaker.entities.entityshapes.Polygon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;
import bham.student.txm683.heartbreaker.utils.graph.Node;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Drone extends AIEntity implements Shooter {

    private Kite shape;
    private int health;

    private int currentTargetNodeInPath;
    private boolean atDestination;

    private Weapon weapon;
    private BNode behaviourTreeRoot;


    public Drone(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, maxSpeed);

        List<Vector> vertices = Polygon.createTriangle(center, size*0.9f, size * 0.75f);

        this.shape = new Kite(center, new Vector[]{
                vertices.get(0),
                vertices.get(1),
                new Vector(center, center.add(new Point(0, 0.5f * size))),
                vertices.get(2)
        }, colorValue, colorValue);

        this.health = initialHealth;
        this.atDestination = false;
        this.currentTargetNodeInPath = 0;

        this.weapon = new BasicWeapon(getName(), 7);

        this.behaviourTreeRoot = new Selector(
                new IsTargetVisible(
                        new Selector(

                                new FireAtTarget()
                        )
                ),
                new Idle()
        );


        context = new BContext();
        context.addPair(BContext.VIEW_RANGE, 600);
    }

    public Drone(String name, Point center){
        this(name, center, 100, ColorScheme.CHASER_COLOR, 300, 100);
    }

    public static Drone build(JSONObject jsonObject) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("center"));
        String name = jsonObject.getString("name");
        return new Drone(name, center);
    }

    @Override
    public void rotate(Vector rotationVector) {
        shape.rotateShape(rotationVector);
    }

    @Override
    public void rotateBy(float angle) {
        shape.rotateBy(angle);
    }

    public void update() {
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
    }

    public void setLevelState(LevelState levelState){
        this.levelState = levelState;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        context.addPair(BContext.ATTACK_TARGET, levelState.getPlayer());
        context.addPair(BContext.HOST_ENTITY, this);
        context.addPair(BContext.HEALTH_BOUND, 25);
        context.addPair(BContext.LEVEL_STATE, levelState);
        behaviourTreeRoot.process(context);

        if (getRequestedMovementVector().equals(new Vector()))
            return;

        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        shape.translateShape(movementVector);
        shape.rotateShape(movementVector);
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
