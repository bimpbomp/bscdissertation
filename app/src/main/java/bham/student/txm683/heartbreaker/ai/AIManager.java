package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;
import bham.student.txm683.heartbreaker.utils.graph.Edge;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;
import bham.student.txm683.heartbreaker.utils.graph.NodeWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;


public class AIManager {
    private static final String TAG = "hb::AIManager";

    private MoveableEntity controlledEntity;
    private LevelState levelState;
    private Graph aiGraph;
    private Vector movementVector;

    public AIManager(MoveableEntity controlledEntity, LevelState levelState){
        this.controlledEntity = controlledEntity;
        this.levelState = levelState;
    }

    public void update(float secondsSinceLastGameTick, Point playerPosition){
        Log.d(TAG, "UPDATE");
        Point centerPosition = controlledEntity.getShape().getCenter();
        Tile[] path = applyAStar(centerPosition, playerPosition);

        if (path.length > 1){
            Point pathPoint = levelState.getMap().mapTileToGlobalPoint(path[1]);
            movementVector = new Vector(pathPoint.getX() - centerPosition.getX(), pathPoint.getY() - centerPosition.getY()).getUnitVector();
            controlledEntity.setMovementVector(movementVector);
            controlledEntity.move(secondsSinceLastGameTick);
            Log.d(TAG, "Moving in direction: " + movementVector.relativeToString());
        } else {
            Log.d(TAG, "Path has length less than 1, won't move");
        }
    }

    public Tile[] applyAStar(Point start, Point target){
        aiGraph = levelState.getMap().getAiGraph();

        Tile startTile = levelState.getMap().mapGlobalPointToTile(start);
        Tile targetTile = levelState.getMap().mapGlobalPointToTile(target);

        /*Tile startTile = new Tile(1,1);
        Tile targetTile = new Tile(4,1);*/

        //if the graph doesn't contain the start/target nodes, return.
        if (!aiGraph.containsNode(startTile) || !aiGraph.containsNode(targetTile)) {
            Log.d(TAG, "Can't start AStar. Either start or target not in graph. StartNode: " + aiGraph.containsNode(startTile) + ", TargetNode: " + aiGraph.containsNode(targetTile));
            return new Tile[0];
        } else if (startTile.equals(targetTile)){
            Log.d(TAG, "Already at destination!");
            return new Tile[0];
        }

        NodeWrapper startNode = new NodeWrapper(aiGraph.getNode(startTile));
        NodeWrapper targetNode = new NodeWrapper(aiGraph.getNode(targetTile));

        //creates a priority queue based on a NodeWrapper's fCost (Lowest at head)
        PriorityQueue<NodeWrapper> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.getfCost() < b.getfCost())
                return -1;
            else if (a.getfCost() == b.getfCost())
                return 0;
            return 1; });

        HashMap<String, Integer> costSoFar = new HashMap<>();

        HashMap<String, String> cameFrom = new HashMap<>();

        startNode.setCosts(0,0,0);

        openSet.add(startNode);
        costSoFar.put(startNode.getName(), 0);
        cameFrom.put(startNode.getName(), startNode.getName());

        while (!openSet.isEmpty()){

            NodeWrapper current = openSet.poll();

            for (Edge connection : current.getConnections()){
                NodeWrapper next = new NodeWrapper(connection.traverse(current));

                Log.d(TAG, "current: " + current.getName() + " next: " + next.getName());

                if (targetNode.equals(next)){
                    Log.d(TAG, "Target Reached!");

                    cameFrom.put(next.getName(), current.getName());
                    return tracePath(cameFrom, targetNode.getName());
                }

                int gCostToNext = current.getgCost() + connection.getWeight();
                int hCostToNext = calculateEuclideanHeuristic(current, targetNode);
                int fCostToNext = gCostToNext + hCostToNext;

                if (!costSoFar.containsKey(next.getName()) || costSoFar.get(next.getName()) > gCostToNext) {

                    next.setCosts(fCostToNext, gCostToNext, hCostToNext);

                    costSoFar.put(next.getName(), gCostToNext);
                    openSet.add(next);
                    cameFrom.put(next.getName(), current.getName());
                }
            }
        }
        return tracePath(cameFrom, targetNode.getName());
    }


    private Tile[] tracePath(HashMap<String, String> cameFrom, String targetNodeName){

        if (cameFrom.containsKey(targetNodeName)){
            Stack<String> path = new Stack<>();

            String previous = targetNodeName;
            String current = cameFrom.get(previous);

            path.push(previous);

            while (!current.equals(previous)){

                path.push(current);
                previous = current;
                try {
                    current = cameFrom.get(previous);
                } catch (NullPointerException e){
                    return new Tile[0];
                }
            }

            StringBuilder pathString = new StringBuilder();
            ArrayList<Tile> pathArray = new ArrayList<>();

            while (!path.empty()){
                String nextStep = path.pop();
                pathString.append(nextStep);
                pathString.append(" -> ");

                pathArray.add(new Tile(nextStep));
            }
            int i = pathString.lastIndexOf(" -> ");
            pathString.setLength(pathString.length()- (pathString.length()- i));

            Log.d(TAG, "PATH: " + pathString.toString());
            return pathArray.toArray(new Tile[0]);

        } else {
            Log.d(TAG, "target node " + targetNodeName + " not in provided map in tracePath");
            return new Tile[0];
        }
    }

    private int calculateEuclideanHeuristic(Node currentNode, Node targetNode){
        return (int) Math.sqrt(
                Math.pow(targetNode.getX() - currentNode.getX(), 2) +
                Math.pow(targetNode.getY() - currentNode.getY(), 2)
        );
    }
}