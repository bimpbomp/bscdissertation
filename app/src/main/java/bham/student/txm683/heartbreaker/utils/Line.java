package bham.student.txm683.heartbreaker.utils;

public class Line {

    private float constant;

    private float varMin;
    private float varMax;

    private boolean vertical;

    public Line(boolean vertical, float constant, float varMin, float varMax){
        this.vertical = vertical;

        this.constant = constant;
        this.varMax = varMax;
        this.varMin = varMin;
    }

    public void extend(Point p){
        if (vertical) {
            if (p.getY() < varMin)
                varMin = p.getY();

            if (p.getY() > varMax)
                varMax = p.getY();
        } else{
            if (p.getX() < varMin)
                varMin = p.getX();
            if (p.getX() > varMax)
                varMax = p.getX();
        }
    }

    public Point projectOnToLine(Point point) {
        Point pointToReturn;

        if (vertical) {
            if (point.getY() < varMin)
                pointToReturn = new Point(constant, varMin);
            else if (point.getY() > varMax)
                pointToReturn = new Point(constant, varMax);
            else {
                pointToReturn = new Point(constant, point.getY());
            }
        } else{
            if (point.getX() < varMin)
                pointToReturn = new Point(varMin, constant);

            else if (point.getX() > varMax)
                pointToReturn = new Point(varMax, constant);
            else {
                pointToReturn = new Point(point.getX(), constant);
            }
        }

        return pointToReturn;
    }
}
