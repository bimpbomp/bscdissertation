package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomNode;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Chaser extends AIEntity {

    public Chaser(String name, Point spawnCoordinates, int size, int colorValue, float maxSpeed, LevelState levelState) {
        super(name, spawnCoordinates, size, colorValue, maxSpeed, levelState);
    }

    @Override
    public void update() {
        Log.d(TAG, "updating ai");
        switch (currentBehaviour) {

            case CHASE:

                if (levelState.inSameRoom(this, target)){
                    Log.d(TAG, "in same room");
                    path = applyAStar(getName(), levelState.getMap().mapGlobalPointToTile(getShape().getCenter()),
                            levelState.getMap().mapGlobalPointToTile(target.getShape().getCenter()), 10);
                    if (path.length > 1){
                        setMovementVector(new Vector(getShape().getCenter(),
                                levelState.getMap().mapTileToGlobalPoint(path[1])).getUnitVector());
                    } else {
                        Log.d(TAG, "Path length of less than 2");
                        setMovementVector(new Vector());
                    }

                } else {
                    Log.d(TAG, "not in same room");
                    RoomNode[] roomPath = levelState.getMap().getRoomGraph().pathToRoom(this.getRoomID(), target.getRoomID());
                    if (roomPath.length > 1){
                        Log.d(TAG, roomPath[0] + " -> " + roomPath[1]);
                        Point door = levelState.getMap().getRoomGraph().getDoorBetweenRooms(getRoomID(), roomPath[1].getRoom().getId()).getSpawnCoordinates();
                        Vector movementVector = new Vector(getShape().getCenter(),door).getUnitVector();

                        setMovementVector(movementVector);
                    } else {
                        if (roomPath.length == 1){
                            Log.d(TAG, roomPath[0]+"");
                        }
                        setMovementVector(new Vector());
                    }
                }
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

        setMovementVector(new Vector());
    }
}
