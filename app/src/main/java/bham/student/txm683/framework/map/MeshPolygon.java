package bham.student.txm683.framework.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.framework.entities.entityshapes.Rectangle;
import bham.student.txm683.framework.rendering.RenderingTools;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Tile;
import bham.student.txm683.heartbreaker.rendering.ColorScheme;

import java.util.List;
import java.util.Random;

public class MeshPolygon {

    private int id;
    private Rectangle boundingVolume;
    private Random random;

    public MeshPolygon(MeshSet meshSet, int tileSize){
        this.id = meshSet.getId();

        this.random = new Random();

        generateBoundingVolume(meshSet.getContainedTiles(), tileSize);
    }

    public MeshPolygon(int id, Rectangle boundingVolume){
        this.id = id;
        this.boundingVolume = boundingVolume;
    }

    public BoundingBox getBoundingBox(){
        return boundingVolume.getBoundingBox();
    }

    public Point getCenter(){
        return boundingVolume.getCenter();
    }

    public void draw(Canvas canvas, Point renderOffset){
        boundingVolume.draw(canvas, renderOffset, 0, false);
    }

    public void drawLabel(Canvas canvas, Point renderOffset, Paint textPaint){
        RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, ""+id, getCenter().add(renderOffset), Color.WHITE, 5);
    }

    private void generateBoundingVolume(List<Tile> meshSetTiles, int tileSize){
        //sort tiles primarily by y coordinate, if equal the x coordinate is then compared
        meshSetTiles.sort((a,b) -> {
            if (a.getY() < b.getY())
                return -1;
            else if (a.getY() > b.getY())
                return 1;
            else{
                return Integer.compare(a.getX(), b.getX());
            }
        });

        //the stored tiles for top left and bottom right respectively
        Tile tl = meshSetTiles.get(0);
        Tile br = meshSetTiles.get(meshSetTiles.size()-1).add(1,1);

        //the tiles above converted to global coordinates
        Point topLeft = new Point(tl).sMult(tileSize);
        Point bottomRight = new Point(br).sMult(tileSize);

        Point center = topLeft.add((bottomRight.getX() - topLeft.getX())/2f,
                (bottomRight.getY() - topLeft.getY())/2f);

        this.boundingVolume = new Rectangle(center, topLeft, bottomRight, ColorScheme.randomGreen());
    }

    public Point getRandomPointInMesh(){
        BoundingBox boundingBox = boundingVolume.getBoundingBox();

        float x = boundingBox.getLeft() + random.nextFloat() * (boundingBox.getRight() - boundingBox.getLeft());
        float y = boundingBox.getTop() + random.nextFloat() * (boundingBox.getBottom() - boundingBox.getTop());
        return new Point(x, y);
    }

    public int getId(){
        return this.id;
    }

    public float getArea(){
        return getBoundingBox().area();
    }
}
