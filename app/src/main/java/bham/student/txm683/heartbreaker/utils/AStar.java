package bham.student.txm683.heartbreaker.utils;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.utils.graph.Edge;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.*;

import static bham.student.txm683.heartbreaker.ai.behaviours.Status.FAILURE;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.SUCCESS;

public class AStar {

    private Node<Integer> startNode;
    private Node<Integer> targetNode;

    private Map<Integer, MeshPolygon> meshPolygonMap;
    private Graph<Integer> meshGraph;

    //creates a priority queue based on a Tile's fCost (Lowest cost at head)
    private PriorityQueue<Pair<Node<Integer>, Integer>> openSet;

    //each key is the tile coordinate of a tile. It's value is the gcost spent to get to that tile from the start
    private Map<Node<Integer>, Integer> costSoFar;

    //each key is the tile coordinate of a tile, it's value is the 'parent' of this tile.
    //i.e. the tile that comes before the key tile in the path
    private Map<Node<Integer>, Node<Integer>> cameFrom;

    private List<Point> waypointPath;

    private AIEntity controlled;

    public AStar(AIEntity controlled, int startMeshId, int targetMeshId, Map<Integer, MeshPolygon> meshPolygonMap, Graph<Integer> meshGraph) {
        this.controlled = controlled;

        this.startNode = meshGraph.getNode(startMeshId);
        this.targetNode = meshGraph.getNode(targetMeshId);

        this.meshPolygonMap = meshPolygonMap;
        this.meshGraph = meshGraph;

        this.waypointPath = new ArrayList<>();

        openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
                return 0;
            return 1; });

        reset();
    }

    private void reset(){
        this.openSet.clear();
        this.cameFrom = new HashMap<>();
        this.costSoFar = new HashMap<>();
        this.waypointPath = new ArrayList<>();
    }

    public int plotRoughPath(){
        reset();
        Status PathFindingStatus = applyAStar();

        int returnStatus;

        Integer[] roughPath;

        if (PathFindingStatus == SUCCESS)
            roughPath = PathFinding.tracePath(PathFinding.formPathStack(cameFrom, targetNode));
        else
            roughPath = new Integer[0];

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        if (roughPath.length == 1){
            stringBuilder.append("ALREADY AT DESTINATION");
            returnStatus = 0;
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
                    return -1;
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

            returnStatus = 1;
        } else {
            stringBuilder.append("NO NODES IN PATH");

            returnStatus = -1;
        }

        Log.d("ASTAR", "status: " + PathFindingStatus + ", nodes: " + stringBuilder.toString());

        if (waypointPath.size() > 0 ){
            Log.d("ASTAR", sb2.toString());
        }

        return returnStatus;
    }

    public List<Point> getPath(){
        return waypointPath;
    }

    private Status applyAStar(){

        //initialise sets by adding the start tile with 0 costs
        openSet.add(new Pair<>(startNode, 0));
        costSoFar.put(startNode, 0);

        //has a value of null so the tracePath algorithm knows when to stop backtracking
        cameFrom.put(startNode, null);

        if (startNode.equals(targetNode)){
            Log.d(controlled.getName(), "Already at destination");
            return SUCCESS;
        }

        while (!openSet.isEmpty()){

            Pair<Node<Integer>, Integer> currentPair = openSet.poll();
            Node<Integer> currentNode = currentPair.first;
            int currentCost = currentPair.second;

            for (Edge<Integer> connection : currentNode.getConnections()){

                Node<Integer> neighbour = connection.traverse();

                Log.d(controlled.getName(), "current: " + currentNode.getNodeID().toString() + " neighbour: " + neighbour.getNodeID().toString());

                //if the next tile is the target, add it to the cameFrom map and return the path generated
                //by tracePath
                if (targetNode.equals(neighbour)){
                    Log.d(controlled.getName(), "Target Reached!");

                    cameFrom.put(neighbour, currentNode);
                    return SUCCESS;
                }

                int gCostToNext = currentCost + connection.getWeight();

                //If the tile hasn't been visited before, or the cost to get to this tile is cheaper than the already stored cost
                //add it to all tracking sets
                if (!costSoFar.containsKey(neighbour) || costSoFar.get(neighbour) > gCostToNext) {

                    int fCost = gCostToNext + calculateEuclideanHeuristic(currentNode.getNodeID(), neighbour.getNodeID());

                    costSoFar.put(neighbour, gCostToNext);
                    openSet.add(new Pair<>(neighbour, fCost));
                    cameFrom.put(neighbour, currentNode);
                }
            }
        }
        return FAILURE;
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
