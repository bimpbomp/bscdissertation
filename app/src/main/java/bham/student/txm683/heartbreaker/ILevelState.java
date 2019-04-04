package bham.student.txm683.heartbreaker;

import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.HashMap;
import java.util.List;

public interface ILevelState {
    int mapToMesh(Point p);
    HashMap<Integer, MeshPolygon> getRootMeshPolygons();
    Graph<Integer> getMeshGraph();
    void addBullet(Projectile[] projectiles);
    MoveableEntity getPlayer();
    List<Collidable> getAvoidables();
}
