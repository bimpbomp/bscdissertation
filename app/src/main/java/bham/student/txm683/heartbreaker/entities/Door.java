package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.fields.DoorField;
import bham.student.txm683.heartbreaker.physics.fields.InteractionField;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import org.json.JSONException;
import org.json.JSONObject;

public class Door extends Entity implements Renderable {

    private Rectangle doorShape;

    private Rectangle lockedSymbol;

    private boolean locked;

    private boolean open;

    private DoorField field;

    private int doorSet;
    private Tile sideSets;

    private static final int LOCKED_COLOR = Color.rgb( 255, 0 ,0);
    private static final int UNLOCKED_COLOR = Color.rgb( 0, 255, 0);

    private static final float DOOR_RATIO = 0.5f;

    private int width, height;
    private boolean vertical;

    public Door(int doorID, Point center, int width, int height, boolean locked,
                boolean vertical, int doorColor, int doorSet, Tile sideSets){

        super("D"+doorID);

        this.doorSet = doorSet;
        this.sideSets = sideSets;

        this.locked = locked;
        this.lockedSymbol = new Rectangle(center, width * 0.25f, height * 0.25f, locked? LOCKED_COLOR : UNLOCKED_COLOR);

        setLocked(locked);

        this.open = false;

        if (vertical) {
            this.doorShape = new Rectangle(center, width * DOOR_RATIO, height, doorColor);
        } else {
            this.doorShape = new Rectangle(center, width, height * DOOR_RATIO, doorColor);
        }

        this.field = new DoorField(getName(), "F"+doorID, center, width, height, Color.GRAY);

        this.width = width;
        this.height = height;
        this.vertical = vertical;
    }

    public static Door build(JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        Point center = new Point(jsonObject.getJSONObject("center"));
        int width = jsonObject.getInt("width");
        int height = jsonObject.getInt("height");
        int color = jsonObject.getInt("color");
        boolean locked = jsonObject.getBoolean("locked");
        boolean vertical = jsonObject.getBoolean("vertical");
        int doorSet = jsonObject.getInt("door_set");
        Tile sideSets = new Tile(jsonObject.getJSONObject("side_sets"));

        return new Door(id, center, width, height, locked, vertical, color, doorSet, sideSets);
    }

    public JSONObject pack() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getDoorID());
        jsonObject.put("center", getCenter().getStateObject());
        jsonObject.put("locked", locked);
        jsonObject.put("vertical", vertical);
        jsonObject.put("side_sets", sideSets.getStateObject());
        jsonObject.put("door_set", doorSet);
        jsonObject.put("color", doorShape.getColor());
        jsonObject.put("width", width);
        jsonObject.put("height", height);

        return jsonObject;
    }

    public int getDoorSet() {
        return doorSet;
    }

    public Tile getSideSets() {
        return sideSets;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {

        doorShape.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        lockedSymbol.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));
    }

    @Override
    public BoundingBox getBoundingBox() {
        return field.getBoundingBox();
    }

    @Override
    public void setColor(int color) {
        doorShape.setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        doorShape.revertToDefaultColor();
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public Point[] getCollisionVertices() {
        return doorShape.getVertices();
    }

    public InteractionField getPrimaryField() {
        return field;
    }

    @Override
    public boolean isSolid() {
        return !open;
    }

    @Override
    public ShapeIdentifier getShapeIdentifier() {
        return ShapeIdentifier.RECTANGLE;
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.DOOR;
    }

    @Override
    public Point getCenter() {
        return doorShape.getCenter();
    }

    @Override
    public void setCenter(Point newCenter) {

    }

    public void setLocked(boolean locked) {
        this.locked = locked;

        if (locked)
            lockedSymbol.setColor(LOCKED_COLOR);
        else
            lockedSymbol.setColor(UNLOCKED_COLOR);
    }

    public void setOpen(boolean open) {
        this.open = open;

        if (!locked) {
            if (open) {
                doorShape.setColor(Color.TRANSPARENT);
                lockedSymbol.setColor(Color.TRANSPARENT);
            } else {
                doorShape.revertToDefaultColor();
                lockedSymbol.revertToDefaultColor();
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isUnlocked(){
        return !locked;
    }

    public int getDoorID(){
        return Integer.parseInt(getName().substring(1));
    }
}