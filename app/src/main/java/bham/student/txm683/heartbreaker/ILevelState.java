package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

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
