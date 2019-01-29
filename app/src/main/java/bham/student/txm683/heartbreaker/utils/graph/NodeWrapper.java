package bham.student.txm683.heartbreaker.utils.graph;

public class NodeWrapper extends Node{
    private NodeWrapper parent;
    private int fCost;
    private int gCost;
    private int hCost;

    public NodeWrapper(Node node){
        super(node.coordinates);
        this.connections = node.connections;
    }

    public void setCosts(int f, int g, int h){
        this.fCost = f;
        this.gCost = g;
        this.hCost = h;
    }

    public NodeWrapper getParent() {
        return parent;
    }

    public boolean hasParent(){
        return parent != null;
    }

    public void setParent(NodeWrapper parent) {
        this.parent = parent;
    }

    public int getfCost() {
        return fCost;
    }

    public int getgCost() {
        return gCost;
    }

    public int gethCost() {
        return hCost;
    }
}
