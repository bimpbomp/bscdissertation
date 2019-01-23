package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.SaveableState;
import bham.student.txm683.heartbreaker.entities.entityshapes.*;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class Entity implements SaveableState {
    String TAG;

    String name;
    EntityShape shape;

    Paint textPaint;

    boolean moveable;

    boolean collided;

    public Entity(String name, Point spawnCoordinates, ShapeIdentifier shapeIdentifier, int width, int height, int colorValue){
        this.name = name;
        this.TAG = "hb::" + this.getClass().getName() + ":" + name;

        switch (shapeIdentifier){

            case ISO_TRIANGLE:
                this.shape = new IsoscelesTriangle(spawnCoordinates, width, height, colorValue);
                break;
            case RECT:
                this.shape = new Rectangle(spawnCoordinates, width, height, colorValue);
                break;
            case CIRCLE:
                this.shape = new Circle(spawnCoordinates, width/2f, colorValue);
                break;
        }
        moveable = false;

        initTextPaint();
    }

    public Entity(String name, EntityShape shape){
        this.name = name;
        this.TAG = "hb::" + this.getClass().getName() + ":" + name;

        this.shape = shape;

        moveable = false;

        initTextPaint();
    }

    public Entity(String stateString) throws ParseException, JSONException {
        JSONObject jsonObject = new JSONObject(stateString);

        this.name = jsonObject.getString("name");
        this.TAG = "hb::" + this.getClass().getName() + ":" + name;

        try {
            switch (ShapeIdentifier.fromInt(jsonObject.getInt("shapeidentifier"))) {
                case ISO_TRIANGLE:
                    this.shape = new IsoscelesTriangle((String)jsonObject.get("shape"));
                    break;
                case RECT:
                    this.shape = new Rectangle(jsonObject.getString("shape"));
                    break;
                default:
                    throw new ParseException("Invalid shape identifier", 0);
            }
        } catch (JSONException e){
            throw new ParseException(name + e.getMessage(),0);
        }

        moveable = false;

        initTextPaint();
    }

    public boolean canMove() {
        return moveable;
    }

    private void initTextPaint(){
        textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setStrokeWidth(15f);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
    * Draws the entity to the given canvas
    * @param canvas Canvas to draw to.
    */
    public void draw(Canvas canvas, Point renderOffset){

        if (collided){
            shape.setColor(Color.RED);
            collided = false;
        } else {
            shape.resetToDefaultColor();
        }

        shape.draw(canvas, renderOffset, new Vector());

        Point center = shape.getCenter().add(renderOffset);
        canvas.drawText(name, center.getX(), center.getY(), textPaint);
    }

    public String getName(){
        return this.name;
    }

    /**
    * Generates a String in JSON for saving state
    * @return String in JSON format
    */
    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", name);
        jsonObject.put("shape", shape.getStateObject());
        jsonObject.put("shapeidentifier", shape.getShapeIdentifier().getId());
        return jsonObject;
    }

    public boolean isCollided() {
        return collided;
    }

    public void setCollided(boolean collided) {
        this.collided = collided;
    }

    public EntityShape getShape(){
        return this.shape;
    }
}