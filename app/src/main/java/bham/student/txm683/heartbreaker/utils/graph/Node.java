package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.Nullable;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.ArrayList;
import java.util.List;

public class Node {
    Tile coordinates;

    List<Edge> connections;

    public Node(){

    }

    public Node(Tile coordinates){
        this.connections = new ArrayList<>();

        this.coordinates = coordinates;
    }

    void addConnection(Edge newConnection){
        this.connections.add(newConnection);
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

    public int costToNeighbour(Node neighbour){
        for (Edge connection : connections){
            if (connection.hasNode(neighbour))
                return connection.getWeight();
        }
        return 0;
    }

    public boolean isConnectedToNode(Node node){
        for (Edge connection : connections){
            if (connection.hasNode(node))
                return true;
        }
        return false;
    }

    public int getX(){
        return coordinates.getX();
    }

    public int getY(){
        return coordinates.getY();
    }

    public String getName(){
        return coordinates.toString();
    }

    public Tile getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Tile coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Node))
            return false;

        return ((Node) obj).coordinates.equals(this.coordinates);
    }
}
