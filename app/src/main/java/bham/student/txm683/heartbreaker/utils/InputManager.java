package bham.student.txm683.heartbreaker.utils;

import android.util.Log;
import android.view.MotionEvent;

public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Vector lastPress;

    public InputManager(){
        lastPress = null;
    }

    public synchronized void press(Vector newPress){
        this.lastPress = newPress;
    }

    public synchronized Vector getLastPress(){
        return this.lastPress;
    }

    public boolean onTouchEvent(MotionEvent event){
        boolean eventHandled = true;

        Vector coordinatesPressed = new Vector(event.getX(), event.getY());

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN: " + coordinatesPressed.toString());
                press(coordinatesPressed);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP: " + coordinatesPressed.toString());
                press(coordinatesPressed);
                break;
            case MotionEvent.ACTION_MOVE:
                Vector lastPress = getLastPress();
                if (lastPress != null){
                    if (!lastPress.equals(coordinatesPressed)){
                        Log.d(TAG, "ACTION_MOVE: " + coordinatesPressed.toString());
                        press(coordinatesPressed);
                    }
                }
                break;
            default:
                eventHandled = false;
                break;
        }

        return eventHandled;
    }
}
