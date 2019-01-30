package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class IsoscelesTrapezium extends Polygon {

    private float bottomWidth;
    private float topWidth;
    private float angleBetweenUpAndPrimaryVectors;

    public IsoscelesTrapezium(Point geometricCenter, float topWidth, float bottomWidth, float height, int colorValue){
        super(geometricCenter, Math.max(topWidth, bottomWidth), height, colorValue, ShapeIdentifier.ISO_TRAPEZIUM);

        this.topWidth = topWidth;
        this.bottomWidth = bottomWidth;

        float halfHeight = height/2f;

        this.vertexVectors = new Vector[4];

        vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX()-topWidth/2f, geometricCenter.getY() - halfHeight));
        vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX()+topWidth/2f, geometricCenter.getY() - halfHeight));
        vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX()+bottomWidth/2f, geometricCenter.getY() + halfHeight));
        vertexVectors[3] = new Vector(geometricCenter, new Point(geometricCenter.getX()-bottomWidth/2f, geometricCenter.getY() + halfHeight));

        this.forwardUnitVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY()-1));

        this.angleBetweenUpAndPrimaryVectors = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    public IsoscelesTrapezium(String stateString) throws JSONException {
        super(stateString, ShapeIdentifier.ISO_TRAPEZIUM);
        JSONObject jsonObject = new JSONObject(stateString);
        this.bottomWidth = (float) jsonObject.getDouble("bottomwidth");
        this.topWidth = (float) jsonObject.getDouble("topwidth");

        this.angleBetweenUpAndPrimaryVectors = (float) jsonObject.getDouble("anglebetweenprimaryandup");
    }

    @Override
    public void contract(float changeInHeightRatio) {
        //make sure proportion is positive
        changeInHeightRatio = Math.abs(changeInHeightRatio);

        Point oldBottomLeftVertex = vertexVectors[3].getHead();

        setHeight(contractionHeight * changeInHeightRatio);

        //moves the shape back to the same base position as before height changed
        translateShape(new Vector(vertexVectors[3].getHead(), oldBottomLeftVertex));
    }

    @Override
    public void returnToNormal() {
        Point oldBottomLeftVertex = vertexVectors[3].getHead();
        setHeight(height);

        translateShape(new Vector(vertexVectors[3].getHead(), oldBottomLeftVertex));

        resetToDefaultColor();
    }

    @Override
    public void setHeight(float newHeight) {
        float changeInHeight = newHeight - contractionHeight;

        //same formula as a rectangle for changing height
        Rectangle.rectOrTrapeziumChangeHeight(changeInHeight, forwardUnitVector, vertexVectors);
        this.contractionHeight = newHeight;
        this.angleBetweenUpAndPrimaryVectors = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = rotateVertexVector(vertexVectors[0], (float) Math.cos(angleBetweenUpAndPrimaryVectors), (float) Math.sin(angleBetweenUpAndPrimaryVectors)).getUnitVector();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = getJSONObject();
        jsonObject.put("topwidth", topWidth);
        jsonObject.put("bottomwidth", bottomWidth);
        jsonObject.put("anglebetweenprimaryandup", angleBetweenUpAndPrimaryVectors);
        return jsonObject;
    }
}
