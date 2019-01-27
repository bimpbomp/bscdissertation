package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.utils.graph.Node;

public class NodeWrapper {
    private Node node;
    private Node parent;
    private int fCost;
    private int gCost;
    private int hCost;

    public NodeWrapper(Node node, Node parent){
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getfCost() {
        return fCost;
    }

    public void setfCost(int fCost) {
        this.fCost = fCost;
    }

    public int getgCost() {
        return gCost;
    }

    public void setgCost(int gCost) {
        this.gCost = gCost;
    }

    public int gethCost() {
        return hCost;
    }

    public void sethCost(int hCost) {
        this.hCost = hCost;
    }
}
