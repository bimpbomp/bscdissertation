package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;

public class Edge <T>{
    private Node<T> firstNode;
    private Node<T> secondNode;

    private int weight;

    public Edge(Node<T> firstNode, Node<T> secondNode, int weight){
        this.firstNode = firstNode;
        this.secondNode = secondNode;

        this.weight = weight;
    }

    /**
     * Returns the node connected to the provided node.
     * @param startNode Node at one end of edge.
     * @return Node at other end of edge, or null if startNode isn't one of the connectedNodes
     */
    public Node<T> traverse(Node<T> startNode){
        if (startNode.equals(firstNode))
            return secondNode;
        else if (startNode.equals(secondNode))
            return firstNode;

        return null;
    }

    public boolean hasNode(Node<T> node){
        return (node.equals(firstNode) || node.equals(secondNode));
    }

    public int getWeight() {
        return weight;
    }

    @NonNull
    @Override
    public String toString() {
        return "Edge{" +
                "firstNode=" + firstNode.getNodeID().toString() +
                ", secondNode=" + secondNode.getNodeID().toString() +
                ", weight=" + weight +
                '}';
    }
}
