package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.utils.Point;

public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Thumbstick thumbstick;
    private Button pauseButton;

    private LevelState levelState;

    public InputManager(LevelState levelState){
        this.levelState = levelState;
    }

    public boolean onTouchEvent(MotionEvent event){
        boolean eventHandled = true;

        int eventID = event.getPointerId(event.getActionIndex());

        Point coordinatesPressed = new Point(event.getX(), event.getY());

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN " + "index: " + event.getActionIndex() + ", id: " + event.getPointerId(event.getActionIndex()));

                if (thumbstick.containsPoint(coordinatesPressed)){
                    thumbstick.setActivePosition(coordinatesPressed);
                    thumbstick.setPointerID(eventID);
                } else if (pauseButton.containsPoint(coordinatesPressed)){
                    pauseButton.setPointerID(eventID);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP " + "index: " + event.getActionIndex() + ", id: " + event.getPointerId(event.getActionIndex()));
                if (thumbstick.hasID(eventID)) {
                    thumbstick.returnToNeutral();
                    thumbstick.setPointerID(MotionEvent.INVALID_POINTER_ID);

                } else if (pauseButton.hasID(eventID)){
                    levelState.setPaused(!levelState.isPaused());
                    pauseButton.setPointerID(MotionEvent.INVALID_POINTER_ID);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE " + "index: " + event.getActionIndex() + ", id: " + event.getPointerId(event.getActionIndex()));
                if (thumbstick.hasID(eventID))
                    thumbstick.setActivePosition(coordinatesPressed);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "ACTION_POINTER_DOWN " + "index: " + event.getActionIndex() + ", id: " + event.getPointerId(event.getActionIndex()));
                if (pauseButton.containsPoint(coordinatesPressed)){
                    pauseButton.setPointerID(eventID);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "ACTION_POINTER_UP " + "index: " + event.getActionIndex() + ", id: " + event.getPointerId(event.getActionIndex()));
                if (thumbstick.hasID(eventID)){
                    thumbstick.returnToNeutral();
                    thumbstick.setPointerID(MotionEvent.INVALID_POINTER_ID);
                } else if (pauseButton.hasID(eventID)) {
                    pauseButton.setPointerID(MotionEvent.INVALID_POINTER_ID);
                    levelState.setPaused(!levelState.isPaused());
                }
                break;
            default:
                eventHandled = false;
                break;
        }

        return eventHandled;
    }

    private void handleDown(MotionEvent event){

    }

    public void draw(Canvas canvas){
        if (!levelState.isPaused()) {
            thumbstick.draw(canvas);
        }

        pauseButton.draw(canvas);
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
}