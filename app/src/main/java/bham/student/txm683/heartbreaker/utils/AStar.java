package bham.student.txm683.heartbreaker.utils;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.PathWrapper;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

public class AStar {

    private Node<Integer> startNode;
    private Node<Integer> targetNode;

    private Map<Integer, MeshPolygon> meshPolygonMap;
    private Graph<Integer> meshGraph;

    private List<Point> waypointPath;

    private AIEntity controlled;

    private Point targetPoint;

    public AStar(AIEntity controlled, Map<Integer, MeshPolygon> meshPolygonMap, Graph<Integer> meshGraph) {
        this.controlled = controlled;

        this.startNode = meshGraph.getNode(((MeshPolygon)controlled.getContext().getValue(CURRENT_MESH)).getId());

        LevelState levelState = (LevelState) controlled.getContext().getValue(LEVEL_STATE);
        targetPoint = (Point) controlled.getContext().getValue(MOVE_TO);

        int targetMeshId = levelState.mapToMesh(targetPoint);

        this.targetNode = meshGraph.getNode(targetMeshId);

        Log.d("ASTAR", "current mesh id: " + startNode.getNodeID() + ", target mesh id: " + targetNode.getNodeID());

        this.meshPolygonMap = meshPolygonMap;
        this.meshGraph = meshGraph;

        this.waypointPath = new ArrayList<>();

        reset();
    }

    private void reset(){
        this.waypointPath = new ArrayList<>();
    }

    public boolean plotPath(){
        reset();

        if (startNode == null || targetNode == null){
            Log.d("ASTAR", "start/target node is null for " + controlled.getName());
            return false;
        }

        Integer[] roughPath  = meshGraph.applyAStar(startNode, targetNode, this::calculateEuclideanHeuristic).toArray(new Integer[0]);

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        if (roughPath.length == 1){
            stringBuilder.append("ALREADY AT DESTINATION");
        }else if (roughPath.length > 1) {
            for (int meshId : roughPath) {
                stringBuilder.append(meshId);
                stringBuilder.append(", ");
            }
            stringBuilder.append("END");

            Point p = controlled.getCenter();
            //Point p = new Point(100,100);

            for (int i = 1; i < roughPath.length; i++){
                MeshPolygon meshPolygon = meshPolygonMap.get(roughPath[i]);

                if (meshPolygon == null){
                    Log.d("ASTAR", "null pointer in path for " + controlled.getName());
                    break;
                }

                p = meshPolygon.getNearestPoint(p, controlled.getWidth());

                waypointPath.add(p);
            }


            sb2.append("WAYPOINT PATH: ");
            for (Point point : waypointPath){
                sb2.append(point.toString());
                sb2.append(", ");
            }
            sb2.append("END");

        } else {
            stringBuilder.append("NO NODES IN PATH");
        }

        if (waypointPath.size() > 0 ){
            waypointPath.add(targetPoint);
            Log.d("ASTAR", sb2.toString());
            controlled.getContext().addPair(PATH, new PathWrapper(waypointPath));
            return true;
        }

        return false;
    }

    private int calculateEuclideanHeuristic(int currentId, int neighbourId){
        MeshPolygon currentMesh = meshPolygonMap.get(currentId);
        MeshPolygon neighbourMesh = meshPolygonMap.get(neighbourId);

        if (currentMesh != null && neighbourMesh != null){
            Point currentCenter = currentMesh.getCenter();
            Point neighbourCenter = neighbourMesh.getCenter();

            return (int) Math.sqrt(
                    Math.pow(neighbourCenter.getX() - currentCenter.getX(), 2) +
                            Math.pow(neighbourCenter.getY() - currentCenter.getY(), 2)
            );
        }
        return Integer.MAX_VALUE;
    }
}
