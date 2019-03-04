package bham.student.txm683.heartbreaker.map;

import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.List;

public class MeshPolygon {

    private int id;
    private Rectangle area;

    public MeshPolygon(MeshSet meshSet){
        this.id = meshSet.getId();

        generateArea(meshSet.getContainedTiles());
    }

    private void generateArea(List<Tile> meshSetTiles){

    }

    public int getId(){
        return this.id;
    }
}
