package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.graph.Edge;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

public abstract class AIEntity extends MoveableEntity implements Renderable{

    Tile[] path;

    LevelState levelState;

    AIBehaviour currentBehaviour;

    MoveableEntity target;

    public AIEntity(String name, float maxSpeed) {
        super(name, maxSpeed);

        this.currentBehaviour = AIBehaviour.HALTED;
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.AI_ENTITY;
    }

    public void setLevelState(LevelState levelState){
        this.levelState = levelState;
    }

    public Tile[] getPath() {
        return path;
    }

    abstract void update();
    abstract void chase(MoveableEntity entityToChase);
    abstract void halt();

    public Tile[] applyAStar(String aIName, Node<Tile> startTile, Node<Tile> targetTile, int depthToPathFind){

        depthToPathFind = Math.abs(depthToPathFind);

        if (startTile.equals(targetTile)){
            Log.d(aIName, "Already at destination");
            return new Tile[0];
        }

        //creates a priority queue based on a Tile's fCost (Lowest cost at head)
        PriorityQueue<Pair<Node<Tile>, Integer>> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second == b.second)
                return 0;
            return 1; });

        //each key is the tile coordinate of a tile. It's value is the gcost spent to get to that tile from the start
        HashMap<Node<Tile>, Integer> costSoFar = new HashMap<>();

        //each key is the tile coordinate of a tile, it's value is the 'parent' of this tile.
        //i.e. the tile that comes before the key tile in the path
        HashMap<Node<Tile>, Node<Tile>> cameFrom = new HashMap<>();

        //initialise sets by adding the start tile with 0 costs
        openSet.add(new Pair<>(startTile, 0));
        costSoFar.put(startTile, 0);

        //has a value of null so the tracePath algorithm knows when to stop backtracking
        cameFrom.put(startTile, null);

        while (!openSet.isEmpty()){

            Pair<Node<Tile>, Integer> currentPair = openSet.poll();
            Node<Tile> currentNode = currentPair.first;
            int currentCost = currentPair.second;

            for (Edge<Tile> connection : currentNode.getConnections()){

                Node<Tile> neighbour = connection.traverse(currentNode);

                Log.d(aIName, "current: " + currentNode.getNodeID().toString() + " neighbour: " + neighbour.getNodeID().toString());

                //if the next tile is the target, add it to the cameFrom map and return the path generated
                //by tracePath
                if (targetTile.equals(neighbour)){
                    Log.d(aIName, "Target Reached!");

                    cameFrom.put(neighbour, currentNode);
                    return tracePath(cameFrom, targetTile);
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
            depthToPathFind--;
        }
        return tracePath(cameFrom, targetTile);
    }

    /**
     * Constructs a path for the AI to take to get to it's target
     * @param cameFrom A map containing the visited nodes, and their parents
     * @param targetNodeName The name of the targeted node (the destination)
     * @return A Tile array containing the path to take, in order
     */
    public static Tile[] tracePath(HashMap<Node<Tile>, Node<Tile>> cameFrom, Node<Tile> targetNodeName){

        /*StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-------START-------");
        for (Node<Tile> node : cameFrom.keySet()){
            stringBuilder.append(node.getNodeID());
            stringBuilder.append(" came from " );
            if (cameFrom.get(node) != null)
                stringBuilder.append(cameFrom.get(node).getNodeID());
            else
                stringBuilder.append("NULL");
            stringBuilder.append("\n");
        }
        stringBuilder.append("-------END-------");
        Log.d("hb:::", stringBuilder.toString());
        return new Tile[0];*/

        if (cameFrom.containsKey(targetNodeName)){
            Stack<Tile> path = new Stack<>();

            Node<Tile> previous = targetNodeName;
            Node<Tile> current = cameFrom.get(targetNodeName);

            path.push(previous.getNodeID());

            while (current != null){
                Log.d("hb::TRACEPATH", current.getNodeID() + ", prev: " + previous.getNodeID());
                path.push(current.getNodeID());
                previous = current;
                try {
                    current = cameFrom.get(previous);
                } catch (NullPointerException e){
                    return new Tile[0];
                }
            }

            ArrayList<Tile> pathArray = new ArrayList<>();

            while (!path.empty()){
                Tile nextStep = path.pop();

                pathArray.add(nextStep);
            }
            return pathArray.toArray(new Tile[0]);

        } else {
            Log.d("hb::TileBFS", "tracePath: target tile not found in path");
            return new Tile[0];
        }
    }

    public static int calculateEuclideanHeuristic(Tile currentTile, Tile targetTile){
        return (int) Math.sqrt(
                Math.pow(targetTile.getX() - currentTile.getX(), 2) +
                        Math.pow(targetTile.getY() - currentTile.getY(), 2)
        );
    }
}
