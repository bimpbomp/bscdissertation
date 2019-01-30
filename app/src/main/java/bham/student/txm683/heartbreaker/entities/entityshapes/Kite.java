package bham.student.txm683.heartbreaker.entities.entityshapes;

import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Kite extends Polygon {
    private float upperSectorRatio;

    public Kite(Point geometricCenter, float width, float upperHeight, float lowerHeight, int colorValue){
        super(geometricCenter, width, upperHeight+lowerHeight, colorValue, ShapeIdentifier.KITE);

        this.upperSectorRatio = upperHeight / height;

        this.vertexVectors = new Vector[4];

        float halfWidth = width/2f;

        vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY() - upperHeight));
        vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY()));
        vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY() + lowerHeight));
        vertexVectors[3] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY()));

        this.forwardUnitVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY()-1));
    }

    public Kite(String stateString) throws JSONException{
        super(stateString, ShapeIdentifier.KITE);
        JSONObject jsonObject = new JSONObject(stateString);

        this.upperSectorRatio = Float.parseFloat(jsonObject.getString("uppersectorratio"));
    }

    @Override
    public void setHeight(float newHeight) {

    }

    @Override
    public void setWidth(float newWidth) {

    }

    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].getUnitVector();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = this.getJSONObject();
        jsonObject.put("uppersectorratio", upperSectorRatio);
        return jsonObject;
    }
}
