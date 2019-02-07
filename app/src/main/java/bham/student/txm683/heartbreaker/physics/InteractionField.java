package bham.student.txm683.heartbreaker.physics;

import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.entities.entityshapes.IsoscelesTrapezium;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class InteractionField implements Renderable, Collidable {

    private IsoscelesTrapezium shape;

    public InteractionField(){

    }

    @Override
    public Point[] getCollisionVertices() {
        return new Point[0];
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, interpolationVector, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        this.shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        this.shape.revertToDefaultColor();
    }
}
