package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Hexagon;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.graph.Graph;

public class HexBubble {
    private Hexagon hex;
    private Graph<Point> graph;

    public HexBubble(Point center, int size){
        hex = new Hexagon(center, size, Color.GRAY);
        graph = new Graph<>();


        Point[] vertices = hex.getVertices();

        for (Point point : vertices){
            graph.addNode(point);
        }

        for (int i = 0; i < vertices.length-1; i++){
            addConnection(vertices[i], vertices[i+1]);
        }

        addConnection(vertices[0], vertices[vertices.length-1]);
    }

    public void draw(Canvas canvas, Point renderOffset){
        hex.draw(canvas, renderOffset, 0, false);
    }

    private void addConnection(Point point, Point point1){
        int weight = CollisionManager.euclideanHeuristic(point, point1);

        graph.addConnection(point, point1, weight);
        graph.addConnection(point1, point, weight);
    }

    public Point[] getCollisionVertices(){
        return hex.getVertices();
    }

    public BoundingBox getBoundingBox(){
        return hex.getBoundingBox();
    }

    public Graph<Point> getGraph(){
        return graph;
    }
}
