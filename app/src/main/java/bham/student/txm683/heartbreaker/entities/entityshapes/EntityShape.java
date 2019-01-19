package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.Log;
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

        Log.d("hb::EntityShape", jsonString);

        this.height = Float.parseFloat(jsonObject.getString("height"));
        this.width = Float.parseFloat(jsonObject.getString("width"));
        this.geometricCenter = new Point(jsonObject.getJSONObject("center"));



        this.defaultColor = jsonObject.getInt("defaultcolor");

        this.paint = new Paint();
        this.paint.setColor(jsonObject.getInt("color"));
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);

        this.relativeUpUnitVector = new Vector(geometricCenter, new Point(jsonObject.getJSONObject("upvector")));

        JSONArray verticesArray = jsonObject.getJSONArray("vertices");
        this.vertexVectors = new Vector[verticesArray.length()];

        for (int i = 0; i < verticesArray.length(); i++){
            this.vertexVectors[i] = new Vector(geometricCenter, new Point(verticesArray.getJSONObject(i)));
        }
    }

    public abstract void setHeight(float newHeight);

    public abstract void setWidth(float newWidth);

    public abstract void setRelativeUpUnitVector();

    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector) {
        Path path;

        if (interpolationVector.equals(new Vector())) {
            //Log.d(TAG+":draw", "zero interpol vector");
            path = getPathWithPoints(getVertices(renderOffset));
        } else {
            //Log.d(TAG+":draw", interpolationVector.toString());
            path = getPathWithPoints(getInterpolatedVertices(interpolationVector, renderOffset));
        }

        canvas.drawPath(path, this.paint);
    }

    private static Path getPathWithPoints(Point[] points){
        Path path = new Path();

        if (points.length > 0) {
            path.moveTo(points[0].getX(), points[0].getY());

            for (Point point : points) {
                path.lineTo(point.getX(), point.getY());
            }
        }
        path.close();
        return path;
    }

    public void move(Vector movementVector){
        rotateVertices(movementVector);
        translateShape(movementVector);
    }

    public void setCenter(Point geometricCenter){
        translateShape(new Vector(this.geometricCenter, geometricCenter));
    }

    public Point getInterpolatedCenter(Vector interpolationVector, Point renderOffset){
        return this.geometricCenter.add(interpolationVector.getRelativeToTailPoint()).add(renderOffset);
    }

    public void scaleKeepingRatios(float scaleByProportion){
        scaleByProportion = Math.abs(scaleByProportion);

        float heightChange = height*scaleByProportion;
        float widthChange = width*scaleByProportion;

        this.setHeight(heightChange);
        this.setWidth(widthChange);
    }

    public Point[] getVertices() {
        Point[] vertices = new Point[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead();
        }
        return vertices;
    }

    public Point[] getVertices(Point renderOffset) {
        Point[] vertices = new Point[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead().add(renderOffset);
        }
        return vertices;
    }

    private Point[] getInterpolatedVertices(Vector interpolationVector, Point renderOffset){

        if (!interpolationVector.equals(new Vector())) {
            Point amountToAdd = interpolationVector.getRelativeToTailPoint();

            float angle = calculateAngleBetweenVectors(relativeUpUnitVector, interpolationVector);

            float cosAngle = (float) Math.cos(angle);

            float sinAngle = (float) Math.sin(angle);

            Point[] interpolatedVertices = new Point[vertexVectors.length];
            for (int i = 0; i < vertexVectors.length; i++){
                interpolatedVertices[i] = rotateVertexVector(vertexVectors[i], cosAngle, sinAngle).translate(amountToAdd).getHead().add(renderOffset);
            }
            return interpolatedVertices;
        }
        return getVertices(renderOffset);
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

        JSONObject[] vertices = new JSONObject[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead().getStateObject();
        }
        jsonObject.put("vertices", new JSONArray(vertices));
        jsonObject.put("upvector", relativeUpUnitVector.getHead().getStateObject());
        jsonObject.put("center", geometricCenter.getStateObject());
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
