package bham.student.txm683.heartbreaker.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Line;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MeshPolygon {

    private int id;
    private Rectangle area;
    private Random random;

    private Map<Integer, Line> edgesToNeighbours;

    public MeshPolygon(MeshSet meshSet, int tileSize){
        this.id = meshSet.getId();

        this.random = new Random();

        this.edgesToNeighbours = new HashMap<>();

        generateArea(meshSet.getContainedTiles(), tileSize);
    }

    public MeshPolygon(int id, Rectangle area){
        this.id = id;
        this.area = area;
    }

    public Map<Integer, Line> getEdgesToNeighbours() {
        return edgesToNeighbours;
    }

    public Point getPointOnLineToNeighbour(int neighbourId, Point p){
        if (edgesToNeighbours.containsKey(neighbourId)){

            return edgesToNeighbours.get(neighbourId).projectOnToLine(p);
        }
        return new Point();
    }

    public Point getNearestPoint(Point p, int widthOfEntity){
        BoundingBox b = area.getBoundingBox();
        return new Point(mapToRange(p.getX(), widthOfEntity, b.getLeft(), b.getRight()), mapToRange(p.getY(), widthOfEntity, b.getTop(), b.getBottom()));
    }

    private float mapToRange(float x, int width, float min, float max){
        width = width / 2;

        if (x-width <= min)
            return min+width;
        else if (x+width >= max)
            return max-width;
        else
            return x;
    }

    public BoundingBox getBoundingBox(){
        return area.getBoundingBox();
    }

    public Point getCenter(){
        return area.getCenter();
    }

    public void draw(Canvas canvas, Point renderOffset, Paint textPaint){
        area.draw(canvas, renderOffset, 0, false);

        RenderingTools.renderCenteredTextWithBoundingBox(canvas, textPaint, ""+id, getCenter().add(renderOffset), Color.WHITE, 5);
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
        Tile br = meshSetTiles.get(meshSetTiles.size()-1).add(1,1);

        Point topLeft = new Point(tl).sMult(tileSize);
        Point bottomRight = new Point(br).sMult(tileSize);

        Point center = topLeft.add(bottomRight.getX() - topLeft.getX(),
                bottomRight.getY() - topLeft.getY());

        this.area = new Rectangle(center, topLeft, bottomRight, Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
    }

    public Point getRandomPointInMesh(){
        BoundingBox boundingBox = area.getBoundingBox();

        float x = boundingBox.getLeft() + random.nextFloat() * (boundingBox.getRight() - boundingBox.getLeft());
        float y = boundingBox.getTop() + random.nextFloat() * (boundingBox.getBottom() - boundingBox.getTop());
        return new Point(x, y);
    }

    public int getId(){
        return this.id;
    }
}
