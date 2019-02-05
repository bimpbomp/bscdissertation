package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.utils.Point;

public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Thumbstick thumbstick;
    private Button pauseButton;

    private Button meleeButton;
    private Button rangedButton;

    private Button[] debugButtons;

    private LevelState levelState;

    public InputManager(LevelState levelState){
        this.levelState = levelState;
    }

    public boolean onTouchEvent(MotionEvent event){
        boolean eventHandled;

        int eventIndex = event.getActionIndex();
        int eventID = event.getPointerId(eventIndex);

        Point coordinatesPressed = new Point(event.getX(eventIndex), event.getY(eventIndex));

        if (!levelState.isPaused()){
            eventHandled = handleWhileResumed(event, eventID, eventIndex, coordinatesPressed);
        } else {
            eventHandled = handleWhilePaused(event, eventID, eventIndex, coordinatesPressed);
        }


        return eventHandled;
    }

    private boolean handleWhilePaused(MotionEvent event, int eventID, int eventIndex, Point coordinatesPressed){

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:

                if (pauseButton.containsPoint(coordinatesPressed)){
                    pauseButton.setPointerID(eventID);
                } else {
                    checkDebugButtonsOnDown(eventID, coordinatesPressed);
                }
                break;

            case MotionEvent.ACTION_UP:
                handleUpPaused(eventID);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (pauseButton.containsPoint(coordinatesPressed))
                    pauseButton.setPointerID(eventID);
                else
                    checkDebugButtonsOnDown(eventID, coordinatesPressed);

                break;

            case MotionEvent.ACTION_POINTER_UP:
                handleUpPaused(eventID);
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handleWhileResumed(MotionEvent event, int eventID, int eventIndex, Point coordinatesPressed){

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:

                handleDownResumed(eventID, coordinatesPressed);
                break;

            case MotionEvent.ACTION_UP:
                handleUpResumed(eventID);
                break;

            case MotionEvent.ACTION_MOVE:

                int pointerCount = event.getPointerCount();
                for(int i = 0; i < pointerCount; ++i) {
                    eventIndex = i;
                    eventID = event.getPointerId(eventIndex);

                    if (thumbstick.hasID(eventID))
                        thumbstick.setActivePosition(coordinatesPressed);
                    /*else if (meleeButton.hasID(eventID))
                        levelState.getPlayer().chargeMelee();*/
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                handleDownResumed(eventID, coordinatesPressed);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                handleUpResumed(eventID);
                break;
            default:
                return false;
        }
        return true;
    }

    private void checkDebugButtonsOnDown(int eventID, Point coordinatesPressed){
        for (Button button : debugButtons){
            if (button.containsPoint(coordinatesPressed))
                button.setPointerID(eventID);
        }
    }

    private void handleDownResumed(int eventID, Point coordinatesPressed){
        if (thumbstick.containsPoint(coordinatesPressed)){
            thumbstick.setActivePosition(coordinatesPressed);
            thumbstick.setPointerID(eventID);

        } else if (meleeButton.containsPoint(coordinatesPressed)){
            meleeButton.setPointerID(eventID);
        } else if (pauseButton.containsPoint(coordinatesPressed)){
            pauseButton.setPointerID(eventID);
        } else if (rangedButton.containsPoint(coordinatesPressed)){
            rangedButton.setPointerID(eventID);
        }
    }

    private void handleUpPaused(int eventID){

        if (pauseButton.hasID(eventID)) {
            //if game is to be resumed, cancel any buttons currently pressed
            pauseButton.onClick();

            for (Button button : debugButtons){
                if (!button.hasID(MotionEvent.INVALID_POINTER_ID)){
                    button.cancel();
                }
            }
        } else {
            for (Button button : debugButtons) {
                if (button.hasID(eventID))
                    button.onClick();
            }
        }
    }

    private void handleUpResumed(int eventID){

        if (pauseButton.hasID(eventID)) {
            //cancel any outstanding pressed buttons
            meleeButton.cancel();
            thumbstick.cancel();

            pauseButton.onClick();

        } else if (thumbstick.hasID(eventID)){
            thumbstick.cancel();
        } else if (meleeButton.hasID(eventID)){
            Log.d(TAG, "up");
            //levelState.getPlayer().meleeAttack();
            meleeButton.cancel();
        } else if (rangedButton.hasID(eventID)){
            //levelState.getPlayer(). ;
            rangedButton.cancel();
        }

    }

    public void draw(Canvas canvas, Paint textPaint){
        if (!levelState.isPaused()) {
            thumbstick.draw(canvas);

            rangedButton.draw(canvas, textPaint);
            meleeButton.draw(canvas, textPaint);
        } else {
            if (debugButtons != null) {
                for (Button button : debugButtons) {
                    button.draw(canvas, textPaint);
                }
            }
        }

        pauseButton.draw(canvas, textPaint);
    }

    public Thumbstick getThumbstick() {
        return thumbstick;
    }

    public void setThumbstick(Thumbstick thumbstick) {
        this.thumbstick = thumbstick;
    }

    public Button getPauseButton() {
        return pauseButton;
    }

    public void setPauseButton(Button pauseButton) {
        this.pauseButton = pauseButton;
    }

    public void setMeleeButton(Button meleeButton) {
        this.meleeButton = meleeButton;
    }

    public void setDebugButtons(Button[] buttons){
        this.debugButtons = buttons;
    }

    public void setRangedButton(Button rangedButton){this.rangedButton = rangedButton;}
}