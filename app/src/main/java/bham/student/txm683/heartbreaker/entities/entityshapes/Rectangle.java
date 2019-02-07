package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Rectangle extends Polygon implements Renderable {
    private float primaryAngle;

    private Paint paint;
    private int defaultColor;
    private int currentColor;

    public Rectangle(Vector[] vertexVectors, int colorValue){
        super(vertexVectors, ShapeIdentifier.RECTANGLE);

        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);

        this.paint = new Paint();

        this.defaultColor = colorValue;
        this.currentColor = colorValue;
    }

    public Rectangle(Point center, float width, float height, int colorValue){
        this(new Vector[]{new Vector(center, center.add(new Point(-width/2f, -height/2f))),
                new Vector(center, center.add(new Point(width/2f, -height/2f))),
                new Vector(center, center.add(new Point(width/2f, height/2f))),
                new Vector(center, center.add(new Point(-width/2f, height/2f)))}, colorValue);
    }

    @Override
    void setForwardUnitVector() {
        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle),
                (float) Math.sin(primaryAngle));
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector, boolean renderEntityName) {
        /*
         * Ignoring interpolationVector since it's not implemented yet,
         * also ignoring the renderEntityName boolean as it doesn't have a name
         * */

        paint.setColor(currentColor);
        canvas.drawPath(getPathWithPoints(getVertices(renderOffset)), paint);
    }

    @Override
    public void revertToDefaultColor() {
        this.currentColor = defaultColor;
    }

    @Override
    public void setColor(int color) {
        this.currentColor = color;
    }
}

/*private float primaryAngle;

    public Rectangle(Point geometricCenter, float width, float height, int colorValue){
        super(geometricCenter, width, height, colorValue, ShapeIdentifier.RECTANGLE);

        this.vertexVectors = new Vector[4];

        float halfWidth = width/2f;
        float halfHeight = height/2f;

        vertexVectors[0] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY() - halfHeight));
        vertexVectors[1] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY() - halfHeight));
        vertexVectors[2] = new Vector(geometricCenter, new Point(geometricCenter.getX()+halfWidth, geometricCenter.getY() + halfHeight));
        vertexVectors[3] = new Vector(geometricCenter, new Point(geometricCenter.getX()-halfWidth, geometricCenter.getY() + halfHeight));

        this.forwardUnitVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY()-1));

        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
        this.collisionOutline = new CollisionOutline(vertexVectors);
    }

    public Rectangle(Point center, Point topLeft, Point bottomRight, int colorValue){
        super(center, bottomRight.getX()-topLeft.getX(), bottomRight.getY()-topLeft.getY(),
                colorValue, ShapeIdentifier.RECTANGLE);

        this.vertexVectors = new Vector[4];

        vertexVectors[0] = new Vector(geometricCenter, topLeft);
        vertexVectors[1] = new Vector(geometricCenter, new Point(bottomRight.getX(), topLeft.getY()));
        vertexVectors[2] = new Vector(geometricCenter, bottomRight);
        vertexVectors[3] = new Vector(geometricCenter, new Point(topLeft.getX(), bottomRight.getY()));

        this.forwardUnitVector = new Vector(geometricCenter, new Point(geometricCenter.getX(), geometricCenter.getY()-1));

        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
        this.collisionOutline = new CollisionOutline(vertexVectors);
    }

    public Rectangle(String stateString) throws JSONException {
        super(stateString, ShapeIdentifier.RECTANGLE);

        JSONObject jsonObject = new JSONObject(stateString);

        primaryAngle = Float.parseFloat(jsonObject.getString("anglebetweenupandprimaryvectors"));
    }

    @Override
    public void setForwardUnitVector() {

        this.forwardUnitVector = vertexVectors[0].rotate((float) Math.cos(primaryAngle), (float) Math.sin(primaryAngle));
    }

    //explanation in notebook [2]
    @Override
    public void setHeight(float newHeight) {

        float changeInHeight = newHeight - height;
        this.height = newHeight;

        rectOrTrapeziumChangeHeight(changeInHeight, forwardUnitVector, vertexVectors);
        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);
    }

    //explanation in notebook [2]
    @Override
    public void setWidth(float newWidth) {
        float changeInWidth = newWidth - width;

        Polygon.rectOrTrapeziumChangeWidth(changeInWidth, forwardUnitVector, vertexVectors);

        this.primaryAngle = calculateAngleBetweenVectors(vertexVectors[0], this.forwardUnitVector);

        this.width = newWidth;
    }

    @Override
    public Point[] getCollisionVertices() {
        return this.getVertices();
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = this.getJSONObject();
        jsonObject.put("anglebetweenupandprimaryvectors", primaryAngle);

        return jsonObject;
    }*/
