package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.Log;
import bham.student.txm683.heartbreaker.physics.CollisionOutline;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Polygon extends EntityShape {

    Vector[] vertexVectors;
    float height;
    float width;

    float contractionHeight;
    float contractionWidth;

    CollisionOutline collisionOutline;
    //boolean shapeDifference;

    public Polygon(Point center, float width, float height, int colorValue, ShapeIdentifier shapeIdentifier){
        super(center, colorValue, shapeIdentifier);

        this.height = height;
        this.width = width;

        this.contractionHeight = height;
        this.contractionWidth = width;

        //this.shapeDifference = false;
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

        this.contractionHeight = height;
        this.contractionWidth = width;
    }

    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector) {
        Path path;

        if (interpolationVector.equals(new Vector())) {
            //Log.d(TAG+":draw", "zero interpol vector");
            path = getPathWithPoints(getRenderVertices(renderOffset));
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
    public void setHeight(float newHeight) {
        this.height = newHeight;
    }

    @Override
    public void setWidth(float newWidth) {
        this.width = newWidth;
    }

    @Override
    public Point[] getCollisionVertices() {
        //return collisionOutline.getCollisionVertices();
        return getRenderVertices();
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

    /*public void resetCollisionOutline(){
        this.collisionOutline.setVertexVectors(vertexVectors);
    }*/

    /**
     *
     * @return The vertices of the visible shape in global coordinates
     */
    public Point[] getRenderVertices() {
        return Vector.getVertices(vertexVectors);
    }

    /**
     *
     * @param renderOffset Amount to offset the shape's vertices
     * @return The vertices of the visible shape offset by the given value
     */
    public Point[] getRenderVertices(Point renderOffset) {
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
                interpolatedVertices[i] = vertexVectors[i].rotate(cosAngle, sinAngle).translate(amountToAdd).getHead().add(renderOffset);
            }
            return interpolatedVertices;
        }
        return getRenderVertices(renderOffset);
    }

    /**
     * Rotates the shape to the direction of the given vector
     * @param movementVector Vector to align direction with
     */
    @Override
    public void rotateShape(Vector movementVector){

        float angle = calculateAngleBetweenVectors(forwardUnitVector, movementVector.getUnitVector());

        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        if (!(Math.abs(cosAngle - 1f) < 0.0001f)) {

            for (int i = 0; i < vertexVectors.length; i++){
                vertexVectors[i] = vertexVectors[i].rotate(cosAngle, sinAngle);
                //collisionOutline.setVertexVector(collisionOutline.getVertexVector(i).rotate(cosAngle, sinAngle), i);
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
            //collisionOutline.setVertexVector(collisionOutline.getVertexVector(i).translate(amountToTranslate), i);
        }

        setForwardUnitVector();
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder vertices = new StringBuilder();
        for (Point vertex : getRenderVertices()){
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

    static void rectOrTrapeziumChangeHeight(float changeInHeight, Vector forwardUnitVector, Vector[] vertexVectors){
        Vector changeInHeightVectorUp = forwardUnitVector.sMult(changeInHeight/2);
        Vector changeInHeightVectorDown = changeInHeightVectorUp.sMult(-1);

        vertexVectors[0] = vertexVectors[0].vAdd(changeInHeightVectorUp);
        vertexVectors[1] = vertexVectors[1].vAdd(changeInHeightVectorUp);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInHeightVectorDown);
        vertexVectors[3] = vertexVectors[3].vAdd(changeInHeightVectorDown);
    }

    static void rectOrTrapeziumChangeWidth(float changeInWidth, Vector forwardUnitVector, Vector[] vertexVectors){
        Vector changeInWidthVectorLeft = forwardUnitVector.rotateAntiClockwise90().sMult(changeInWidth/2);
        Vector changeInWidthVectorRight = changeInWidthVectorLeft.sMult(-1);

        vertexVectors[0] = vertexVectors[0].vAdd(changeInWidthVectorLeft);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInWidthVectorRight);
        vertexVectors[1] = vertexVectors[1].vAdd(changeInWidthVectorRight);
        vertexVectors[3] = vertexVectors[3].vAdd(changeInWidthVectorLeft);
    }
}
