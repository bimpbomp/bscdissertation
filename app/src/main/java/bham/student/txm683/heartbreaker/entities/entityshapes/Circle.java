package bham.student.txm683.heartbreaker.entities.entityshapes;


import android.graphics.Canvas;
import bham.student.txm683.heartbreaker.physics.CollisionOutline;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

public class Circle extends EntityShape {

    private float radius;
    private CollisionOutline collisionOutline;

    public Circle(Point center, float radius, int colorValue){
        super(center, colorValue, ShapeIdentifier.CIRCLE);
        this.radius = radius;

        setForwardUnitVector();
    }

    public Circle(String stateString) throws JSONException{
        super(stateString, ShapeIdentifier.CIRCLE);

        JSONObject jsonObject = new JSONObject(stateString);

        radius = Float.parseFloat(jsonObject.getString("radius"));

    }

    public float getRadius(){
        return radius;
    }

    @Override
    public void setHeight(float newHeight) {
        this.radius = newHeight/2f;
    }

    @Override
    public float getHeight() {
        return this.radius*2;
    }

    @Override
    public void setWidth(float newWidth) {
        this.radius = newWidth/2f;
    }

    @Override
    public float getWidth() {
        return this.radius*2f;
    }

    @Override
    public void setForwardUnitVector() {
        this.forwardUnitVector = new Vector(-1, 0);
    }

    /**
     * Returns a bounding box covering the circle for Spatial partitioning
     * @return Bounding rectangle vertices
     */
    @Override
    public Point[] getCollisionVertices() {
        return new Point[]{
                new Point(geometricCenter.getX()-radius, geometricCenter.getY()-radius),
                new Point(geometricCenter.getX()+radius, geometricCenter.getY()-radius),
                new Point(geometricCenter.getX()+radius, geometricCenter.getY()+radius),
                new Point(geometricCenter.getX()-radius, geometricCenter.getY()+radius)};
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, Vector interpolationVector) {
        Point offsettedCenter = geometricCenter.add(renderOffset);
        canvas.drawCircle(offsettedCenter.getX(), offsettedCenter.getY(), radius, paint);
    }

    @Override
    public void translateShape(Vector translationVector) {
        //Log.d(TAG, "translationVector: " + translationVector.relativeToString() + ", center: " + geometricCenter.toString());
        geometricCenter = geometricCenter.add(translationVector.getRelativeToTailPoint());
    }

    @Override
    public void rotateShape(Vector rotationVector) {
        float angle = calculateAngleBetweenVectors(forwardUnitVector, rotationVector);
        forwardUnitVector = forwardUnitVector.rotate((float) Math.cos(angle), (float) Math.sin(angle));
    }

    @Override
    public JSONObject getStateObject() throws JSONException {
        JSONObject jsonObject = super.getJSONObject();

        jsonObject.put("radius", radius);

        return jsonObject;
    }
}
