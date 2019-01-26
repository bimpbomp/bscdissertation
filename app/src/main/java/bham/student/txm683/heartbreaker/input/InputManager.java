package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.utils.Point;

public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Thumbstick thumbstick;
    private Button pauseButton;

    private Button[] debugButtons;

    private LevelState levelState;

    public InputManager(LevelState levelState){
        this.levelState = levelState;
    }

    public boolean onTouchEvent(MotionEvent event){
        boolean eventHandled = true;

        int eventIndex = event.getActionIndex();
        int eventID = event.getPointerId(eventIndex);

        Point coordinatesPressed = new Point(event.getX(eventIndex), event.getY(eventIndex));

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:

                if (thumbstick.containsPoint(coordinatesPressed)){
                    thumbstick.setActivePosition(coordinatesPressed);
                    thumbstick.setPointerID(eventID);

                } else if (pauseButton.containsPoint(coordinatesPressed)){
                    pauseButton.setPointerID(eventID);
                } else {
                    checkDebugButtonsOnDown(eventID, coordinatesPressed);
                }
                break;

            case MotionEvent.ACTION_UP:
                handleUp(eventID);
                break;

            case MotionEvent.ACTION_MOVE:
                if (thumbstick.hasID(eventID))
                    thumbstick.setActivePosition(coordinatesPressed);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (pauseButton.containsPoint(coordinatesPressed))
                    pauseButton.setPointerID(eventID);
                else
                    checkDebugButtonsOnDown(eventID, coordinatesPressed);

                break;

            case MotionEvent.ACTION_POINTER_UP:
                handleUp(eventID);
                break;
            default:
                eventHandled = false;
                break;
        }

        return eventHandled;
    }

    private void checkDebugButtonsOnDown(int eventID, Point coordinatesPressed){
        for (Button button : debugButtons){
            if (button.containsPoint(coordinatesPressed))
                button.setPointerID(eventID);
        }
    }

    private void handleUp(int eventID){
        if (thumbstick.hasID(eventID)){
            thumbstick.deactivate();
        } else if (pauseButton.hasID(eventID)) {
            pauseButton.onClick();
        } else {

            for (Button button : debugButtons) {
                if (button.hasID(eventID))
                    button.onClick();
            }
        }
    }

    public void draw(Canvas canvas, Paint textPaint){
        if (!levelState.isPaused()) {
            thumbstick.draw(canvas);
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

    public void setDebugButtons(Button[] buttons){
        this.debugButtons = buttons;
    }
}