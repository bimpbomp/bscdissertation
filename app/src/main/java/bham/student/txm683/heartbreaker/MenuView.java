package bham.student.txm683.heartbreaker;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.heartbreaker.utils.Point;

public class MenuView extends SurfaceView implements SurfaceHolder.Callback {
    private int viewWidth;
    private int viewHeight;


    public MenuView(Context context) {
        super(context);

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean eventHandled;

        int eventIndex = event.getActionIndex();
        int eventID = event.getPointerId(eventIndex);

        Point coordinatesPressed = new Point(event.getX(eventIndex), event.getY(eventIndex));

        switch(event.getActionMasked()){

        }

        return super.onTouchEvent(event);
    }
}
