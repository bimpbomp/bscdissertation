package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

import java.util.List;
import java.util.Map;

public interface IMap {

    String getName();
    String getStage();

    int getTileSize();

    void setDoors(List<Door> doors);
    Map<String, Door> getDoors();

    void setRootMeshPolygons(List<MeshPolygon> meshPolygons);
    Map<Integer, MeshPolygon> getRootMeshPolygons();

    void setWidthInTiles(int w);
    float getWidth();

    void setHeightInTiles(int h);
    float getHeight();

    void setMeshGraph(Graph<Integer> graph);
    Graph<Integer> getMeshGraph();

    void setWalls(List<Wall> walls);
    List<Wall> getWalls();
}
