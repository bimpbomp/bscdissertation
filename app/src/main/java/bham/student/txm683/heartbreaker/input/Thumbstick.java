package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Thumbstick {

    private final String TAG = "hb:: Thumbstick";

    private Vector neutralPosition;
    private float neutralCircleRadius;
    private Paint neutralPaint;

    private Vector activePosition;
    private float activeCircleRadius;
    private Paint activePaint;

    private float maxRadius;
    private Paint maxPaint;

    private boolean receivingInput;

    public Thumbstick(Vector neutralPosition, float neutralCircleRadius, float maxRadius){
        this.neutralPosition = neutralPosition;
        activePosition = neutralPosition;

        this.receivingInput = false;

        this.neutralCircleRadius = neutralCircleRadius;
        this.activeCircleRadius = neutralCircleRadius*1.5f;

        this.activePaint = new Paint();
        this.activePaint.setColor(Color.BLUE);
        this.activePaint.setStrokeWidth(10);

        this.neutralPaint = new Paint(Color.BLACK);
        this.neutralPaint.setStrokeWidth(10);

        this.maxRadius = maxRadius;
        this.maxPaint = new Paint();
        this.maxPaint.setColor(Color.RED);
        this.maxPaint.setStrokeWidth(10);
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(neutralPosition.getX(), neutralPosition.getY(), maxRadius, maxPaint);
        canvas.drawCircle(neutralPosition.getX(), neutralPosition.getY(), neutralCircleRadius, neutralPaint);
        canvas.drawCircle(activePosition.getX(), activePosition.getY(), activeCircleRadius, activePaint);
    }

    public void returnToNeutral(){
        this.activePosition = neutralPosition;
        receivingInput = false;
    }

    public Vector getMovementVector(){
        if (activePosition.equals(neutralPosition)){
            return new Vector();
        } else {
            return neutralPosition.directionTo(activePosition);
        }
    }

    public Vector getNeutralPosition() {
        return neutralPosition;
    }

    public void setNeutralPosition(Vector neutralPosition) {
        this.neutralPosition = neutralPosition;
    }

    public Vector getActivePosition() {
        return activePosition;
    }

    public void setActivePosition(Vector newActivePosition) {

        if (receivingInput) {
            Vector neutralToActiveVector = neutralPosition.directionTo(newActivePosition);
            float movementVectorLength = neutralToActiveVector.getLength();

            if (movementVectorLength > maxRadius) {
                this.activePosition = neutralToActiveVector.getUnitVector().sMult(maxRadius).vAdd(neutralPosition);
                //Log.d(TAG, "mV: " + newActivePosition.toString() + ", |mVector|: " + movementVectorLength + ", activePosition: " + this.activePosition.toString() + "|aP|: " + this.activePosition.getLength());
            } else
                this.activePosition = newActivePosition;
        }
    }

    public boolean isInRadius(Vector touchEventPosition){
        if (neutralPosition.directionTo(touchEventPosition).getLength() <= maxRadius){
            receivingInput = true;
            return true;
        }
        return false;
    }
}
