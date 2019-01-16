package bham.student.txm683.heartbreaker.input;

import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.utils.Point;

public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Thumbstick thumbstick;

    public InputManager(Thumbstick thumbstick){
        this.thumbstick = thumbstick;
    }

    public boolean onTouchEvent(MotionEvent event){
        boolean eventHandled = true;

        Point coordinatesPressed = new Point(event.getX(), event.getY());

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //Log.d(TAG, "ACTION_DOWN: " + coordinatesPressed.toString());
                if (thumbstick.isInRadius(coordinatesPressed)){
                    thumbstick.setActivePosition(coordinatesPressed);
                }
                break;
            case MotionEvent.ACTION_UP:
                //Log.d(TAG, "ACTION_UP: " + coordinatesPressed.toString());
                thumbstick.returnToNeutral();
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d(TAG, "ACTION_MOVE: " + coordinatesPressed.toString());
                thumbstick.setActivePosition(coordinatesPressed);
                break;
            default:
                eventHandled = false;
                break;
        }

        return eventHandled;
    }

    public Thumbstick getThumbstick(){
        return this.thumbstick;
    }
}
