package bham.student.txm683.heartbreaker.entities.entityshapes;

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
    }

    public Rectangle(String stateString) throws JSONException {
        super(stateString, ShapeIdentifier.RECT);

        JSONObject jsonObject = new JSONObject(stateString);

        angleBetweenUpAndPrimaryVectors = Float.parseFloat(jsonObject.getString("anglebetweenupandprimaryvectors"));
    }

    @Override
    public void setForwardUnitVector() {

        this.forwardUnitVector = rotateVertexVector(vertexVectors[0], (float) Math.cos(angleBetweenUpAndPrimaryVectors), (float) Math.sin(angleBetweenUpAndPrimaryVectors));
    }

    /**
     * Sets the height of the shape to a new value
     * @param newHeight The new height of the shape
     */
    //explanation in notebook [2]
    @Override
    public void setHeight(float newHeight) {
        float changeInHeight = newHeight - height;
        Vector changeInHeightVectorUp = forwardUnitVector.sMult(changeInHeight/2);
        Vector changeInHeightVectorDown = changeInHeightVectorUp.sMult(-1);

        vertexVectors[0] = vertexVectors[0].vAdd(changeInHeightVectorUp);
        vertexVectors[1] = vertexVectors[1].vAdd(changeInHeightVectorUp);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInHeightVectorDown);
        vertexVectors[3] = vertexVectors[3].vAdd(changeInHeightVectorDown);

        this.height = newHeight;
    }

    //explanation in notebook [2]
    @Override
    public void setWidth(float newWidth) {
        float changeInWidth = newWidth - width;
        Vector changeInWidthVectorLeft = forwardUnitVector.rotateAntiClockwise90().sMult(changeInWidth/2);
        Vector changeInWidthVectorRight = changeInWidthVectorLeft.sMult(-1);

        vertexVectors[0] = vertexVectors[0].vAdd(changeInWidthVectorLeft);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInWidthVectorRight);
        vertexVectors[1] = vertexVectors[1].vAdd(changeInWidthVectorRight);
        vertexVectors[3] = vertexVectors[3].vAdd(changeInWidthVectorLeft);

        this.width = newWidth;
    }

    @Override
    public Point[] getCollisionVertices() {
        return getVertices();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = this.getJSONObject();
        jsonObject.put("anglebetweenupandprimaryvectors", angleBetweenUpAndPrimaryVectors);

        return jsonObject;
    }
}
