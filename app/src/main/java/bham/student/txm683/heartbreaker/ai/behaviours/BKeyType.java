package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.utils.Point;

public enum BKeyType {
    MOVE_TO (Point.class),
    CONTROLLED_ENTITY(AIEntity.class),
    FLEE_FROM (Entity.class),
    HEALTH_BOUND (Integer.class),
    ATTACK_TARGET (Entity.class),
    LEVEL_STATE (LevelState.class),
    VIEW_RANGE (Integer.class),
    SIGHT_BLOCKED (Boolean.class),
    CURRENT_MESH (MeshPolygon.class),
    TARGET (Point.class),
    TIME_PER_IDLE (Integer.class),
    TIME_LEFT_IN_IDLE (Integer.class),
    PATH (PathWrapper.class);

    private Class type;

    BKeyType(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}
