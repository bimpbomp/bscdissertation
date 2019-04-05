package bham.student.txm683.framework.ai.behaviours;

import bham.student.txm683.framework.ILevelState;
import bham.student.txm683.framework.ai.IAIEntity;
import bham.student.txm683.framework.ai.PathWrapper;
import bham.student.txm683.framework.map.MeshPolygon;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;

public enum BKeyType {
    MOVE_TO (Point.class),
    CONTROLLED_ENTITY(IAIEntity.class),
    LEVEL_STATE (ILevelState.class),
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