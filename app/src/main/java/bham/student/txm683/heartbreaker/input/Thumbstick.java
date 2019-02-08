package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Thumbstick implements InputUIElement{

    private final String TAG = "hb:: Thumbstick";

    private Point neutralPosition;
    private float neutralCircleRadius;
    private Paint neutralPaint;

    private float activeCircleRadius;
    private Paint activePaint;

    private Vector inputVector;

    private float maxRadius;
    private float maxInputLengthRequirement;
    private Paint maxPaint;

    private boolean receivingInput;

    private int pointerID;

    public Thumbstick(Point neutralPosition, float neutralCircleRadius, float maxRadius){
        this.neutralPosition = neutralPosition;

        this.inputVector = new Vector(neutralPosition, neutralPosition);

        this.receivingInput = false;

        this.neutralCircleRadius = neutralCircleRadius;
        this.activeCircleRadius = neutralCircleRadius*1.5f;

        this.activePaint = new Paint();
        this.activePaint.setColor(Color.GRAY);
        this.activePaint.setStrokeWidth(10);

        this.neutralPaint = new Paint();
        this.neutralPaint.setColor(Color.DKGRAY);
        this.neutralPaint.setStrokeWidth(10);

        this.maxRadius = maxRadius;
        this.maxPaint = new Paint();
        this.maxPaint.setColor(Color.LTGRAY);
        this.maxPaint.setStrokeWidth(10);

        this.maxInputLengthRequirement = maxRadius * 0.7f;

        this.pointerID = MotionEvent.INVALID_POINTER_ID;
    }

    @Override
    public void cancel() {
        this.pointerID = MotionEvent.INVALID_POINTER_ID;
        returnToNeutral();
    }

    @Override
    public void setPointerID(int id) {
        if (id >= 0)
            this.pointerID = id;
    }

    @Override
    public boolean hasID(int id) {
        return pointerID == id;
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(neutralPosition.getX(), neutralPosition.getY(), maxRadius, maxPaint);
        canvas.drawCircle(neutralPosition.getX(), neutralPosition.getY(), neutralCircleRadius, neutralPaint);

        canvas.drawCircle(inputVector.getHead().getX(), inputVector.getHead().getY(), activeCircleRadius, activePaint);
    }

    private void returnToNeutral(){
        this.inputVector = new Vector(neutralPosition, neutralPosition);

        receivingInput = false;
    }

    public Vector getMovementVector(){
        if (inputVector.getTail().equals(inputVector.getHead())){
            return Vector.ZERO_VECTOR;
        } else {
            if (inputVector.getLength() > maxInputLengthRequirement){
                return inputVector.getUnitVector();
            }
            return inputVector.sMult(1/maxRadius);
        }
    }

    /**
     * Sets the active position for the thumbstick. If the new position exceeds the max radius,
     * it will be set to an equivalent direction but with magnitude of maxradius
     * @param newActivePosition the position to set active position as
     */
    void setActivePosition(Point newActivePosition) {

        if (receivingInput){
            Vector newInputVector = new Vector(neutralPosition, newActivePosition);

            float proportionOfLengths = newInputVector.getLength() / maxRadius;

            if (Float.compare(proportionOfLengths, 1f) > 0){
                //shrink input vector to length of max radius if too long
                newInputVector = newInputVector.sMult(1f / proportionOfLengths);
            }
            this.inputVector = newInputVector;
        }
    }

    public boolean containsPoint(Point touchEventPosition){
        if (new Vector(neutralPosition, touchEventPosition).getLength() <= maxRadius && pointerID < 0){
            receivingInput = true;
            return true;
        }
        return false;
    }

    @Override
    public int getID() {
        return pointerID;
    }
}