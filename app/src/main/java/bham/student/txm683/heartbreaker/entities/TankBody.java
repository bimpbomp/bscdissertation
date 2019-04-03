package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.entityshapes.TankShape;
import bham.student.txm683.heartbreaker.rendering.ColorScheme;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class TankBody implements Renderable, Shape {

    private TankShape body;
    private Rectangle turret;
    private Rectangle barrel;

    private Vector barrelPosition;
    private float barrelDistance;

    private float lengthToBarrelTip;

    public TankBody(Point center, int size, int color, TankModifiers tankModifiers){

        int tw = (int) (size * 0.9f);
        int bw = (int) (size * 0.75f);
        int mh = (int) (size * 0.8f);
        int ph = (int) (size * 0.2f);

        this.body = TankShape.build(center, tw, bw, mh, ph, color);

        float turretSize = tankModifiers.getTurretSizeModifier() * size * 0.35f;
        int turretColor = ColorScheme.manipulateColor(color, 0.7f);
        this.turret = new Rectangle(center, turretSize, turretSize, turretColor);

        float barrelLength = tankModifiers.getBarrelLengthModifier() * size/2.5f;
        float barrelWidth = tankModifiers.getBarrelWidthModifier() * size/6f;

        Point barrelCenter = center.add(0, -1 * (barrelLength/2f + turretSize/2f));
        this.barrelPosition = new Vector(center, barrelCenter);
        this.barrelDistance = barrelPosition.getLength();

        this.lengthToBarrelTip = turretSize/2f + barrelLength;

        Log.d("TANK", "barrel distance: " + barrelDistance);

        this.barrel = new Rectangle(barrelCenter, barrelWidth, barrelLength, Color.BLACK);
    }

    public TankBody(Point center, int size, int color){
        this(center, size, color, new TankModifiers());
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

    public void setBodyColor(int color){
        body.setColor(color);
    }

    public void setTurretColor(int color){
        turret.setColor(color);
    }

    public int getDefaultColor(){
        return body.getDefaultColor();
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
    }

    private void rotateBarrel(float angle){
        barrelPosition = barrelPosition.rotate((float) Math.cos(angle), (float) Math.sin(angle));

        Vector v = turret.getForwardUnitVector().setLength(barrelDistance);

        barrel.translate(v.getHead());
    }

    public void rotateTurret(float angle){
        turret.rotate(angle);

        barrel.rotate(angle);

        rotateBarrel(angle);
    }

    public void rotateTurret(Vector v){
        turret.rotate(v);

        barrel.rotate(v);

        float angle = Vector.calculateAngleBetweenVectors(barrelPosition, v);

        rotateBarrel(angle);
    }

    public Vector getTurretFUnit(){
        return new Vector(turret.getCenter(), barrel.getCenter()).getUnitVector();
    }

    public Vector getShootingVector(){
        Vector v = getTurretFUnit().setLength(lengthToBarrelTip);
        Log.d("TANK", "shootvector: " + v + ", turretcenter: "  + turret.getCenter() );
        return v;
    }

    @Override
    public void rotate(Vector v) {
        body.rotate(v);
    }

    @Override
    public int getColor() {
        return body.getColor();
    }
}
