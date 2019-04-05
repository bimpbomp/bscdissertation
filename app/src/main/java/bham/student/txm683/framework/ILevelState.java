package bham.student.txm683.framework;

import bham.student.txm683.framework.entities.MoveableEntity;
import bham.student.txm683.framework.entities.Projectile;
import bham.student.txm683.framework.map.MeshPolygon;
import bham.student.txm683.framework.physics.Collidable;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.graph.Graph;

import java.util.List;
import java.util.Map;

public interface ILevelState {
    int mapToMesh(Point p);
    Map<Integer, MeshPolygon> getRootMeshPolygons();
    Graph<Integer> getMeshGraph();
    void addBullet(Projectile[] projectiles);
    MoveableEntity getPlayer();
    List<Collidable> getAvoidables();
}
