package bham.student.txm683.heartbreaker.map.roomGraph;

import android.support.annotation.Nullable;
import bham.student.txm683.heartbreaker.map.Room;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class RoomNode {
    private List<RoomEdge> connections;

    private Room room;

    public RoomNode(Room room){
        this.connections = new ArrayList<>();
        this.room = room;
    }

    public Room getRoom(){
        return this.room;
    }

    public List<RoomEdge> getConnections() {
        return connections;
    }

    public void addConnection(RoomEdge newConnection){
        this.connections.add(newConnection);
    }

    public boolean isConnectedToNode(RoomNode node){
        for (RoomEdge connection : connections){
            if (connection.hasNode(node))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof RoomNode))
            return false;

        return ((RoomNode) obj).getRoom().getId() == this.getRoom().getId();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 23).append(room.getId()).toHashCode();
    }
}
