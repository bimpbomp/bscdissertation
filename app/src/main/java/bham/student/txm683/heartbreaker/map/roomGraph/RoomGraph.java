package bham.student.txm683.heartbreaker.map.roomGraph;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoomGraph {

    private Map<Integer, RoomNode> nodes;

    @SuppressLint("UseSparseArrays")
    public RoomGraph(){
        this.nodes = new HashMap<>();
    }

    public RoomNode addNode(int id){
        RoomNode newNode;
        if (!nodes.containsKey(id)){
            newNode = new RoomNode(id);

            nodes.put(id, newNode);
            return newNode;
        }

        return nodes.get(id);
    }

    public RoomEdge addConnection(int first, int second, int doorID){
        if (containsNode(first) && containsNode(second)) {
            RoomEdge connection = new RoomEdge(doorID, nodes.get(first), nodes.get(second));

            nodes.get(first).addConnection(connection);
            nodes.get(second).addConnection(connection);

            return connection;
        }
        return null;
    }

    public RoomNode getNode(int requestedID){
        return this.nodes.containsKey(requestedID) ? nodes.get(requestedID) : null;
    }

    public ArrayList<RoomNode> getNodes(){
        return new ArrayList<>(this.nodes.values());
    }

    public boolean containsNode(Integer id){
        return nodes.containsKey(id);
    }
}