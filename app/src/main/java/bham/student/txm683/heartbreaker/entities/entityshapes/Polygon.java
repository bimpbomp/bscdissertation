package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.Log;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Polygon extends EntityShape {

    Vector[] vertexVectors;
    float height;
    float width;

    public Polygon(Point center, float width, float height, int colorValue, ShapeIdentifier shapeIdentifier){
        super(center, colorValue, shapeIdentifier);

        this.height = height;
        this.width = width;
    }

    public Polygon(String jsonString, ShapeIdentifier shapeIdentifier) throws JSONException {
        super(jsonString, shapeIdentifier);

        JSONObject jsonObject = new JSONObject(jsonString);

        Log.d("hb::Polygon", jsonString);

        this.height = Float.parseFloat(jsonObject.getString("height"));
        this.width = Float.parseFloat(jsonObject.getString("width"));

        JSONArray verticesArray = jsonObject.getJSONArray("vertices");

        this.vertexVectors = new Vector[verticesArray.length()];
        for (int i = 0; i < verticesArray.length(); i++){
            this.vertexVectors[i] = new Vector(geometricCenter, new Point(verticesArray.getJSONObject(i)));
        }
    }

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

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public Point[] getCollisionVertices() {
        return getVertices();
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

    public void setCenter(Point geometricCenter){
        translateShape(new Vector(this.geometricCenter, geometricCenter));
    }

    private Point[] getInterpolatedVertices(Vector interpolationVector, Point renderOffset){

        if (!interpolationVector.equals(new Vector())) {
            Point amountToAdd = interpolationVector.getRelativeToTailPoint();

            float angle = calculateAngleBetweenVectors(forwardUnitVector, interpolationVector);

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

    @Override
    public void rotateShape(Vector movementVector){

        float angle = calculateAngleBetweenVectors(forwardUnitVector, movementVector);

        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);


        if (!(Math.abs(cosAngle - 1f) < 0.0001f)) {

            for (int i = 0; i < vertexVectors.length; i++){
                vertexVectors[i] = rotateVertexVector(vertexVectors[i], cosAngle, sinAngle);
            }
        }

        setForwardUnitVector();
    }

    /**
     * Translates the vertices of this shape by the given movement vector.
     * @param movementVector The vector to move the vertices.
     */
    @Override
    public void translateShape(Vector movementVector){
        Point amountToTranslate = movementVector.getRelativeToTailPoint();

        this.geometricCenter = this.geometricCenter.add(amountToTranslate);

        for (int i = 0; i < vertexVectors.length; i++){
            vertexVectors[i] = vertexVectors[i].translate(amountToTranslate);
        }

        setForwardUnitVector();
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

    @Override
    JSONObject getJSONObject() throws JSONException {
        JSONObject jsonObject = super.getJSONObject();

        JSONObject[] vertices = new JSONObject[vertexVectors.length];
        for (int i = 0; i < vertexVectors.length; i++){
            vertices[i] = vertexVectors[i].getHead().getStateObject();
        }
        jsonObject.put("vertices", new JSONArray(vertices));
        jsonObject.put("height", height);
        jsonObject.put("width",width);

        return jsonObject;
    }
}
