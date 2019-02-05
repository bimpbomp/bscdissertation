package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
    T nodeID;

    List<Edge<T>> connections;

    public Node(T nodeID){
        this.connections = new ArrayList<>();

        this.nodeID = nodeID;
    }

    public void addConnection(Edge<T> newConnection){
        this.connections.add(newConnection);
    }

    public List<Edge<T>> getConnections() {
        return connections;
    }

    public List<Node> getNeighbours(){
        ArrayList<Node> neighbours = new ArrayList<>();

        for (Edge<T> connection : connections){
            neighbours.add(connection.traverse(this));
        }
        return neighbours;
    }

    public int costToNeighbour(Node<T> neighbour){
        for (Edge<T> connection : connections){
            if (connection.hasNode(neighbour))
                return connection.getWeight();
        }
        return 0;
    }

    public boolean isConnectedToNode(Node<T> node){
        for (Edge<T> connection : connections){
            if (connection.hasNode(node))
                return true;
        }
        return false;
    }

    public T getNodeID() {
        return nodeID;
    }

    public void setNodeID(T nodeID) {
        this.nodeID = nodeID;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof Node))
            return false;

        return ((Node) obj).nodeID.equals(this.nodeID);
    }
}
