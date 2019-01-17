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

    //Explanation in notebook (1)
    @Override
    public void setHeight(float newHeight) {
        float proportionOfHeightChange = newHeight/height;

        this.vertexVectors[0] = this.vertexVectors[0].sMult(proportionOfHeightChange);

        float oneThirdHeightSquared = (this.height/3f)*(this.height/3f);
        float newThirdHeightSquared = (newHeight/3f)/(newHeight/3f);
        float widthSquaredOverFour = (width*width)/4f;

        updateBaseLengths(widthSquaredOverFour, oneThirdHeightSquared,
                widthSquaredOverFour, newThirdHeightSquared);

        this.height = newHeight;
    }

    //explanation in notebook (1)
    @Override
    public void setWidth(float newWidth) {
        float oneThirdHeightSquared = (this.height/3f)*(this.height/3f);
        float widthSquaredOverFour = (width*width)/4f;
        float newWidthSquaredOverFour = (newWidth*newWidth)/4f;

        updateBaseLengths(widthSquaredOverFour, oneThirdHeightSquared, newWidthSquaredOverFour, oneThirdHeightSquared);

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