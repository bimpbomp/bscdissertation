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

    EntityShape(){

    }

    /**
     * Creates an EntityShape with center at the given coordinates.
     * @param geometricCenter Point for the center of the shape.
     * @param colorValue Color.constant for shape color.
     */
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

    /**
     * Creates an EntityShape object from the given JSON formatted String.
     * @param jsonString String in JSON format
     * @throws JSONException Thrown if the required members cannot be extracted from the jsonString
     */
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

    /**
     * Draws the shape to the given canvas.
     * InterpolationVector of zero will render the shape as it was on the last game tick
     * @param canvas Canvas to draw to.
     * @param renderOffset Offset to translate shape's world coordinates into screen coordinates. Given as a negative value
     * @param interpolationVector Vector describing movement direction
     */
    public abstract void draw(Canvas canvas, Point renderOffset, Vector interpolationVector);

    public abstract void translateShape(Vector translationVector);

    public abstract void rotateShape(Vector rotationVector);

    public void contractHeight(float proportionOfHeight){

    }

    public void contractWidth(float proportionOfWidth){

    }

    public void returnToNormal(){

    }

    public void setCenter(Point geometricCenter){
        translateShape(new Vector(this.geometricCenter, geometricCenter));
    }

    /**
     * Interpolates the shapes center for rendering, and applies the given renderOffset.
     * Doesn't update the shapes stored value for it's center
     * @param interpolationVector Vector describing movement direction
     * @param renderOffset Offset to translate shape's world coordinates into screen coordinates. Given as a negative value
     * @return The interpolated center for this shape after adding the interpolationVector and renderOffset
     */
    public Point getInterpolatedCenter(Vector interpolationVector, Point renderOffset){
        return this.geometricCenter.add(interpolationVector.getRelativeToTailPoint()).add(renderOffset);
    }

    /**
     * Rotates, then translates the shape in the direction of the given vector.
     * Rotation makes the shapes forward vector point in the same direction as the movementVector.
     * @param movementVector Vector for use in rotation and translation
     */
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

    /**
     * Serialises the EntityShape into a JSON object.
     * Since this class is abstract, this function is called by the extending classes as part
     * of the serialisation to JSON format.
     * @return JSONObject containing the members of this class.
     * @throws JSONException Throws JSON exception if the object cannot be serialised.
     */
    JSONObject getJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("forwardvector", forwardUnitVector.getHead().getStateObject());
        jsonObject.put("center", geometricCenter.getStateObject());
        jsonObject.put("color", paint.getColor());
        jsonObject.put("defaultcolor", defaultColor);

        return jsonObject;
    }

    float calculateAngleBetweenVectors(Vector primaryVector, Vector movementVector){
        float dot = primaryVector.dot(movementVector);
        float det = primaryVector.det(movementVector);

        return (float) Math.atan2(det, dot);
    }

    Vector rotateVertexVector(Vector vectorToRotate, float cosAngle, float sinAngle){
        if (!vectorToRotate.isFromOrigin()){
            vectorToRotate = vectorToRotate.translate(geometricCenter.smult(-1));
            vectorToRotate = vectorToRotate.rotate(cosAngle, sinAngle);
            return vectorToRotate.translate(geometricCenter);
        } else {
            return vectorToRotate.rotate(cosAngle, sinAngle);
        }
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
