package bham.student.txm683.heartbreaker.pickups;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.fields.InteractionField;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Pickup extends InteractionField implements Renderable {

    private PickupType pickupType;

    private Rectangle shape;

    public Pickup(String name, PickupType pickupType, Point center, int size) {
        super(name, name);

        this.pickupType = pickupType;

        int color;

        switch (pickupType){

            case KEY:
                //yellow
                color = Color.rgb(255,255,51);
                break;
            case BOMB:
                //red ish
                color = Color.rgb(255, 102,102);
                break;
            case HEALTH:
                //green ish
                color = Color.rgb(102,255,178);
                break;
            default:
                color = Color.BLACK;
                break;
        }

        this.shape = new Rectangle(center, size, size, color);
    }

    public Pickup(String name, PickupType type, Point center){
        this(name, type, center, 50);
    }

    public static Pickup build(JSONObject jsonObject) throws JSONException {
        String name = jsonObject.getString("name");
        Point center = new Point(jsonObject.getJSONObject("center"));
        PickupType type = PickupType.valueOf(jsonObject.getString("type"));
        return new Pickup(name, type, center);
    }

    public JSONObject pack() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", getName());
        jsonObject.put("center", getCenter().getStateObject());

        jsonObject.put("type", pickupType.toString());

        return jsonObject;
    }

    public PickupType getPickupType() {
        return pickupType;
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
    public BoundingBox getBoundingBox() {
        return shape.getBoundingBox();
    }

    @Override
    public Point[] getCollisionVertices() {
        return shape.getVertices();
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.RECTANGLE;
    }

    @Override
    public Point getCenter() {
        return shape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {
        shape.translateShape(new Vector(shape.getCenter(), newCenter));
    }

    @Override
    public boolean canMove() {
        return false;
    }
}
