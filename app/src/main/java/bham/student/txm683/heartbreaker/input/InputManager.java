package bham.student.txm683.heartbreaker.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.rendering.popups.PopUpElement;
import bham.student.txm683.heartbreaker.rendering.popups.Popup;
import bham.student.txm683.heartbreaker.rendering.popups.TextBox;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

public class InputManager {
    private static final String TAG = "hb::InputManager";

    private Thumbstick thumbstick;

    private Thumbstick rotationThumbstick;

    private Button pauseButton;

    private Button returnToMenuButton;

    private Button secondaryWeaponButton;
    private Button primaryWeaponButton;

    private Button[] debugButtons;

    private LevelState levelState;

    private Context context;

    private Popup pauseScreen;

    private Popup activePopup;

    public InputManager(LevelState levelState, Context context){
        this.levelState = levelState;
        this.context = context;

        Tile screenDims = levelState.getScreenDimensions();
        Point screenCenter = new Point(screenDims).sMult(0.5f);

        PopUpElement[] elements = new PopUpElement[]{
                new TextBox("Game is Paused", screenCenter, 0, 0, Color.RED, 20, 60),
                new RectButton("Resume", screenCenter, 300, 100, Color.GRAY, 40, () -> {levelState.setPaused(false); activePopup.hide(); activePopup = null;}),
                new RectButton("Restart", screenCenter, 300, 100, Color.GRAY, 60, () -> {}),
                new RectButton("Return To Menu", screenCenter, 300, 100, Color.GRAY, 80, () -> returnToMenuButton.onClick())
        };

        this.pauseScreen = new Popup(screenCenter, screenDims.getX()/2, (int)(screenDims.getY()/1.5f), true, elements);
    }

    public void setActivePopup(Popup popup){
        this.activePopup = popup;
    }

    public boolean onTouchEvent(MotionEvent event){

        boolean eventHandled;

        int eventIndex = event.getActionIndex();
        int eventID = event.getPointerId(eventIndex);

        Point coordinatesPressed = new Point(event.getX(eventIndex), event.getY(eventIndex));

        if (activePopup == null){
            eventHandled = handleWhileResumed(event, eventID, eventIndex, coordinatesPressed);
        } else {
            eventHandled = handlePopupInput(event, eventID, coordinatesPressed);
        }

        /*if (!levelState.isPaused()){
            eventHandled = handleWhileResumed(event, eventID, eventIndex, coordinatesPressed);
        } else {
            //eventHandled = handleWhilePaused(event, eventID, eventIndex, coordinatesPressed);
            eventHandled = handlePopupInput(event, eventID, coordinatesPressed);
        }*/

        return eventHandled;
    }

    private boolean handlePopupInput(MotionEvent event, int eventID, Point coordinatesPressed){
        for (Button element : activePopup.getInputElements()){

            if (element.containsPoint(coordinatesPressed)){

                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        element.setPointerID(eventID);
                        break;
                    case MotionEvent.ACTION_UP:
                        element.onClick();
                        break;
                    default:
                        return false;
                }
            }
        }

        /*for (Button element : pauseScreen.getInputElements()){
            Log.d("INPUTMANAGER", "ticking pausescreenelements!");

            if (element.containsPoint(coordinatesPressed)){

                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        element.setPointerID(eventID);
                        break;
                    case MotionEvent.ACTION_UP:
                        element.onClick();
                        break;
                    default:
                        return false;
                }
            }
        }*/

        return true;
    }

    private boolean handleWhilePaused(MotionEvent event, int eventID, int eventIndex, Point coordinatesPressed){

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:

                if (pauseButton.containsPoint(coordinatesPressed)){
                    pauseButton.setPointerID(eventID);
                } else {
                    checkDebugButtonsOnDown(eventID, coordinatesPressed);

                    if (returnToMenuButton.containsPoint(coordinatesPressed))
                        returnToMenuButton.setPointerID(eventID);
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

                    //Log.d("hb::INPUT", "index: " + eventIndex + ", id: " + eventID);

                    //Log.d("hb::INPUTIDS", "thumb: " + thumbstick.getID() + ", rot: " + rotationThumbstick.getID());
                    if (thumbstick.hasID(eventID))
                        thumbstick.setActivePosition(coordinatesPressed);
                    /*else if (primaryWeaponButton.hasID(eventID))
                        levelState.addBullet(levelState.getPlayer().shoot());*/
                    else if (secondaryWeaponButton.hasID(eventID))
                        levelState.addBullet(levelState.getPlayer().shootSecondary());
                    else if (rotationThumbstick.hasID(eventID)){
                        rotationThumbstick.setActivePosition(coordinatesPressed);
                        levelState.addBullet(levelState.getPlayer().shoot());
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

            returnToMenuButton.cancel();
        } else {
            for (Button button : debugButtons) {
                if (button.hasID(eventID))
                    button.onClick();
            }

            if (returnToMenuButton.hasID(eventID))
                returnToMenuButton.onClick();
        }
    }

    private void handleUpResumed(int eventID){
        Log.d("hb::INPUTUP", "up: " + eventID);

        if (pauseButton.hasID(eventID)) {
            //cancel any outstanding pressed buttons
            secondaryWeaponButton.cancel();
            thumbstick.cancel();

            pauseButton.onClick();
            activePopup = pauseScreen;
            activePopup.show();

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
            thumbstick.draw(canvas, textPaint);

            /*primaryWeaponButton.setLabel("∞");
            primaryWeaponButton.draw(canvas, textPaint);*/

            secondaryWeaponButton.setLabel(levelState.getPlayer().getSecondaryAmmo()+"");
            secondaryWeaponButton.draw(canvas, textPaint);

            rotationThumbstick.draw(canvas, textPaint);

            pauseButton.draw(canvas, textPaint);
        } else {
            pauseScreen.draw(canvas, new Point());
            /*returnToMenuButton.draw(canvas, textPaint);

            if (debugButtons != null) {
                for (Button button : debugButtons) {
                    button.draw(canvas, textPaint);
                }
            }*/
        }


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

    public Button getReturnToMenuButton() {
        return returnToMenuButton;
    }

    public void setReturnToMenuButton(Button returnToMenuButton) {
        this.returnToMenuButton = returnToMenuButton;
    }
}