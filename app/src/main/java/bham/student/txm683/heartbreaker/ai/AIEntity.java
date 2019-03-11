package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class AIEntity extends MoveableEntity implements Renderable, Damageable {

    Tile[] path;

    LevelState levelState;

    BContext context;

    public AIEntity(String name, float maxSpeed) {
        super(name, maxSpeed);
        context = new BContext();
    }

    public abstract Weapon getWeapon();

    public BContext getContext() {
        return context;
    }

    public abstract Vector getForwardUnitVector();

    public abstract void rotate(Vector rotationVector);

    public abstract void rotateBy(float angle);

    public abstract int getWidth();

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.AI_ENTITY;
    }

    public void setLevelState(LevelState levelState){
        Log.d("AIENTITY", getName() + " has levelState added");

        this.levelState = levelState;
        this.context.addPair(BKeyType.LEVEL_STATE, levelState);
    }

    public Tile[] getPath() {
        return path;
    }

    public void setPath(Tile[] path) {
        this.path = path;
    }



    /*public Tile[] applyAStar(String aIName, Node<Tile> startTile, Node<Tile> targetTile, int depthToPathFind){

        depthToPathFind = Math.abs(depthToPathFind);

        if (startTile.equals(targetTile)){
            Log.d(aIName, "Already at destination");
            return new Tile[0];
        }

        //creates a priority queue based on a Tile's fCost (Lowest cost at head)
        PriorityQueue<Pair<Node<Tile>, Integer>> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
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

                Node<Tile> neighbour = connection.traverse();

                Log.d(aIName, "current: " + currentNode.getNodeID().toString() + " neighbour: " + neighbour.getNodeID().toString());

                //if the next tile is the target, add it to the cameFrom map and return the path generated
                //by tracePath
                if (targetTile.equals(neighbour)){
                    Log.d(aIName, "Target Reached!");

                    cameFrom.put(neighbour, currentNode);
                    return PathFinding.tracePath(PathFinding.formPathStack(cameFrom, targetTile));
                }

                int gCostToNext = currentCost + connection.getWeight();

                //If the tile hasn't been visited before, or the cost to get to this tile is cheaper than the already stored cost
                //add it to all tracking sets
                if (!costSoFar.containsKeys(neighbour) || costSoFar.get(neighbour) > gCostToNext) {

                    int fCost = gCostToNext + calculateEuclideanHeuristic(currentNode.getNodeID(), neighbour.getNodeID());

                    costSoFar.put(neighbour, gCostToNext);
                    openSet.add(new Pair<>(neighbour, fCost));
                    cameFrom.put(neighbour, currentNode);
                }
            }
            depthToPathFind--;
        }
        return PathFinding.tracePath(PathFinding.formPathStack(cameFrom, targetTile));
    }*/

    /*public static int calculateEuclideanHeuristic(Tile currentTile, Tile targetTile){
        return (int) Math.sqrt(
                Math.pow(targetTile.getX() - currentTile.getX(), 2) +
                        Math.pow(targetTile.getY() - currentTile.getY(), 2)
        );
    }*/

    /*public static Node<Tile> getClosestNode(Tile tile, Graph<Tile> graph){
        int smallestDistance = Integer.MAX_VALUE;

        List<Node<Tile>> nodes = graph.getNodes();

        if (nodes.size() == 0)
            throw new IllegalArgumentException("Graph has no nodes, cannot compute smallest distance");

        if (tile == null)
            throw new IllegalArgumentException("Given Tile is null");

        Node<Tile> closestNode = nodes.get(0);
        int currentDistance;

        for (Node<Tile> node : graph.getNodes()){
            currentDistance = calculateEuclideanHeuristic(node.getNodeID(), tile);

            if (currentDistance < smallestDistance){
                closestNode = node;
                smallestDistance = currentDistance;
            }
        }

        return closestNode;
    }*/
}
