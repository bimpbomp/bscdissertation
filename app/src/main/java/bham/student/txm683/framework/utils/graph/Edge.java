package bham.student.txm683.framework.utils.graph;

import android.support.annotation.NonNull;
import android.util.Pair;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Edge <T>{
    private int edgeId;
    private Node<T> fromNode;
    private Node<T> toNode;

    private int weight;

    public Edge(int edgeId, Node<T> fromNode, Node<T> toNode, int weight){
        this.edgeId = edgeId;

        this.fromNode = fromNode;
        this.toNode = toNode;

        this.weight = weight;
    }

    public int getEdgeId(){
        return this.edgeId;
    }

    /**
     * Traverses the edge and returns the to-node.
     * @return The to node
     */
    public Node<T> traverse(){
        return toNode;
    }

    public Pair<Node<T>, Node<T>> getConnectedNodes(){
        return new Pair<>(fromNode, toNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge<?> edge = (Edge<?>) o;
        return edgeId == edge.edgeId;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7,17).append(edgeId).toHashCode();
    }

    /*public boolean hasNode(Node<T> node){
        return (node.equals(fromNode) || node.equals(toNode));
    }*/

    public int getWeight() {
        return weight;
    }

    @NonNull
    @Override
    public String toString() {
        return "Edge{" +
                "fromNode=" + fromNode.getNodeID().toString() +
                ", toNode=" + toNode.getNodeID().toString() +
                ", weight=" + weight +
                '}';
    }
}
