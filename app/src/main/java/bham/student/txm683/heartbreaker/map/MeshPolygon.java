package bham.student.txm683.heartbreaker.map;

import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.List;

public class MeshPolygon {

    private int id;
    private Rectangle area;

    public MeshPolygon(MeshSet meshSet, int tileSize){
        this.id = meshSet.getId();

        generateArea(meshSet.getContainedTiles(), tileSize);
    }

    public MeshPolygon(int id, Rectangle area){
        this.id = id;
        this.area = area;
    }

    private void generateArea(List<Tile> meshSetTiles, int tileSize){
        //sort tiles
        meshSetTiles.sort((a,b) -> {
            if (a.getY() < b.getY())
                return -1;
            else if (a.getY() > b.getY())
                return 1;
            else{
                return Integer.compare(a.getX(), b.getX());
            }
        });

        Tile tl = meshSetTiles.get(0);
        Tile br = meshSetTiles.get(meshSetTiles.size()-1);

        Point topLeft = new Point(tl).sMult(tileSize);
        Point bottomRight = new Point(br).sMult(tileSize);

        Point center = topLeft.add(bottomRight.getX() - topLeft.getX(),
                bottomRight.getY() - topLeft.getY());

        this.area = new Rectangle(center, topLeft, bottomRight, Color.LTGRAY);
    }

    public int getId(){
        return this.id;
    }
}
