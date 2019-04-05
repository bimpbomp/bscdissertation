package bham.student.txm683.framework.utils.graph;

import android.support.annotation.Nullable;
import org.apache.commons.lang.builder.HashCodeBuilder;

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

    public void removeConnectionTo(Node<T> node){
        Edge edgeToRemove = null;

        for (Edge<T> edge : connections){
            if (edge.traverse().equals(node)){
                edgeToRemove = edge;
                break;
            }
        }

        if (edgeToRemove != null){
            connections.remove(edgeToRemove);
        }
    }

    public List<Edge<T>> getConnections() {
        return connections;
    }

    public List<Node<T>> getNeighbours(){
        ArrayList<Node<T>> neighbours = new ArrayList<>();

        for (Edge<T> connection : connections){
            neighbours.add(connection.traverse());
        }
        return neighbours;
    }

    public int costToNeighbour(Node<T> neighbour){
        for (Edge<T> connection : connections){
            if (connection.traverse().equals(neighbour))
                return connection.getWeight();
        }
        return 0;
    }

    public boolean hasConnectionToNode(Node<T> node){
        for (Edge<T> connection : connections){
            if (connection.traverse().equals(node))
                return true;
        }
        return false;
    }

    public Edge<T> getConnectionTo(Node<T> node){
        for (Edge<T> connection : connections){
            if (connection.traverse().equals(node))
                return connection;
        }
        return null;
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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11,19).append(nodeID).toHashCode();
    }
}
