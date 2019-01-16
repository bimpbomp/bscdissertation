package bham.student.txm683.heartbreaker.entities.entityshapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;

public abstract class EntityShape {

    Point geometricCenter;
    float height;
    float width;

    Paint paint;
    int defaultColor;

    EntityShape(){

    }

    public EntityShape(Point geometricCenter, float width, float height, int colorValue){
        this.geometricCenter = geometricCenter;

        this.height = height;
        this.width = width;

        this.defaultColor = colorValue;

        this.paint = new Paint();
        this.paint.setColor(defaultColor);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setAntiAlias(true);
    }

    public abstract Point[] getVertices();

    public abstract Point[] getInterpolatedVertices(Vector interpolationVector);

    public abstract Point getInterpolatedCenter(Vector interpolationVector);

    public abstract void draw(Canvas canvas, Vector interpolationVector);

    public abstract void setHeight(float newHeight);

    public abstract void setWidth(float newWidth);

    public abstract void setCenter(Point geometricCenter);

    public abstract void move(Vector movementVector);

    @Override
    @NonNull
    public abstract String toString();

    Vector rotateVertexVector(Vector vectorToRotate, float cosAngle, float sinAngle){
        if (!vectorToRotate.isFromOrigin()){
            vectorToRotate = vectorToRotate.translate(geometricCenter.smult(-1));
            vectorToRotate = vectorToRotate.rotate(cosAngle, sinAngle);
            return vectorToRotate.translate(geometricCenter);
        } else {
            return vectorToRotate.rotate(cosAngle, sinAngle);
        }
    }

    public Point getCenter(){
        return geometricCenter;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint(){
        return this.paint;
    }

    public void setColor(int colorValue){
        this.paint.setColor(colorValue);
    }

    public void resetToDefaultColor(){
        this.paint.setColor(defaultColor);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }
}
