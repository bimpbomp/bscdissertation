package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.composites.Selector;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.HealthMonitor;
import bham.student.txm683.heartbreaker.ai.behaviours.decorators.IsTargetVisible;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.FireAtTarget;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.FleeFromTarget;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.Idle;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.entities.Shooter;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;
import bham.student.txm683.heartbreaker.utils.graph.Node;

public class Drone extends AIEntity implements Shooter {

    private Rectangle shape;
    private int health;

    private int currentTargetNodeInPath;
    private boolean atDestination;

    private Weapon weapon;
    private BNode behaviourTreeRoot;

    public Drone(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, maxSpeed);

        shape = new Rectangle(center, size, size, colorValue);
        this.health = initialHealth;
        this.atDestination = false;
        this.currentTargetNodeInPath = 0;

        this.weapon = new BasicWeapon(getName());

        this.behaviourTreeRoot = new Selector(
                new IsTargetVisible(
                        new Selector(
                                new HealthMonitor(
                                        new FleeFromTarget()
                                ),
                                new FireAtTarget()
                        )
                ),
                new Idle()
        );
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

    @Override
    public void chase(MoveableEntity entityToChase) {
        currentBehaviour = AIBehaviour.CHASE;

        this.target = entityToChase;
    }

    @Override
    public void halt() {
        currentBehaviour = AIBehaviour.HALTED;

        setRequestedMovementVector(Vector.ZERO_VECTOR);
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        /*StringBuilder stringBuilder = new StringBuilder();
        for (Tile tile : path){
            stringBuilder.append(tile.toString());
            stringBuilder.append(" -> ");
        }
        stringBuilder.append(" END");
        Log.d("hb::"+getName(), stringBuilder.toString());*/

        /*if (atDestination) {
            path = new Tile[0];
            update();
        }

        if (path.length < 1)
            return;

            //if distance to current node is less than a certain amount, move current node to the next in path
        //if the next node is out of the length of the path, the destination is reached, stop moving
        if (calculateEuclideanHeuristic(new Tile(getCenter()), path[currentTargetNodeInPath]) < 75) {
            currentTargetNodeInPath++;

            if (currentTargetNodeInPath >= path.length){
                atDestination = true;
                setRequestedMovementVector(new Vector());
            } else {
                setRequestedMovementVector(new Vector(getCenter(), new Point(path[currentTargetNodeInPath])).getUnitVector());
            }
        }

        if (getRequestedMovementVector().equals(new Vector()))
            return;

        Vector movementVector = calculateMovementVector(secondsSinceLastGameTick);

        shape.translateShape(movementVector);
        shape.rotateShape(movementVector);*/

        BContext bContext = new BContext();
        bContext.addPair(BContext.ATTACK_TARGET_KEY, levelState.getPlayer());
        bContext.addPair(BContext.CONTROLLED_ENTITY_KEY, this);
        bContext.addPair(BContext.HEALTH_BOUND_KEY, 25);
        bContext.addPair(BContext.LEVEL_STATE_KEY, levelState);
        behaviourTreeRoot.process(bContext);

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
    public BoundingBox getRenderingVertices() {
        return shape.getRenderingVertices();
    }

    @Override
    public void setColor(int color) {
        shape.setColor(color);
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
