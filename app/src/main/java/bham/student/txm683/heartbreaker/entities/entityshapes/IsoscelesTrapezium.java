package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class IsoscelesTrapezium extends Polygon implements Renderable {
    private float primaryAngle;

    private int currentColor;
    private int defaultColor;
    private Paint paint;

    public IsoscelesTrapezium(Vector[] vertexVectors, int color){
        super(vertexVectors);

        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);

        this.currentColor = color;
        this.defaultColor = color;

        this.paint = new Paint();
    }

    @Override
    void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle), (float) Math.sin(primaryAngle)).getUnitVector();
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        this.paint.setColor(currentColor);

        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public void setColor(int color) {
        this.currentColor = color;
    }

    @Override
    public void revertToDefaultColor() {
        this.currentColor = defaultColor;
    }
}

/*private float bottomWidth;
    private float topWidth;
    private float primaryAngle;

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
        this.collisionOutline = new CollisionOutline(vertexVectors);

        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    public IsoscelesTrapezium(String stateString) throws JSONException {
        super(stateString, ShapeIdentifier.ISO_TRAPEZIUM);
        JSONObject jsonObject = new JSONObject(stateString);
        this.bottomWidth = (float) jsonObject.getDouble("bottomwidth");
        this.topWidth = (float) jsonObject.getDouble("topwidth");

        this.primaryAngle = (float) jsonObject.getDouble("anglebetweenprimaryandup");
    }

    public void contractHeight(float changeInHeightRatio) {
        //make sure proportion is positive

        changeInHeightRatio = Math.abs(changeInHeightRatio);

        Point oldBottomLeftVertex = vertexVectors[3].getHead();

        setHeight(contractionHeight * changeInHeightRatio);

        //moves the shape back to the same base position as before height changed
        translateShape(new Vector(vertexVectors[3].getHead(), oldBottomLeftVertex));
    }

    public void contractWidth(float changeInWidthRatio){
        changeInWidthRatio = Math.abs(changeInWidthRatio);

        setWidth(contractionWidth * changeInWidthRatio);
    }

    @Override
    public void returnToNormal() {
        Log.d(TAG, "Returning to normal");
        Point oldBottomLeftVertex = vertexVectors[3].getHead();

        setHeight(height);

        translateShape(new Vector(vertexVectors[3].getHead(), oldBottomLeftVertex));

        setWidth(width);

        resetToDefaultColor();
    }

    @Override
    public void setWidth(float newWidth) {
        if (Math.abs(newWidth - contractionWidth) < 0.0001)
            return;

        float changeInWidth = newWidth - contractionWidth;

        Polygon.rectOrTrapeziumChangeWidth(changeInWidth, forwardUnitVector, vertexVectors);

        this.contractionWidth = newWidth;
        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    @Override
    public void setHeight(float newHeight) {
        if (Math.abs(newHeight - contractionHeight) < 0.0001)
            return;

        float changeInHeight = newHeight - contractionHeight;

        //same formula as a rectangle for changing height
        Rectangle.rectOrTrapeziumChangeHeight(changeInHeight, forwardUnitVector, vertexVectors);
        this.contractionHeight = newHeight;
        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle), (float) Math.sin(primaryAngle)).getUnitVector();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = getJSONObject();
        jsonObject.put("topwidth", topWidth);
        jsonObject.put("bottomwidth", bottomWidth);
        jsonObject.put("anglebetweenprimaryandup", primaryAngle);
        return jsonObject;
    }*/
