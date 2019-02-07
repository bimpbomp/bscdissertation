package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.util.Log;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Chaser extends AIEntity implements Damageable, Collidable {

    private Rectangle shape;
    private int health;

    public Chaser(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, center, maxSpeed);

        shape = new Rectangle(center, size, size, colorValue);
        this.health = initialHealth;
    }

    @Override
    public void update() {
        Log.d(getName(), "updating ai");
        switch (currentBehaviour) {

            case CHASE:

                if (levelState.inSameRoom(this, target)){
                    Log.d(getName(), "in same room");
                    path = applyAStar(getName(), levelState.getMap().mapGlobalPointToTile(getPosition()),
                            levelState.getMap().mapGlobalPointToTile(target.getPosition()), 10);
                    if (path.length > 1){
                        setRequestedMovementVector(new Vector(getPosition(),
                                levelState.getMap().mapTileToGlobalPoint(path[1])).getUnitVector());
                    } else {
                        Log.d(getName(), "Path length of less than 2");
                        setRequestedMovementVector(new Vector());
                    }

                }/* else {
                    Log.d(getName(), "not in same room");
                    RoomNode[] roomPath = levelState.getMap().getRoomGraph().pathToRoom(this.getRoomID(), target.getRoomID());
                    if (roomPath.length > 1){
                        Log.d(getName(), roomPath[0] + " -> " + roomPath[1]);
                        Point door = levelState.getMap().getRoomGraph().getDoorBetweenRooms(getRoomID(), roomPath[1].getRoom().getId()).getSpawnCoordinates();
                        Vector movementVector = new Vector(getPosition(),door).getUnitVector();

                        setRequestedMovementVector(movementVector);
                    } else {
                        if (roomPath.length == 1){
                            Log.d(getName(), roomPath[0]+"");
                        }
                        setRequestedMovementVector(new Vector());
                    }
                }*/
                break;
            case HUNT:
                break;
            case HALTED:
                break;
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

        setRequestedMovementVector(new Vector());
    }

    @Override
    public void move(float secondsSinceLastGameTick) {
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
        return health < 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, interpolationVector, renderEntityName);

        if (renderEntityName)
            drawName(canvas, renderOffset);
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
}
