package bham.student.txm683.heartbreaker.map.roomGraph;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.map.Room;

import java.util.*;

public class RoomGraph {

    private Map<Integer, RoomNode> nodes;

    @SuppressLint("UseSparseArrays")
    public RoomGraph(){
        this.nodes = new HashMap<>();
    }

    public RoomNode addNode(Room room){
        RoomNode newNode;
        if (!nodes.containsKey(room.getId())){
            newNode = new RoomNode(room);

            nodes.put(room.getId(), newNode);
            return newNode;
        }

        return nodes.get(room.getId());
    }

    public RoomEdge getConnection(int id){
        for (RoomNode node : nodes.values()){
            for (RoomEdge edge: node.getConnections()){
                if (edge.getDoor().getDoorID() == id)
                    return edge;
            }
        }
        return null;
    }

    public Door getDoorBetweenRooms(int room1, int room2){
        if (nodes.containsKey(room1) && nodes.containsKey(room2)){
            for (RoomEdge edge : nodes.get(room1).getConnections()){
                if (edge.hasNode(nodes.get(room2)))
                    return edge.getDoor();
            }
        }
        return null;
    }

    public RoomEdge addConnection(int first, int second, Door door){
        if (containsNode(first) && containsNode(second)) {
            RoomEdge connection = new RoomEdge(door, nodes.get(first), nodes.get(second));

            nodes.get(first).addConnection(connection);
            nodes.get(second).addConnection(connection);

            return connection;
        }
        return null;
    }

    public RoomNode getNode(int requestedID){
        return this.nodes.containsKey(requestedID) ? nodes.get(requestedID) : null;
    }

    public Map<Integer, RoomNode> getNodes() {
        return nodes;
    }

    public boolean containsNode(Integer id){
        return nodes.containsKey(id);
    }

    public RoomNode[] pathToRoom(int startingRoomID, int targetRoomId){

        //visited tiles are added here
        HashSet<RoomNode> closedSet = new HashSet<>();

        //if nodes dont exist, return
        if (!this.nodes.containsKey(startingRoomID) || !this.nodes.containsKey(targetRoomId)) {
            Log.d("hb::ROOMGRAPH", "start " + nodes.containsKey(startingRoomID) +
                    "/ target " + nodes.containsKey(targetRoomId) + " not in graph: ");
            return new RoomNode[0];
        }

        //if already at target, return
        if (startingRoomID == targetRoomId){
            Log.d("hb::ROOMGRAPH", "already at destination");
            return new RoomNode[0];
        }

        RoomNode startNode = nodes.get(startingRoomID);
        RoomNode targetNode = nodes.get(targetRoomId);

        Log.d("hb::NODES", startNode.getRoom().getId() + ": " + targetNode.getRoom().getId());

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        PriorityQueue<Pair<RoomNode, Integer>> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second.equals(b.second))
                return 0;
            else if (a.second < b.second)
                return -1;
            return 1; });

        openSet.add(new Pair<>(startNode, 0));

        HashMap<RoomNode, RoomNode> cameFrom = new HashMap<>();
        cameFrom.put(startNode, null);

        HashMap<RoomNode, Integer> costSoFar = new HashMap<>();
        costSoFar.put(startNode, 0);

        //initialise variables needed in loop
        Pair<RoomNode, Integer> tileAndCost;
        RoomNode currentNode;
        int currentCost;
        RoomNode neighbour;
        while (!openSet.isEmpty()) {

            //get the tile with the lowest cost
            tileAndCost = openSet.poll();
            currentNode = tileAndCost.first;
            currentCost = tileAndCost.second;

            //add it to the closed set so it isn't inspected again
            closedSet.add(currentNode);


            for (RoomEdge connection : currentNode.getConnections()) {
                neighbour = connection.traverse(currentNode);

                if (targetNode.equals(neighbour)){
                    cameFrom.put(neighbour, currentNode);
                    Log.d("hb::RoomGraph", "target reached!");
                    return tracePath(cameFrom, targetNode);
                }

                if (closedSet.contains(neighbour)) {
                    //if the node has already been inspected, move on
                    continue;
                }

                int neighbourCost = currentCost + 1;
                if (!costSoFar.containsKey(neighbour) || costSoFar.get(neighbour) < neighbourCost) {
                    //neighbour is a valid tile but not target, calc it's cost and add to openset
                    costSoFar.put(neighbour, neighbourCost);
                    openSet.add(new Pair<>(neighbour, neighbourCost));
                    cameFrom.put(neighbour, currentNode);
                }
            }
        }
        return tracePath(cameFrom, targetNode);
    }

    private RoomNode[] tracePath(HashMap<RoomNode, RoomNode> cameFrom, RoomNode targetNode){

        if (cameFrom.containsKey(targetNode)){
            Stack<RoomNode> path = new Stack<>();

            RoomNode previous = targetNode;
            RoomNode current = cameFrom.get(previous);

            path.push(previous);

            while (current != null){

                Log.d("hb::RoomGraph", current.getRoom().getId() + ": "  + previous.getRoom().getId());
                path.push(current);
                previous = current;
                try {
                    current = cameFrom.get(previous);
                } catch (NullPointerException e){
                    return new RoomNode[0];
                }
            }
            return path.toArray(new RoomNode[0]);

        } else {
            Log.d("hb::RoomGraph", "tracePath: target tile not found in path");
            return new RoomNode[0];
        }
    }
}