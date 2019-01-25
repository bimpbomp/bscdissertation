package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {
    private Map<String, Node> nodes;

    public Graph(){
        this.nodes = new HashMap<>();
    }

    public Node addNode(Tile coordinates){
        Node newNode;
        if (!nodes.containsKey(coordinates.toString())){
            newNode = new Node(coordinates);
            nodes.put(coordinates.toString(), newNode);
            return newNode;
        }

        return null;
    }

    public Edge addConnection(Node first, Node second, int weight){
        Edge connection = new Edge(first, second, weight);

        first.addConnection(connection);
        second.addConnection(connection);

        /*if (nodes.containsKey(first.getCoordinates()) && nodes.containsKey(second.getCoordinates())) {
            Log.d("hb::Graph", "adding edge between " + first.getCoordinates().toString() + " and " + second.getCoordinates().toString());
            first = nodes.get(first.getCoordinates());
            second = nodes.get(second.getCoordinates());

            Edge connection = new Edge(first, second, weight);

            first.addConnection(connection);
            second.addConnection(connection);

            return connection;
        }*/
        return null;
    }

    public Node getNode(Tile coordinates){
        return this.nodes.containsKey(coordinates.toString()) ? nodes.get(coordinates.toString()) : null;
    }

    public ArrayList<Node> getNodes(){
        return new ArrayList<>(this.nodes.values());
    }

    public boolean containsNode(Tile coordinates){
        return nodes.containsKey(coordinates.toString());
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Graph\n");

        for (Node node : nodes.values()){
            stringBuilder.append("Node ");
            stringBuilder.append(node.getCoordinates().toString());

            if (node.getConnections().size() > 0){
                stringBuilder.append(" has neighbours: ");
                for (Edge connection : node.getConnections()){
                    stringBuilder.append(connection.traverse(node).getCoordinates().toString());
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
