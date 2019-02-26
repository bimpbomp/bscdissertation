package bham.student.txm683.heartbreaker.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.utils.Point;


//TODO issue with input when thumbstick has pointer and first finger was activating other button
public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Thumbstick thumbstick;

    private Thumbstick rotationThumbstick;

    private Button pauseButton;

    private Button secondaryWeaponButton;
    private Button primaryWeaponButton;

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

                    coordinatesPressed = new Point(event.getX(eventIndex), event.getY(eventIndex));

                    Log.d("hb::INPUT", "index: " + eventIndex + ", id: " + eventID);

                    Log.d("hb::INPUTIDS", "thumb: " + thumbstick.getID() + ", rot: " + rotationThumbstick.getID());
                    if (thumbstick.hasID(eventID))
                        thumbstick.setActivePosition(coordinatesPressed);
                    /*else if (primaryWeaponButton.hasID(eventID))
                        levelState.addBullet(levelState.getPlayer().shoot());*/
                    else if (secondaryWeaponButton.hasID(eventID))
                        levelState.addBullet(levelState.getPlayer().shootSecondary());
                    else if (rotationThumbstick.hasID(eventID)){
                        rotationThumbstick.setActivePosition(coordinatesPressed);
                    }
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
        Log.d("hb::INPUTDOWN", "down: " + eventID);
        if (thumbstick.getID() < 0 && thumbstick.containsPoint(coordinatesPressed)){
            thumbstick.setActivePosition(coordinatesPressed);
            thumbstick.setPointerID(eventID);

        } else if (secondaryWeaponButton.getID() < 0 && secondaryWeaponButton.containsPoint(coordinatesPressed)){
            levelState.addBullet(levelState.getPlayer().shootSecondary());
            secondaryWeaponButton.setPointerID(eventID);
        } else if (pauseButton.getID() < 0 && pauseButton.containsPoint(coordinatesPressed)){
            pauseButton.setPointerID(eventID);/*
        } else if (primaryWeaponButton.getID() < 0 && primaryWeaponButton.containsPoint(coordinatesPressed)){
            levelState.addBullet(levelState.getPlayer().shoot());
            primaryWeaponButton.setPointerID(eventID);*/
        } else if (rotationThumbstick.getID() < 0 && rotationThumbstick.containsPoint(coordinatesPressed)){
            rotationThumbstick.setActivePosition(coordinatesPressed);
            rotationThumbstick.setPointerID(eventID);
            levelState.addBullet(levelState.getPlayer().shoot());
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
        Log.d("hb::INPUTUP", "up: " + eventID);

        if (pauseButton.hasID(eventID)) {
            //cancel any outstanding pressed buttons
            secondaryWeaponButton.cancel();
            thumbstick.cancel();

            pauseButton.onClick();

        } else if (thumbstick.hasID(eventID)){
            thumbstick.cancel();
        } else if (secondaryWeaponButton.hasID(eventID)){
            //levelState.getPlayer().meleeAttack();
            secondaryWeaponButton.cancel();
        }/* else if (primaryWeaponButton.hasID(eventID)){
            primaryWeaponButton.cancel();
        }*/
        else if (rotationThumbstick.hasID(eventID)){
            rotationThumbstick.cancel();
        }

    }

    public void draw(Canvas canvas, Paint textPaint){
        if (!levelState.isPaused()) {
            thumbstick.draw(canvas);

            /*primaryWeaponButton.setLabel("∞");
            primaryWeaponButton.draw(canvas, textPaint);*/

            secondaryWeaponButton.setLabel(levelState.getPlayer().getSecondaryAmmo()+"");
            secondaryWeaponButton.draw(canvas, textPaint);

            rotationThumbstick.draw(canvas);
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

    public Thumbstick getRotationThumbstick() {
        return rotationThumbstick;
    }

    public void setRotationThumbstick(Thumbstick rotationThumbstick) {
        this.rotationThumbstick = rotationThumbstick;
    }

    public Button getPauseButton() {
        return pauseButton;
    }

    public void setPauseButton(Button pauseButton) {
        this.pauseButton = pauseButton;
    }

    public void setSecondaryWeaponButton(Button secondaryWeaponButton) {
        this.secondaryWeaponButton = secondaryWeaponButton;
    }

    public void setDebugButtons(Button[] buttons){
        this.debugButtons = buttons;
    }

    public void setPrimaryWeaponButton(Button primaryWeaponButton){this.primaryWeaponButton = primaryWeaponButton;}
}