package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.TileBFS;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;

public abstract class AIEntity extends MoveableEntity {
    String TAG = "hb::" + this.getName();
    static EnemyType ENEMY_TYPE = EnemyType.CHASER;

    Tile[] path;

    LevelState levelState;

    AIBehaviour currentBehaviour;

    MoveableEntity target;

    public AIEntity(String name, Point spawnCoordinates, int size, int colorValue, float maxSpeed, LevelState levelState) {
        super(name, spawnCoordinates, ShapeIdentifier.KITE, size, size, colorValue, maxSpeed);

        this.levelState = levelState;
        this.currentBehaviour = AIBehaviour.HALTED;
    }

    public Tile[] getPath() {
        return path;
    }

    abstract void update();
    abstract void chase(MoveableEntity entityToChase);
    abstract void halt();

    public Tile[] applyAStar(String aIName, Tile startTile, Tile targetTile, int depthToPathFind){

        depthToPathFind = Math.abs(depthToPathFind);

        if (startTile.equals(targetTile)){
            Log.d(aIName, "Already at destination");
            return new Tile[0];
        }

        //creates a priority queue based on a NodeWrapper's fCost (Lowest at head)
        PriorityQueue<Pair<Tile, Float>> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (Float.compare(a.second,b.second) ==0)
                return 0;
            return 1; });

        //each key is the tile coordinate of a tile. It's value is the gcost spent to get to that tile from the start
        HashMap<Tile, Float> costSoFar = new HashMap<>();

        //each key is the tile coordinate of a tile, it's value is the 'parent' of this tile.
        //i.e. the tile that comes before the key tile in the path
        HashMap<Tile, Tile> cameFrom = new HashMap<>();

        //initialise sets by adding the start tile with 0 costs
        openSet.add(new Pair<>(startTile, 0f));
        costSoFar.put(startTile, 0f);
        //has a value of start tile so the tracePath algorithm knows when to stop backtracking
        cameFrom.put(startTile, startTile);

        while (!openSet.isEmpty()){

            Pair<Tile, Float> currentPair = openSet.poll();
            Tile currentTile = currentPair.first;
            float currentCost = currentPair.second;

            if (depthToPathFind < 1){
                return tracePath(cameFrom, currentTile);
            }

            for (Tile neighbour : TileBFS.getNeighbours(currentTile)){

                if (levelState.hasEntityAtTile(neighbour))
                    continue;

                Log.d(aIName, "current: " + currentTile.toString() + " neighbour: " + neighbour.toString());

                //if the next tile is the target, add it to the cameFrom map and return the path generated
                //by tracePath
                if (targetTile.equals(neighbour)){
                    Log.d(aIName, "Target Reached!");

                    cameFrom.put(neighbour, currentTile);
                    return tracePath(cameFrom, targetTile);
                }

                float gCostToNext;
                if (new Vector(currentTile, neighbour).getLength() > 1)
                    gCostToNext = currentCost + 1.4f;
                else
                    gCostToNext = currentCost + 1;

                //If the tile hasn't been visited before, or the cost to get to this tile is cheaper than the already stored cost
                //add it to all tracking sets
                if (!costSoFar.containsKey(neighbour) || costSoFar.get(neighbour) > gCostToNext) {

                    float fCost = gCostToNext + calculateEuclideanHeuristic(currentTile, neighbour);

                    costSoFar.put(neighbour, gCostToNext);
                    openSet.add(new Pair<>(neighbour, fCost));
                    cameFrom.put(neighbour, currentTile);
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
    public static Tile[] tracePath(HashMap<Tile, Tile> cameFrom, Tile targetNodeName){

        if (cameFrom.containsKey(targetNodeName)){
            Stack<Tile> path = new Stack<>();

            Tile previous = targetNodeName;
            Tile current = cameFrom.get(previous);

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

    private static int calculateEuclideanHeuristic(Tile currentTile, Tile targetTile){
        return (int) Math.sqrt(
                Math.pow(targetTile.getX() - currentTile.getX(), 2) +
                        Math.pow(targetTile.getY() - currentTile.getY(), 2)
        );
    }
}
