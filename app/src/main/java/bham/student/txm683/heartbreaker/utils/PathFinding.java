package bham.student.txm683.heartbreaker.utils;

import android.util.Log;
import bham.student.txm683.heartbreaker.TileSet;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.*;

public class PathFinding {

    private PathFinding(){

    }

    public static List<Tile> bfsFlee(TileSet tileSet, Point startPoint){
        HashMap<Tile, Tile> cameFrom = new HashMap<>();
        Queue<Tile> openSet = new LinkedList<>();
        Set<Tile> closedSet = new HashSet<>();

        Tile centerOffset = new Tile(tileSet.getTileSize()/2, tileSet.getTileSize()/2);

        Tile start = Tile.mapToTile(startPoint, tileSet.getTileSize());
        openSet.add(start);

        cameFrom.put(start.add(centerOffset), null);

        Tile currentTile;
        while(!openSet.isEmpty()){

            currentTile = openSet.poll();
            closedSet.add(currentTile);

            for (Tile neighbour : getNeighbours(currentTile, tileSet)){

                if (!tileSet.tileIsVisibleToPlayer(neighbour)){
                    cameFrom.put(neighbour.add(centerOffset), currentTile.add(centerOffset));
                    return traceAsList(formPathStack(cameFrom, neighbour.add(centerOffset)));
                }

                if (!openSet.contains(neighbour) && !closedSet.contains(neighbour)) {
                    openSet.add(neighbour);
                    cameFrom.put(neighbour.add(centerOffset), currentTile.add(centerOffset));
                }
            }
        }
        return new ArrayList<>();
    }

    //gets the coordinates of the 8 surrounding cells. Warning: doesn't check if they're valid
    private static List<Tile> getNeighbours(Tile coordinates, TileSet tileSet){
        List<Tile> neighbours = new ArrayList<>();
        List<Collidable> currentBin;
        Tile currentTile;
        boolean blocked;
        int tileSize = tileSet.getTileSize();

        for (int i = -1; i < 2; i++){
            for (int j = -1; j < 2; j++){
                if (i == 0 && j == 0)
                    continue;

                currentTile = coordinates.add(i * tileSize, j*tileSize);
                blocked = false;
                currentBin = tileSet.getTileBin(currentTile);

                if (currentBin.size() != 0){
                    for (Collidable collidable : currentBin){
                        if (collidable.getBoundingBox().intersecting(
                                new Point(Tile.mapToCenterOfTile(currentTile, tileSize)))){
                            //if the tile's center is blocked, consider it blocked
                            blocked = true;
                            break;
                        }
                    }
                }

                if (!blocked)
                    neighbours.add(currentTile);

            }
        }
        return neighbours;
    }

    /**
     * Constructs a path for the AI to take to get to it's target
     * @param path the stack containing the path to follow, first node at head
     * @return A Tile array containing the path to take, in order
     */
    public static Tile[] tracePath(Stack<Tile> path){

        List<Tile> pathArray = new ArrayList<>();

        while (!path.empty()){
            Tile nextStep = path.pop();

            pathArray.add(nextStep);
        }
        return pathArray.toArray(new Tile[0]);
    }

    public static List<Tile> traceAsList(Stack<Tile> path){
        return Arrays.asList(tracePath(path));
    }

    public static Stack<Tile> formPathStack(HashMap<Tile, Tile> cameFrom, Tile targetTile){
        Stack<Tile> path = new Stack<>();

        Tile previous = targetTile;
        Tile current = cameFrom.get(targetTile);

        path.push(previous);

        while (current != null){
            Log.d("hb::TRACEPATH", current + ", prev: " + previous);
            path.push(current);
            previous = current;

            current = cameFrom.get(previous);
        }
        return path;
    }

    public static Stack<Tile> formPathStack(HashMap<Node<Tile>, Node<Tile>> cameFrom, Node<Tile> targetNodeName){
        Stack<Tile> path = new Stack<>();

        Node<Tile> previous = targetNodeName;
        Node<Tile> current = cameFrom.get(targetNodeName);

        path.push(previous.getNodeID());

        while (current != null){
            Log.d("hb::TRACEPATH", current.getNodeID() + ", prev: " + previous.getNodeID());
            path.push(current.getNodeID());
            previous = current;

            current = cameFrom.get(previous);
        }
        return path;
    }
}
