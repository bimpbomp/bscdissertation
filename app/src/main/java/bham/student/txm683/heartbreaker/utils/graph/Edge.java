package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;

public class Edge {
    private Node firstNode;
    private Node secondNode;

    private int weight;

    Edge(Node firstNode, Node secondNode, int weight){
        this.firstNode = firstNode;
        this.secondNode = secondNode;

        this.weight = weight;
    }

    /**
     * Returns the node connected to the provided node.
     * @param startNode Node at one end of edge.
     * @return Node at other end of edge, or null if startNode isn't one of the connectedNodes
     */
    public Node traverse(Node startNode){
        if (startNode.equals(firstNode))
            return secondNode;
        else if (startNode.equals(secondNode))
            return firstNode;

        return null;
    }

    public boolean hasNode(Node node){
        return (node.equals(firstNode) || node.equals(secondNode));
    }

    public int getWeight() {
        return weight;
    }

    @NonNull
    @Override
    public String toString() {
        return "Edge{" +
                "firstNode=" + firstNode.getCoordinates().toString() +
                ", secondNode=" + secondNode.getCoordinates().toString() +
                ", weight=" + weight +
                '}';
    }
}
