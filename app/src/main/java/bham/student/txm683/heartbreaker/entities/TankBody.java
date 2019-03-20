package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import bham.student.txm683.heartbreaker.entities.entityshapes.Kite;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class TankBody implements Renderable, Shape {

    private Kite body;
    private Rectangle turret;
    private Rectangle barrel;

    private Vector barrelPosition;
    private float barrelDistance;

    public TankBody(Point center, int size, int color){
        this.body = Kite.constructKite(center, size, color);

        float turretSize = size/4f;
        this.turret = new Rectangle(center, turretSize, turretSize, ColorScheme.manipulateColor(color, 0.7f));

        float barrelLength = size/2f;
        float barrelWidth = size/6f;

        Point barrelCenter = center.add(0, -1 * (barrelLength/2f + turretSize/2f));
        this.barrelPosition = new Vector(center, barrelCenter);
        this.barrelDistance = barrelPosition.getLength();

        Log.d("TANK", "barrel distance: " + barrelDistance);

        this.barrel = new Rectangle(barrelCenter, barrelWidth, barrelLength, Color.BLACK);
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        body.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        barrel.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        turret.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        body.setColor(color);
        turret.setColor(ColorScheme.manipulateColor(color, 0.7f));
    }

    @Override
    public void revertToDefaultColor() {
        body.revertToDefaultColor();
        turret.revertToDefaultColor();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return body.getBoundingBox();
    }

    @Override
    public String getName() {
        return "TANK BODY";
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.KITE;
    }

    @Override
    public Vector getForwardUnitVector() {
        return body.getForwardUnitVector();
    }

    @Override
    public Point getCenter() {
        return body.getCenter();
    }

    @Override
    public Point[] getVertices() {
        return body.getVertices();
    }

    @Override
    public Point[] getVertices(Point offset) {
        return body.getVertices(offset);
    }

    @Override
    public void translate(Vector movementVector) {
        body.translate(movementVector);
        turret.translate(movementVector);

        barrel.translate(movementVector);

        barrelPosition = barrelPosition.vAdd(movementVector);
    }

    @Override
    public void translate(Point newCenter) {
        translate(new Vector(body.getCenter(), newCenter));
    }

    @Override
    public void rotate(float angle) {
        body.rotate(angle);
        turret.rotate(angle);

        barrel.rotate(angle);
        barrelPosition = barrelPosition.rotate((float) Math.cos(angle), (float) Math.sin(angle));

        Vector v = turret.getForwardUnitVector().setLength(barrelDistance);

        barrel.translate(v.getHead());

        Log.d("TANK", "v head: " + v.getHead());
    }

    @Override
    public void rotate(Vector v) {
        body.rotate(v);
        turret.rotate(v);

        barrel.rotate(v);

        float angle = Vector.calculateAngleBetweenVectors(barrelPosition, v);

        barrelPosition = barrelPosition.rotate((float) Math.cos(angle), (float) Math.sin(angle));

        Vector a = turret.getForwardUnitVector().setLength(barrelDistance);


        barrel.translate(a.getHead());

        Log.d("TANK", "a head: " + v.getHead());
    }

    @Override
    public int getColor() {
        return body.getColor();
    }
}
