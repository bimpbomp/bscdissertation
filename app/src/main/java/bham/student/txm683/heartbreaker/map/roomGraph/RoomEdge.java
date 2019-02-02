package bham.student.txm683.heartbreaker.map.roomGraph;

import bham.student.txm683.heartbreaker.utils.graph.Edge;

public class RoomEdge extends Edge<Integer> {
    private int doorID;

    public RoomEdge(RoomNode room1, RoomNode room2) {
        super(room1, room2, 1);
    }

    public int getDoorID() {
        return doorID;
    }
}
