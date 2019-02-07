package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Kite extends Polygon implements Renderable {
    private int defaultUpperTriColor;
    private int currentUpperTriColor;
    private int defaultLowerTriColor;
    private int currentLowerTriColor;

    private Paint paint;

    public Kite(Vector[] vertexVectors, int upperTriColor, int lowerTriColor) throws IllegalArgumentException {
        super(vertexVectors, ShapeIdentifier.KITE);

        if (vertexVectors.length != 4){
            throw new IllegalArgumentException("Incorrect Number of vertices given to Kite class." +
                    " 4 vertices are needed, " + vertexVectors.length + " were provided");
        }

        this.defaultUpperTriColor = upperTriColor;
        this.currentUpperTriColor = upperTriColor;

        this.defaultLowerTriColor = lowerTriColor;
        this.currentLowerTriColor = lowerTriColor;

        this.paint = new Paint();
    }

    @Override
    void setForwardUnitVector() {
        this.forwardUnitVector = new Vector(vertexVectors[0].getHead(), vertexVectors[0].getHead().add(new Point(0,-1)));
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        /*
        * Ignoring interpolationVector since it's not implemented yet,
        * also ignoring the renderEntityName boolean as it doesn't have a name
        * */

        //draw upper triangle
        paint.setColor(currentUpperTriColor);
        canvas.drawPath(getPathWithPoints(offsetVertices(upperTriangleVertices(), renderOffset)), paint);

        //draw lower triangle
        paint.setColor(currentLowerTriColor);
        canvas.drawPath(getPathWithPoints(offsetVertices(lowerTriangleVertices(), renderOffset)), paint);
    }

    @Override
    public void setColor(int color) {
        this.currentLowerTriColor = color;
    }

    public void setUpperTriColor(int color){
        this.currentUpperTriColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        this.currentLowerTriColor = defaultLowerTriColor;
    }

    public void revertUppertriToDefaultColor(){
        this.currentUpperTriColor = defaultUpperTriColor;
    }

    private Point[] upperTriangleVertices(){
        return new Point[]{
                vertexVectors[0].getHead(),
                vertexVectors[1].getHead(),
                vertexVectors[3].getHead()
        };
    }

    private Point[] lowerTriangleVertices(){
        return new Point[]{
                vertexVectors[1].getHead(),
                vertexVectors[2].getHead(),
                vertexVectors[3].getHead()
        };
    }
}

/*
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
*/