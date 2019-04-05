package bham.student.txm683.framework.utils;

import android.util.Log;

public class QCurve {

    private Point p0;
    private Point p1;
    private Point p2;

    public QCurve(Point p0, Point p1, Point p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Point evalQCurve(float t){
        if (t < 0)
            t = 0;
        else if (t > 1)
            t = 1;

        Log.d("EVALSTART", "START");
        Log.d("EVAL", "t: " + t + ", " + p0 + ", " + p1 + ", " + p2);
        Point tp0 = p0.sMult((1-t)*(1-t));
        Point tp1 = p1.sMult((1-t)*t*2);
        Point tp2 = p2.sMult(t*t);

        Log.d("EVALL", tp0 + ", " + tp1 + ", " + tp2);
        Log.d("EVALEND","END");

        return tp0.add(tp1).add(tp2);
    }
}
