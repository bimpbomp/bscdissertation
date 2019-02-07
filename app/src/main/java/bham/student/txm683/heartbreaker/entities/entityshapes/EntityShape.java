/*
package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import bham.student.txm683.heartbreaker.SaveableState;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class EntityShape implements SaveableState {

    String TAG;

    ShapeIdentifier shapeIdentifier;

    Vector forwardUnitVector;
    Point geometricCenter;

    Paint paint;
    int defaultColor;

    EntityShape(Point geometricCenter, int colorValue, ShapeIdentifier shapeIdentifier){
        TAG = "hb::" + this.getClass().getSimpleName();

        this.geometricCenter = geometricCenter;

        this.defaultColor = colorValue;

        this.paint = new Paint();
        this.paint.setColor(defaultColor);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);

        this.shapeIdentifier = shapeIdentifier;
    }

    EntityShape(String jsonString, ShapeIdentifier shapeIdentifier) throws JSONException {
        TAG = "hb::" + this.getClass().getSimpleName();

        JSONObject jsonObject = new JSONObject(jsonString);

        Log.d("hb::EntityShape", jsonString);

        this.geometricCenter = new Point(jsonObject.getJSONObject("center"));

        this.defaultColor = jsonObject.getInt("defaultcolor");

        this.paint = new Paint();
        this.paint.setColor(jsonObject.getInt("color"));
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);

        this.forwardUnitVector = new Vector(geometricCenter, new Point(jsonObject.getJSONObject("forwardvector")));

        this.shapeIdentifier = shapeIdentifier;
    }

    public abstract void setHeight(float newHeight);

    public abstract float getHeight();

    public abstract void setWidth(float newWidth);

    public abstract float getWidth();

    public abstract void setForwardUnitVector();

    public Vector getForwardUnitVector() {
        return forwardUnitVector;
    }

    public abstract Point[] getCollisionVertices();

    public abstract void draw(Canvas canvas, Point renderOffset, Vector interpolationVector);

    public abstract void translateShape(Vector translationVector);

    public abstract void rotateShape(Vector rotationVector);

    public void setCenter(Point geometricCenter){
        translateShape(new Vector(this.geometricCenter, geometricCenter));
    }

    public Point getInterpolatedCenter(Vector interpolationVector, Point renderOffset){
        return this.geometricCenter.add(interpolationVector.getRelativeToTailPoint()).add(renderOffset);
    }

    public void move(Vector movementVector){
        rotateShape(movementVector);
        translateShape(movementVector);
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "center: " + geometricCenter.toString() +
                "}";
    }

    JSONObject getJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("forwardvector", forwardUnitVector.getHead().getStateObject());
        jsonObject.put("center", geometricCenter.getStateObject());
        jsonObject.put("color", paint.getColor());
        jsonObject.put("defaultcolor", defaultColor);

        return jsonObject;
    }

    public Point getCenter(){
        return geometricCenter;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint(){
        return this.paint;
    }

    public void setColor(int colorValue){
        this.paint.setColor(colorValue);
    }

    public void resetToDefaultColor(){
        this.paint.setColor(defaultColor);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public ShapeIdentifier getShapeIdentifier() {
        return shapeIdentifier;
    }
}
*/
