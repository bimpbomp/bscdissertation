package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Room {

    private int id;
    private Perimeter perimeter;
    private RoomGrid roomGrid;

    public Room(){

    }

    /**
     * Checks if the perimeter of the room overlaps with the entity
     * @param entity Entity to check
     * @return true if entity overlaps perimeter, false if not.
     */
    public boolean isEntityInRoom(Entity entity){
        return !CollisionManager.getPushVectorBetweenTwoShapes(perimeter, entity.getShape()).equals(new Vector());
    }
}
