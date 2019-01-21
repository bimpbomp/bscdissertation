package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private int inputPointer;

    public Thumbstick(Point neutralPosition, float neutralCircleRadius, float maxRadius){
        this.neutralPosition = neutralPosition;

        this.inputVector = new Vector(neutralPosition, neutralPosition);

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

        this.maxInputLengthRequirement = maxRadius * 0.9f;
    }

    @Override
    public void setPointerID(int id) {
        this.inputPointer = id;
    }

    @Override
    public boolean hasID(int id) {
        return inputPointer == id;
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(neutralPosition.getX(), neutralPosition.getY(), maxRadius, maxPaint);
        canvas.drawCircle(neutralPosition.getX(), neutralPosition.getY(), neutralCircleRadius, neutralPaint);

        canvas.drawCircle(inputVector.getHead().getX(), inputVector.getHead().getY(), activeCircleRadius, activePaint);
    }

    public void returnToNeutral(){
        this.inputVector = new Vector(neutralPosition, neutralPosition);

        receivingInput = false;
    }

    public Vector getMovementVector(){
        if (inputVector.getTail().equals(inputVector.getHead())){
            return new Vector();
        } else {
            return inputVector.sMult(1/maxInputLengthRequirement).translate(neutralPosition.smult(-1f));
        }
    }

    public void setActivePosition(Point newActivePosition) {

        if (receivingInput){
            Vector newInputVector = new Vector(neutralPosition, newActivePosition);

            float proportionOfLengths = newInputVector.getLength() / maxRadius;

            if (Float.compare(proportionOfLengths, 1f) > 0){
                newInputVector = newInputVector.sMult(1f / proportionOfLengths);
            }
            this.inputVector = newInputVector;
        }
    }

    public boolean containsPoint(Point touchEventPosition){
        if (new Vector(neutralPosition, touchEventPosition).getLength() <= maxRadius){
            receivingInput = true;
            return true;
        }
        return false;
    }

    public Point getNeutralPosition() {
        return neutralPosition;
    }

    public void setNeutralPosition(Point neutralPosition) {
        this.neutralPosition = neutralPosition;
    }

    public float getNeutralCircleRadius() {
        return neutralCircleRadius;
    }

    public void setNeutralCircleRadius(float neutralCircleRadius) {
        this.neutralCircleRadius = neutralCircleRadius;
    }

    public float getActiveCircleRadius() {
        return activeCircleRadius;
    }

    public void setActiveCircleRadius(float activeCircleRadius) {
        this.activeCircleRadius = activeCircleRadius;
    }

    public Point getActivePosition() {
        return this.inputVector.getHead();
    }
}