package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.physics.CollisionOutline;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Rectangle extends Polygon {
    private float angleBetweenUpAndPrimaryVectors;

    public Rectangle(Point geometricCenter, float width, float height, int colorValue){
        super(geometricCenter, width, height, colorValue, ShapeIdentifier.RECT);

        this.vertexVectors = new Vector[4];

        float halfWidth = width/2f;
        float halfHeight = height/2f;

        vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY() - halfHeight));
        vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY() - halfHeight));
        vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY() + halfHeight));
        vertexVectors[3] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY() + halfHeight));

        this.forwardUnitVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY()-1));

        this.angleBetweenUpAndPrimaryVectors = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
        this.collisionOutline = new CollisionOutline(vertexVectors);
    }

    public Rectangle(String stateString) throws JSONException {
        super(stateString, ShapeIdentifier.RECT);

        JSONObject jsonObject = new JSONObject(stateString);

        angleBetweenUpAndPrimaryVectors = Float.parseFloat(jsonObject.getString("anglebetweenupandprimaryvectors"));
    }

    @Override
    public void setForwardUnitVector() {

        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(angleBetweenUpAndPrimaryVectors), (float) Math.sin(angleBetweenUpAndPrimaryVectors));
    }

    /**
     * Sets the height of the shape to a new value
     * @param newHeight The new height of the shape
     */
    //explanation in notebook [2]
    @Override
    public void setHeight(float newHeight) {

        float changeInHeight = newHeight - height;
        this.height = newHeight;

        rectOrTrapeziumChangeHeight(changeInHeight, forwardUnitVector, vertexVectors);
        this.angleBetweenUpAndPrimaryVectors = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    //explanation in notebook [2]
    @Override
    public void setWidth(float newWidth) {
        float changeInWidth = newWidth - width;

        Polygon.rectOrTrapeziumChangeWidth(changeInWidth, forwardUnitVector, vertexVectors);

        this.angleBetweenUpAndPrimaryVectors = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);

        this.width = newWidth;
    }

    @Override
    public Point[] getCollisionVertices() {
        return getRenderVertices();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = this.getJSONObject();
        jsonObject.put("anglebetweenupandprimaryvectors", angleBetweenUpAndPrimaryVectors);

        return jsonObject;
    }
}
