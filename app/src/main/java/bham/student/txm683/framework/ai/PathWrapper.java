package bham.student.txm683.framework.ai;

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
            //there are enough nodes in the path to perform interpolation at lease once.

            //stores the last interpolated value
            Point lastIPoint = null;

            //add the starting node
            interpolatedPath.add(basePath.get(0));


            for (int i = 0; i < basePath.size() - 2; i+=2){
                //visit every other node in the basePath

                //the starting value of t
                float t = 0.25f;

                //create a new Bézier curve using the current path point, and the following two path points
                QCurve qCurve = new QCurve(basePath.get(i), basePath.get(i+1), basePath.get(i+2));
                //evaluate the curve at the starting value of t, but do not add it to
                //the interpolated path
                Point p = qCurve.evalQCurve(t);


                if (lastIPoint != null){
                    //if this is not the first curve to be evaluated

                    //form a new curve starting at the last interpolated point of the previous curve,
                    //ending at the first interpolated point of qCurve.
                    QCurve qCurve1 = new QCurve(lastIPoint, basePath.get(i), p);

                    interpolateCurve(qCurve1);
                }

                //evaluate qCurve at the fixed time step
                lastIPoint = interpolateCurve(qCurve);
            }

            if (basePath.size() % 2 == 0){
                //if the path is an even length, the last node wont have been interpolated
                QCurve qCurve = new QCurve(lastIPoint, basePath.get(basePath.size()-2), basePath.get(basePath.size()-1));
                interpolateCurve(qCurve);
            }

            //add the last node to the path, as the value of t never reaches 1.
            interpolatedPath.add(basePath.get(basePath.size()-1));
        } else {
            //there aren't enough points to interpolate
            interpolatedPath = basePath;
        }
    }

    private Point interpolateCurve(QCurve qCurve){
        float t = 0.25f;
        Point lastIPoint = null;

        while (Float.compare(t, 1f) < 1){
            //evaluate the curve whilst t is less than 1

            Point p = qCurve.evalQCurve(t);

            if (Float.compare(t, 1f) != 0) {
                //add the interpolated point to the path provided the value of t
                //is not equal to 1
                interpolatedPath.add(p);

                //update the last interpolated point field
                lastIPoint = p;
            }

            t += 0.25;
        }

        return lastIPoint;
    }
}
