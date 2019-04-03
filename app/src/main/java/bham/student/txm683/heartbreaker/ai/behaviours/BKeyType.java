package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public enum BKeyType {
    MOVE_TO (Point.class),
    CONTROLLED_ENTITY(AIEntity.class),
    LEVEL_STATE (LevelState.class),
    FRIENDLY_BLOCKING_SIGHT (Boolean.class),
    SIGHT_BLOCKED (Boolean.class),
    SIGHT_VECTOR (Vector.class),
    CURRENT_MESH (MeshPolygon.class),
    TARGET (Point.class),
    PATH (PathWrapper.class);

    private Class type;

    BKeyType(Class type) {
        this.type = type;
    }

    public Class getType() {
        return type;
    }
}