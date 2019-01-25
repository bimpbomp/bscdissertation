package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.Nullable;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private Point coordinates;

    private List<Edge> connections;

    Node(String name){
        this.name = name;
        this.connections = new ArrayList<>();
    }

    Node(String name, Point coordinates){
        this.name = name;
        this.connections = new ArrayList<>();

        this.coordinates = coordinates;
    }

    void addConnection(Edge newConnection){
        this.connections.add(newConnection);
    }

    public String getName() {
        return name;
    }

    public List<Edge> getConnections() {
        return connections;
    }

    public List<Node> getNeighbours(){
        ArrayList<Node> neighbours = new ArrayList<>();

        for (Edge connection : connections){
            neighbours.add(connection.traverse(this));
        }
        return neighbours;
    }

    public boolean isConnectedToNode(Node node){
        for (Edge connection : connections){
            if (connection.hasNode(node))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Node))
            return false;

        return ((Node) obj).name.equals(this.name);
    }
}
