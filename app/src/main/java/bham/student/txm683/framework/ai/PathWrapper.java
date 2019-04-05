package bham.student.txm683.framework.ai;

import android.util.Log;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.QCurve;

import java.util.ArrayList;
import java.util.List;

public class PathWrapper {

    private List<Point> basePath;

    private List<Integer> basePathMeshIds;

    private List<Point> interpolatedPath;

    public PathWrapper(List<Point> basePath, List<Integer> basePathMeshIds){
        this.basePath = basePath;
        this.basePathMeshIds = basePathMeshIds;

        this.interpolatedPath = new ArrayList<>();

        interpolatePath();
    }

    public List<Point> basePath(){
        return this.basePath;
    }

    public List<Point> getIPath(){
        return this.interpolatedPath;
    }

    public List<Integer> getMeshIds() {
        return basePathMeshIds;
    }

    private void interpolatePath(){
        if (basePath.size() >= 3){

            Point lastIPoint = null;

            interpolatedPath.add(basePath.get(0));

            for (int i = 0; i < basePath.size() - 2; i+=2){

                float t = 0.25f;

                QCurve qCurve = new QCurve(basePath.get(i), basePath.get(i+1), basePath.get(i+2));
                Point p = qCurve.evalQCurve(t);

                Log.d("QCURVE", basePath.get(i) + ", " + basePath.get(i+1) + ", " + basePath.get(i+2));

                if (lastIPoint != null){
                    QCurve qCurve1 = new QCurve(lastIPoint, basePath.get(i), p);

                    interpolateCurve(qCurve1);
                }

                lastIPoint = interpolateCurve(qCurve);

            }

            if (basePath.size() % 2 == 0){
                //if the path is an even length, the last node wont have been interpolated
                QCurve qCurve = new QCurve(lastIPoint, basePath.get(basePath.size()-2), basePath.get(basePath.size()-1));
                interpolateCurve(qCurve);
            }

            interpolatedPath.add(basePath.get(basePath.size()-1));
        } else {
            interpolatedPath = basePath;
        }
    }

    private Point interpolateCurve(QCurve qCurve){
        float t = 0.25f;
        Point lastIPoint = null;
        while (Float.compare(t, 1f) < 1){

            Point p = qCurve.evalQCurve(t);

            if (Float.compare(t, 1f) != 0) {
                interpolatedPath.add(p);
                lastIPoint = p;
            }

            Log.d("QCURVEE", "t: " + t +  p.toString());

            t += 0.25;
        }

        return lastIPoint;
    }
}
