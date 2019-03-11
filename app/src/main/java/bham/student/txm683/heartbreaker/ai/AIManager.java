package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.utils.GameTickTimer;

import java.util.ArrayList;
import java.util.List;


public class AIManager {
    private static final String TAG = "hb::AIManager";
    private LevelState levelState;

    private List<AIEntity> controlledAI;

    private boolean update;

    private GameTickTimer timer;

    public AIManager(LevelState levelState, List<AIEntity> ais){
        this.levelState = levelState;
        this.controlledAI = new ArrayList<>();

        for (AIEntity ai : ais){
            addAI(ai);
        }

        update = true;

        //tick 10 times a second
        this.timer = new GameTickTimer(50);
        timer.start();
    }

    public void addAI(AIEntity ai){
        if (!controlledAI.contains(ai)) {
            this.controlledAI.add(ai);
            ai.setLevelState(levelState);
            Log.d("AIMANAGER", ai.getName() + " has had levelstate set");
        }
    }

    public void removeAI(AIEntity ai){
        controlledAI.remove(ai);
    }

    public void update(float secondsSinceLastGameTick){
        //if AI is turned off, return
        if (!levelState.getDebugInfo().isAIActivated())
            return;

        if (timer.tick() > 0) {
            for (AIEntity aiEntity : controlledAI) {
                aiEntity.tick(secondsSinceLastGameTick);
            }
        }

        /*if (!levelState.getDebugInfo().isAIActivated()){
            return;
        }

        Log.d(TAG, "UPDATE");
        Point centerPosition = controlledEntity.getShape().getCenter();
        path = applyAStar(centerPosition, playerPosition);

        if (path.length > 1){
            Point pathPoint = levelState.getMap().mapTileToGlobalPoint(path[1]);
            movementVector = new Vector(pathPoint.getX() - centerPosition.getX(), pathPoint.getY() - centerPosition.getY()).getUnitVector();
            controlledEntity.setMovementVector(movementVector);
            controlledEntity.tick(secondsSinceLastGameTick);
            Log.d(TAG, "Moving in direction: " + movementVector.relativeToString());
        } else {
            Log.d(TAG, "Path has length less than 1, won't tick");
        }*/
    }

    /*public Tile[] applyAStar(Point start, Point target){
        aiGraph = levelState.getMap().getAiGraph();

        //maps the start and target points to integer coordinates relative to the maptiles layout
        Tile startTile = levelState.getMap().mapGlobalPointToTile(start);
        Tile targetTile = levelState.getMap().mapGlobalPointToTile(target);

        //if the graph doesn't contain the start/target nodes, or is already at it's destination, return.
        if (!aiGraph.containsNode(startTile) || !aiGraph.containsNode(targetTile)) {
            Log.d(TAG, "Can't start AStar. Either start or target not in graph. StartNode: " + aiGraph.containsNode(startTile) + ", TargetNode: " + aiGraph.containsNode(targetTile));
            return new Tile[0];
        } else if (startTile.equals(targetTile)){
            Log.d(TAG, "Already at destination!");
            return new Tile[0];
        }

        //Wrapper class designed to hold movement costs for each node
        NodeWrapper startNode = new NodeWrapper(aiGraph.getNode(startTile));
        NodeWrapper targetNode = new NodeWrapper(aiGraph.getNode(targetTile));

        //creates a priority queue based on a NodeWrapper's fCost (Lowest at head)
        PriorityQueue<NodeWrapper> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.getfCost() < b.getfCost())
                return -1;
            else if (a.getfCost() == b.getfCost())
                return 0;
            return 1; });

        //each key is the tile coordinate of a node. It's value is the gcost spent to get to that node from the start
        HashMap<Tile, Integer> costSoFar = new HashMap<>();

        //each key is the tile coordinate of a node, it's value is the 'parent' of this node.
        //i.e. the node that comes before the key node in the path
        HashMap<Tile, Tile> cameFrom = new HashMap<>();

        startNode.setCosts(0,0,0);

        //initialise sets by adding the start node with 0 costs
        openSet.add(startNode);
        costSoFar.put(startNode.getNodeID(), 0);
        //has a value of start node so the tracePath algorithm knows when to stop backtracking
        cameFrom.put(startNode.getNodeID(), startNode.getNodeID());

        while (!openSet.isEmpty()){

            NodeWrapper current = openSet.poll();

            for (Edge<Tile> connection : current.getConnections()){
                NodeWrapper next = new NodeWrapper(connection.traverse(current));

                Log.d(TAG, "current: " + current.getNodeID() + " next: " + next.getNodeID());

                //if the next node is the target, add it to the cameFrom map and return the path generated
                //by tracePath
                if (targetNode.equals(next)){
                    Log.d(TAG, "Target Reached!");

                    cameFrom.put(next.getNodeID(), current.getNodeID());
                    return tracePath(cameFrom, targetNode.getNodeID());
                }

                //the calculated costs for the next node
                int gCostToNext = current.getgCost() + connection.getWeight();
                int hCostToNext = calculateEuclideanHeuristic(current, targetNode);
                int fCostToNext = gCostToNext + hCostToNext;

                //If the node hasn't been visited before, or the cost to get to this node is cheaper than the already stored cost
                //add it to all tracking sets
                if (!costSoFar.containsKeys(next.getNodeID()) || costSoFar.get(next.getNodeID()) > gCostToNext) {

                    next.setCosts(fCostToNext, gCostToNext, hCostToNext);

                    costSoFar.put(next.getNodeID(), gCostToNext);
                    openSet.add(next);
                    cameFrom.put(next.getNodeID(), current.getNodeID());
                }
            }
        }
        return tracePath(cameFrom, targetNode.getNodeID());
    }*/
}
