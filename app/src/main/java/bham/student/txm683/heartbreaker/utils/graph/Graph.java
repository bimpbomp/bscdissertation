package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {
    private Map<String, Node> nodes;

    public Graph(){
        this.nodes = new HashMap<>();
    }

    public Node addNode(String name){
        Node newNode;
        if (!nodes.containsKey(name)){
            newNode = new Node(name);
            nodes.put(name, newNode);
            return newNode;
        }
        return null;
    }

    public Edge addConnection(Node first, Node second, int weight){

        Edge connection = new Edge(first, second, weight);

        first.addConnection(connection);
        second.addConnection(connection);

        /*if (nodes.containsKey(first.getName()) && nodes.containsKey(second.getName())) {
            first = nodes.get(first.getName());
            second = nodes.get(second.getName());

            Edge connection = new Edge(first, second, weight);

            first.addConnection(connection);
            second.addConnection(connection);

            return connection;
        }*/
        return null;
    }

    public Node getNode(String name){
        return this.nodes.containsKey(name) ? nodes.get(name) : null;
    }

    public ArrayList<Node> getNodes(){
        return new ArrayList<>(this.nodes.values());
    }

    public boolean containsNode(String name){
        return nodes.containsKey(name);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Graph\n");

        for (Node node : nodes.values()){
            stringBuilder.append("Node ");
            stringBuilder.append(node.getName());

            if (node.getConnections().size() > 0){
                stringBuilder.append(" has neighbours: ");
                for (Edge connection : node.getConnections()){
                    stringBuilder.append(connection.traverse(node).getName());
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
