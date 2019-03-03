package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;
import bham.student.txm683.heartbreaker.utils.UniqueID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph <T> {
    private Map<T, Node<T>> nodes;
    private UniqueID uniqueID;

    public Graph(){
        this.nodes = new HashMap<>();
        this.uniqueID = new UniqueID();
    }

    public Node<T> addNode(T id){
        Node<T> newNode;
        if (!nodes.containsKey(id)){
            newNode = new Node<>(id);

            nodes.put(id, newNode);
            return newNode;
        }

        return null;
    }

    public Edge<T> addConnection(Node<T> from, Node<T> to, int weight){
        Edge<T> connection = new Edge<>(uniqueID.id(), from, to, weight);

        //dont connect two nodes that are already connected
        if (from.hasConnectionToNode(to))
            return from.getConnectionTo(to);

        from.addConnection(connection);

        return connection;
    }

    public Edge<T> addConnection(T first, T second, int weight){
        if (containsNode(first) && containsNode(second)){
            return addConnection(getNode(first), getNode(second), weight);
        }

        return null;
    }

    public void removeConnection(T from, T to){
        Node<T> fromNode = getNode(from);
        Node<T> toNode = getNode(to);

        fromNode.removeConnectionTo(toNode);
    }

    public Node<T> getNode(T requestedID){
        return this.nodes.containsKey(requestedID) ? nodes.get(requestedID) : null;
    }

    public ArrayList<Node<T>> getNodes(){
        return new ArrayList<>(this.nodes.values());
    }

    public boolean containsNode(T id){
        return nodes.containsKey(id);
    }

    public boolean containsNode(Node<T> node){return nodes.containsKey(node.getNodeID());}

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Graph\n");

        for (Node<T> node : nodes.values()){
            stringBuilder.append("Node ");
            stringBuilder.append(node.getNodeID().toString());

            if (node.getConnections().size() > 0){
                stringBuilder.append(" has neighbours: ");
                for (Edge<T> connection : node.getConnections()){
                    stringBuilder.append(connection.traverse().getNodeID().toString());
                    stringBuilder.append(" (");
                    stringBuilder.append(connection.getWeight());
                    stringBuilder.append("), ");
                }
                //removes final ", "
                stringBuilder.delete(stringBuilder.lastIndexOf(", "), stringBuilder.length());

                stringBuilder.append(".\n");

            } else {
                stringBuilder.append(" has no neighbours.\n");
            }
        }
        return stringBuilder.toString();
    }
}
