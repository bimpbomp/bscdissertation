package bham.student.txm683.framework.utils.graph;

import bham.student.txm683.framework.utils.Tile;

public class NodeWrapper extends Node<Tile>{
    private int fCost;
    private int gCost;
    private int hCost;

    public NodeWrapper(Node<Tile> node){
        super(node.nodeID);
        this.connections = node.connections;
    }

    public void setCosts(int f, int g, int h){
        this.fCost = f;
        this.gCost = g;
        this.hCost = h;
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
