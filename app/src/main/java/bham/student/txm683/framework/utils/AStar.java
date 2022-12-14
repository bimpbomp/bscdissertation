package bham.student.txm683.framework.utils;

import android.util.Log;
import bham.student.txm683.framework.ILevelState;
import bham.student.txm683.framework.ai.PathWrapper;
import bham.student.txm683.framework.ai.behaviours.BContext;
import bham.student.txm683.framework.map.MeshPolygon;
import bham.student.txm683.framework.utils.graph.Graph;
import bham.student.txm683.framework.utils.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bham.student.txm683.framework.ai.behaviours.BKeyType.*;

public class AStar {

    private Node<Integer> startNode;
    private Node<Integer> targetNode;

    private Map<Integer, MeshPolygon> meshPolygonMap;
    private Graph<Integer> meshGraph;

    private List<Point> waypointPath;

    private BContext controlledContext;

    private Point targetPoint;

    public AStar(BContext controlledContext, Map<Integer, MeshPolygon> meshPolygonMap, Graph<Integer> meshGraph) {
        this.controlledContext = controlledContext;

        if (controlledContext.containsCompulsory(CURRENT_MESH)){
            this.startNode = meshGraph.getNode(((MeshPolygon)controlledContext.getCompulsory(CURRENT_MESH)).getId());
        } else {
            this.startNode = null;
        }

        ILevelState levelState = (ILevelState) controlledContext.getCompulsory(LEVEL_STATE);
        targetPoint = (Point) controlledContext.getCompulsory(MOVE_TO);

        int targetMeshId = levelState.mapToMesh(targetPoint);

        this.targetNode = meshGraph.getNode(targetMeshId);

        if (startNode != null && targetNode != null)
            Log.d("ASTAR", "current mesh id: " + startNode.getNodeID() + ", target mesh id: " + targetNode.getNodeID());

        this.meshPolygonMap = meshPolygonMap;
        this.meshGraph = meshGraph;

        this.waypointPath = new ArrayList<>();

        reset();
    }

    private void reset(){
        this.waypointPath = new ArrayList<>();
    }

    public boolean plotPath(boolean returnIncompletePath){
        reset();

        if (startNode == null || targetNode == null){
            Log.d("ASTAR", "start/target node is null");
            return false;
        }

        Integer[] roughPath  = meshGraph.applyAStar(startNode, targetNode, returnIncompletePath, this::calculateEuclideanHeuristic).toArray(new Integer[0]);

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

            Point p;

            for (int i = 1; i < roughPath.length-1; i++){
                MeshPolygon meshPolygon = meshPolygonMap.get(roughPath[i]);

                if (meshPolygon == null){
                    Log.d("ASTAR", "null pointer in basePath for ");
                    break;
                }
                p = meshPolygon.getCenter();

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

            List<Integer> pathMeshIds = new ArrayList<>();

            for (Point point : waypointPath){

                for (MeshPolygon meshPolygon : meshPolygonMap.values() ){
                    if (meshPolygon.getBoundingBox().intersecting(point)){
                        pathMeshIds.add(meshPolygon.getId());
                        break;
                    }
                }
            }

            controlledContext.addCompulsory(PATH, new PathWrapper(waypointPath, pathMeshIds));
            return true;
        }

        return false;
    }

    public static int calculateEuclideanHeuristic(Point a, Point b){
        return (int) Math.sqrt(
                Math.pow(b.getX() - a.getX(), 2) +
                        Math.pow(b.getY() - a.getY(), 2)
        );
    }

    private int calculateEuclideanHeuristic(int currentId, int neighbourId){
        MeshPolygon currentMesh = meshPolygonMap.get(currentId);
        MeshPolygon neighbourMesh = meshPolygonMap.get(neighbourId);

        if (currentMesh != null && neighbourMesh != null){
            Point currentCenter = currentMesh.getCenter();
            Point neighbourCenter = neighbourMesh.getCenter();

            return calculateEuclideanHeuristic(currentCenter, neighbourCenter);
        }
        return Integer.MAX_VALUE;
    }
}
