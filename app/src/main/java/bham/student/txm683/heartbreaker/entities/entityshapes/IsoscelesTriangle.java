package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.physics.CollisionOutline;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class IsoscelesTriangle extends Polygon {

    private static final String TAG = "hb::TRIANGLE";
    private static final float TWO_THIRDS_CONSTANT = 0.6667f;

    public IsoscelesTriangle(Point geometricCenter, float baseWidth, float height, int colorValue){
        super(geometricCenter, baseWidth, height, colorValue, ShapeIdentifier.ISO_TRIANGLE);

        this.vertexVectors = new Vector[3];

        float twoThirdsHeight = this.height * TWO_THIRDS_CONSTANT;

        this.vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY() - twoThirdsHeight));

        float baseY = geometricCenter.getY() + (height-twoThirdsHeight);

        this.vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX() + (width / 2), baseY));
        this.vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX() - (width / 2), baseY));

        setForwardUnitVector();
        this.collisionOutline = new CollisionOutline(vertexVectors);
    }

    public IsoscelesTriangle(String jsonString) throws JSONException {
        super(jsonString, ShapeIdentifier.ISO_TRIANGLE);
    }

    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].getUnitVector();
    }

    //explanation in notebook [2]
    @Override
    public void setHeight(float newHeight) {
        float changeInHeight = newHeight - height;
        Vector changeInHeightVectorUp = forwardUnitVector.sMult(changeInHeight * TWO_THIRDS_CONSTANT);
        Vector changeInHeightVectorDown = forwardUnitVector.sMult(changeInHeight * (1-TWO_THIRDS_CONSTANT) * -1);

        vertexVectors[0] = vertexVectors[0].vAdd(changeInHeightVectorUp);
        vertexVectors[1] = vertexVectors[1].vAdd(changeInHeightVectorDown);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInHeightVectorDown);

        this.height = newHeight;
    }

    //explanation in notebook [2]
    @Override
    public void setWidth(float newWidth) {
        float changeInHeight = newWidth - width;
        Vector changeInHeightVectorLeft = forwardUnitVector.rotateAntiClockwise90().sMult(changeInHeight/2);
        Vector changeInHeightVectorRight = changeInHeightVectorLeft.sMult(-1);

        vertexVectors[1] = vertexVectors[1].vAdd(changeInHeightVectorRight);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInHeightVectorLeft);

        this.width = newWidth;
    }

    @Override
    public Point[] getCollisionVertices() {
        return getRenderVertices();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        return getJSONObject();
    }


}