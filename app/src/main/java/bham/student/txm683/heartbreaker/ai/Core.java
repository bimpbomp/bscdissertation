package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Hexagon;
import bham.student.txm683.heartbreaker.entities.entityshapes.Octagon;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Core extends AIEntity implements Damageable {
    private Octagon innerShape;
    private Hexagon outerShape;

    private int health;
    private int width;

    public Core(String name, Point center, int size) {
        super(name, 0);
        health = 500;

        this.width = size;

        this.innerShape = new Octagon(center, size/2, Color.WHITE);
        this.outerShape = new Hexagon(center, size, Color.BLACK);
    }

    public Core(String name, Point center){
        this(name, center, 200);
    }

    public static Core build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);

        String name = jsonObject.getString("name");
        Core core = new Core(name, center);

        if (jsonObject.has("controls")) {
            List<String> controlledDoors = new ArrayList<>();

            JSONArray controls = jsonObject.getJSONArray("controls");

            for (int i = 0; i < controls.length(); i++){
                controlledDoors.add(controls.getString(i));
            }

            core.getContext().addVariable("controlled_doors", controlledDoors.toArray(new String[0]));
        }

        return core;
    }

    @Override
    public Vector getForwardUnitVector() {
        return null;
    }

    @Override
    public void rotate(Vector rotationVector) {

    }

    @Override
    public Weapon getWeapon() {
        return null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void rotateBy(float angle) {
        this.outerShape.rotateBy(angle);
        this.innerShape.rotateBy(angle);
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;
        return health <= 0;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {
        float angle = 0.261799f;

        innerShape.rotateBy(-1 * angle);
        outerShape.rotateBy(angle/2);
    }

    @Override
    public Point[] getCollisionVertices() {
        return outerShape.getVertices();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return innerShape.getShapeIdentifier();
    }

    @Override
    public Point getCenter() {
        return innerShape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        innerShape.translateShape(new Vector(innerShape.getCenter(), newCenter));
        outerShape.translateShape(new Vector(outerShape.getCenter(), newCenter));
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        outerShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        innerShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        innerShape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        innerShape.revertToDefaultColor();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return outerShape.getBoundingBox();
    }
}
