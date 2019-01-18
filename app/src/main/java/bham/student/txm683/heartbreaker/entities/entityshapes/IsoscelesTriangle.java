package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public class IsoscelesTriangle extends EntityShape {

    private static final String TAG = "hb::TRIANGLE";
    private static final float TWO_THIRDS_CONSTANT = 0.6667f;

    public IsoscelesTriangle(Point geometricCenter, float baseWidth, float height, int colorValue){
        super(geometricCenter, baseWidth, height, colorValue);

        shapeIdentifier = ShapeIdentifier.ISO_TRIANGLE;

        init();
    }

    public IsoscelesTriangle(String jsonString) throws JSONException, ParseException {
        super(jsonString);

        shapeIdentifier = ShapeIdentifier.ISO_TRIANGLE;
    }

    /**
     * Initialises the triangle in zero rotation
     */
    private void init(){
        this.vertexVectors = new Vector[3];

        float twoThirdsHeight = this.height * TWO_THIRDS_CONSTANT;

        this.vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY() - twoThirdsHeight));

        float baseY = geometricCenter.getY() + (height-twoThirdsHeight);

        this.vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX() + (width / 2), baseY));
        this.vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX() - (width / 2), baseY));

        setRelativeUpUnitVector();
    }

    @Override
    public void setRelativeUpUnitVector() {
        this.relativeUpUnitVector = vertexVectors[0].getUnitVector();
    }

    //explanation in notebook [2]
    @Override
    public void setHeight(float newHeight) {
        float changeInHeight = newHeight - height;
        Vector changeInHeightVectorUp = relativeUpUnitVector.sMult(changeInHeight * TWO_THIRDS_CONSTANT);
        Vector changeInHeightVectorDown = relativeUpUnitVector.sMult(changeInHeight * (1-TWO_THIRDS_CONSTANT) * -1);

        vertexVectors[0] = vertexVectors[0].vAdd(changeInHeightVectorUp);
        vertexVectors[1] = vertexVectors[1].vAdd(changeInHeightVectorDown);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInHeightVectorDown);

        this.height = newHeight;
    }

    //explanation in notebook [2]
    @Override
    public void setWidth(float newWidth) {
        float changeInHeight = newWidth - width;
        Vector changeInHeightVectorLeft = relativeUpUnitVector.rotateAntiClockwise90().sMult(changeInHeight/2);
        Vector changeInHeightVectorRight = changeInHeightVectorLeft.sMult(-1);

        vertexVectors[1] = vertexVectors[1].vAdd(changeInHeightVectorRight);
        vertexVectors[2] = vertexVectors[2].vAdd(changeInHeightVectorLeft);

        this.width = newWidth;
    }

    private void updateBaseLengths(float oldWidthSquaredOverFour, float oldOneThirdHeightSquared, float newWidthSquaredOverFour, float newOneThirdHeightSquared){

        float proportionOfBaseVectorLengthChange = (float) Math.sqrt(newOneThirdHeightSquared + newWidthSquaredOverFour)/
                (float) Math.sqrt(oldOneThirdHeightSquared + oldWidthSquaredOverFour);

        this.vertexVectors[1] = this.vertexVectors[1].sMult(proportionOfBaseVectorLengthChange);
        this.vertexVectors[2] = this.vertexVectors[2].sMult(proportionOfBaseVectorLengthChange);
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        return getAbstractJSONObject();
    }
}