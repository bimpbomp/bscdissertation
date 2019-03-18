package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public enum BKeyType {
    MOVE_TO (Point.class),
    CONTROLLED_ENTITY(AIEntity.class),
    FLEE_FROM (Entity.class),
    HEALTH_BOUND (Integer.class),
    ATTACK_TARGET (MoveableEntity.class),
    LEVEL_STATE (LevelState.class),
    VIEW_RANGE (Integer.class),
    FRIENDLY_BLOCKING_SIGHT (Boolean.class),
    SIGHT_BLOCKED (Boolean.class),
    SIGHT_VECTOR (Vector.class),
    CURRENT_MESH (MeshPolygon.class),
    TARGET (Point.class),
    TIME_PER_IDLE (Integer.class),
    TIME_LEFT_IN_IDLE (Integer.class),
    PATH (PathWrapper.class),
    COLLISION_MANAGER (CollisionManager.class),
    ROT_DAMP (Float.class);

    /*
    //TODO change BKeyType to these keys

    LEVEL_STATE (LevelState.class),
    OVERLORD (),
    CONTROLLED_ENTITY (AIEntity.class),
    SIGHT_BLOCKED (Boolean.class),
    FRIENDLY_BLOCKING_SIGHT (Boolean.class),
    ROTATE_TO (Point.class),
    MOVE_TO (Point.class),
    CURRENT_MESH (MeshPolygon.class),
    COLLISION_MANAGER (CollisionManager.class);

    */

    private Class type;

    BKeyType(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
