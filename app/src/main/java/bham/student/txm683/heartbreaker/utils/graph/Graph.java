package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph <T> {
    private Map<T, Node<T>> nodes;

    public Graph(){
        this.nodes = new HashMap<>();
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

    public Edge<T> addConnection(Node<T> first, Node<T> second, int weight){
        Edge<T> connection = new Edge<>(first, second, weight);

        first.addConnection(connection);
        second.addConnection(connection);

        return connection;
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
                    stringBuilder.append(connection.traverse(node).getNodeID().toString());
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
