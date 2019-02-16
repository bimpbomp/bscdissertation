package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;
import bham.student.txm683.heartbreaker.utils.graph.Node;

public class Chaser extends AIEntity implements Damageable, Collidable {

    private Rectangle shape;
    private int health;

    private int currentTargetNodeInPath;
    private boolean atDestination;

    public Chaser(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, maxSpeed);

        shape = new Rectangle(center, size, size, colorValue);
        this.health = initialHealth;
        this.atDestination = false;
        this.currentTargetNodeInPath = 0;
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
        /*Log.d(getName(), "updating ai");
        switch (currentBehaviour) {

            case CHASE:

                if (levelState.inSameRoom(this, target)){
                    Log.d(getName(), "in same room");
                    path = applyAStar(getName(), levelState.getMap().mapGlobalPointToTile(getCenter()),
                            levelState.getMap().mapGlobalPointToTile(target.getCenter()), 10);
                    if (path.length > 1){
                        setRequestedMovementVector(new Vector(getCenter(),
                                levelState.getMap().mapTileToGlobalPoint(path[1])).getUnitVector());
                    } else {
                        Log.d(getName(), "Path length of less than 2");
                        setRequestedMovementVector(Vector.ZERO_VECTOR);
                    }

                }*//* else {
                    Log.d(getName(), "not in same room");
                    RoomNode[] roomPath = levelState.getMap().getRoomGraph().pathToRoom(this.getRoomID(), target.getRoomID());
                    if (roomPath.length > 1){
                        Log.d(getName(), roomPath[0] + " -> " + roomPath[1]);
                        Point door = levelState.getMap().getRoomGraph().getDoorBetweenRooms(getRoomID(), roomPath[1].getRoom().getId()).getSpawnCoordinates();
                        Vector movementVector = new Vector(getCenter(),door).getUnitVector();

                        setRequestedMovementVector(movementVector);
                    } else {
                        if (roomPath.length == 1){
                            Log.d(getName(), roomPath[0]+"");
                        }
                        setRequestedMovementVector(Vector.ZERO_VECTOR);
                    }
                }*//*
                break;
            case HUNT:
                break;
            case HALTED:
                break;
        }*/


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

        if (atDestination) {
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
        shape.rotateShape(movementVector);
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
