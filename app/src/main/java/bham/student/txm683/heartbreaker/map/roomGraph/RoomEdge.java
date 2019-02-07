package bham.student.txm683.heartbreaker.map.roomGraph;

import android.support.annotation.NonNull;
import android.util.Pair;
import bham.student.txm683.heartbreaker.entities.Door;

public class RoomEdge {
    private RoomNode firstRoom;
    private RoomNode secondRoom;
    private Door door;

    public RoomEdge(Door door, RoomNode room1, RoomNode room2) {
        this.door = door;
        this.firstRoom = room1;
        this.secondRoom = room2;
    }

    public Door getDoor() {
        return door;
    }

    /**
     * Returns the node connected to the provided node.
     * @param startNode Node at one end of edge.
     * @return Node at other end of edge, or the startNode it isn't one of the connectedNodes
     */
    public RoomNode traverse(RoomNode startNode){
        if (startNode.equals(firstRoom))
            return secondRoom;
        else if (startNode.equals(secondRoom))
            return firstRoom;

        return startNode;
    }

    public Pair<RoomNode, RoomNode> getConnectedRoomNodes() {
        return new Pair<>(firstRoom, secondRoom);
    }

    public boolean hasNode(RoomNode node){
        return (node.equals(firstRoom)) || node.equals(secondRoom);
    }

    @NonNull
    @Override
    public String toString() {
        return "Edge{" +
                "firstNode=" + firstRoom.getRoom().getId() +
                ", secondNode=" + secondRoom.getRoom().getId() +
                ", door=" + door.getName() +
                '}';
    }
}
