package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import bham.student.txm683.heartbreaker.SaveableState;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public abstract class EntityShape implements SaveableState {

    ShapeIdentifier shapeIdentifier;

    Vector[] vertexVectors;
    Vector relativeUpUnitVector;
    Point geometricCenter;
    float height;
    float width;

    private Paint paint;
    private int defaultColor;

    EntityShape(){

    }

    EntityShape(Point geometricCenter, float width, float height, int colorValue){
        this.geometricCenter = geometricCenter;

        this.height = height;
        this.width = width;

        this.defaultColor = colorValue;

        this.paint = new Paint();
        this.paint.setColor(defaultColor);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);
    }

    EntityShape(String jsonString) throws JSONException, ParseException {
        JSONObject jsonObject = new JSONObject(jsonString);

        this.geometricCenter = Point.createPointFromStateString((String)jsonObject.get("center"));

        this.height = Float.parseFloat((String)jsonObject.get("height"));
        this.width = Float.parseFloat((String)jsonObject.get("width"));

        this.defaultColor = Integer.parseInt((String)jsonObject.get("defaultcolor"));

        this.paint = new Paint();
        this.paint.setColor(Integer.parseInt((String)jsonObject.get("color")));
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);

        this.relativeUpUnitVector = Vector.createVectorFromStateString((String)jsonObject.get("upvector"));

        JSONArray verticesArray = new JSONArray((String)jsonObject.get("vertices"));
        this.vertexVectors = new Vector[verticesArray.length()];

        for (int i = 0; i < verticesArray.length(); i++){
            this.vertexVectors[i] = new Vector(geometricCenter, Point.createPointFromStateString((String) verticesArray.get(i)));
        }
    }

    public abstract void setHeight(float newHeight);

    public abstract void setWidth(float newWidth);

    public abstract void setRelativeUpUnitVector();

    public void draw(Canvas canvas, Vector interpolationVector) {
        Path path;

        if (interpolationVector.equals(new Vector())) {
            //Log.d(TAG+":draw", "zero interpol vector");
            path = Point.getPathWithPoints(getVertices());
        } else {
            //Log.d(TAG+":draw", interpolationVector.toString());
            path = Point.getPathWithPoints(getInterpolatedVertices(interpolationVector));
        }

        canvas.drawPath(path, this.paint);
    }

    public void move(Vector movementVector){
        rotateVertices(movementVector);
        translateShape(movementVector);
    }

    public void setCenter(Point geometricCenter){
        translateShape(new Vector(this.geometricCenter, geometricCenter));
    }

    public Point getInterpolatedCenter(Vector interpolationVector){
        return this.geometricCenter.add(interpolationVector.getRelativeToTailPoint());
    }

    public Point[] getVertices() {
        Point[] vertices = new Point[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead();
        }
        return vertices;
    }

    private Point[] getInterpolatedVertices(Vector interpolationVector){

        if (!interpolationVector.equals(new Vector())) {
            Point amountToAdd = interpolationVector.getRelativeToTailPoint();

            float angle = calculateAngleBetweenVectors(relativeUpUnitVector, interpolationVector);

            float cosAngle = (float) Math.cos(angle);

            float sinAngle = (float) Math.sin(angle);

            Point[] interpolatedVertices = new Point[vertexVectors.length];
            for (int i = 0; i < vertexVectors.length; i++){
                interpolatedVertices[i] = rotateVertexVector(vertexVectors[i], cosAngle, sinAngle).translate(amountToAdd).getHead();
            }
            return interpolatedVertices;
        }
        return getVertices();
    }

    private void rotateVertices(Vector movementVector){

        float angle = calculateAngleBetweenVectors(relativeUpUnitVector, movementVector);

        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);


        if (!(Math.abs(cosAngle - 1f) < 0.0001f)) {

            for (int i = 0; i < vertexVectors.length; i++){
                vertexVectors[i] = rotateVertexVector(vertexVectors[i], cosAngle, sinAngle);
            }
        }

        setRelativeUpUnitVector();
    }

    /**
     * Translates the vertices of this shape by the given movement vector.
     * @param movementVector The vector to move the vertices.
     */
    private void translateShape(Vector movementVector){
        Point amountToTranslate = movementVector.getRelativeToTailPoint();

        this.geometricCenter = this.geometricCenter.add(amountToTranslate);

        for (int i = 0; i < vertexVectors.length; i++){
            vertexVectors[i] = vertexVectors[i].translate(amountToTranslate);
        }

        setRelativeUpUnitVector();
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

    float calculateAngleBetweenVectors(Vector primaryVector, Vector movementVector){
        float dot = primaryVector.dot(movementVector);
        float det = primaryVector.det(movementVector);

        return (float) Math.atan2(det, dot);
    }

    public Point getCenter(){
        return geometricCenter;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
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

    @NonNull
    @Override
    public String toString() {
        StringBuilder vertices = new StringBuilder();
        for (Point vertex : getVertices()){
            vertices.append(vertex.toString());
            vertices.append(", ");
        }
        return this.getClass().getName() + "{" +
                "center: " + geometricCenter.toString() +
                ", vertices: " + vertices.toString() +
                "}";
    }

    JSONObject getAbstractJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        String[] vertices = new String[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead().getStateString();
        }
        jsonObject.put("vertices", vertices);
        jsonObject.put("upvector", relativeUpUnitVector.getHead().getStateString());
        jsonObject.put("center", geometricCenter.getStateString());
        jsonObject.put("height", height);
        jsonObject.put("width",width);
        jsonObject.put("color", paint.getColor());
        jsonObject.put("defaultcolor", defaultColor);

        return jsonObject;
    }

    public ShapeIdentifier getShapeIdentifier() {
        return shapeIdentifier;
    }
}
