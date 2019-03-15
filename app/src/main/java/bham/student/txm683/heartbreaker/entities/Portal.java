package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ICircle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Portal extends Entity implements ICircle, Renderable {
    public enum PortalType {
        ENTRANCE,
        EXIT
    }

    public enum ActivateCondition {
        GUARDS_DEFEATED
    }

    private Circle shape;

    private PortalType portalType;
    private ActivateCondition activateCondition;

    private boolean active;

    private String leadsTo;

    private List<String> guards;

    public Portal(String name, Point center, PortalType portalType, ActivateCondition activateCondition, String leadsTo, List<String> guards) {
        super(name);
        shape = new Circle(center, 50, Color.MAGENTA);

        setActive(false);
        this.guards = guards;

        this.leadsTo = leadsTo;

        this.portalType = portalType;
        this.activateCondition = activateCondition;
    }

    public static Portal build(JSONObject jsonObject, int tileSize) throws JSONException {

        String name = "portal";
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);
        PortalType type = PortalType.valueOf(jsonObject.getString("type"));

        List<String> guards = new ArrayList<>();

        if (jsonObject.has("guards")) {
            JSONArray jsonArray = jsonObject.getJSONArray("guards");

            for (int i = 0; i < jsonArray.length(); i++){
                guards.add(jsonArray.getString(i));
            }
        }

        String leadsTo = "";
        if (jsonObject.has("leads_to"))
            leadsTo = jsonObject.getString("leads_to");

        ActivateCondition activateCondition = ActivateCondition.GUARDS_DEFEATED;
        if (jsonObject.has("activate_condition"))
            activateCondition = ActivateCondition.valueOf(jsonObject.getString("activate_condition"));

        return new Portal(name, center, type, activateCondition, leadsTo, guards);
    }

    public ActivateCondition getActivateCondition() {
        return activateCondition;
    }

    public String getLeadsTo() {
        return leadsTo;
    }

    public PortalType getPortalType() {
        return portalType;
    }

    public boolean isActive() {
        return active;
    }

    public List<String> getGuards() {
        return guards;
    }

    public void setActive(boolean active) {
        this.active = active;

        if (active) {
            shape.setRadius(200f);
            shape.setColor(Color.MAGENTA);
        } else {
            shape.setRadius(50);
            shape.setColor(Color.argb(175, 255,192,203));
        }
    }

    @Override
    public float getRadius() {
        return shape.getRadius();
    }

    @Override
    public Circle getCircle() {
        return shape;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        shape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
    }

    @Override
    public void setColor(int color) {
        shape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        shape.revertToDefaultColor();
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getBoundingBox().getCollisionVertices();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.CIRCLE;
    }

    @Override
    public CollidableType getCollidableType() {
        return null;
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        this.shape.translateShape(new Vector(shape.getCenter(), newCenter));
    }
}
