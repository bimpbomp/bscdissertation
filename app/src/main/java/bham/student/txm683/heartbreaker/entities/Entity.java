package bham.student.txm683.heartbreaker.entities;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Entity {

    private int x,y;
    private int xV, yV;
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int width, height;

    public Entity(){
        this.x = 100;
        this.y = 100;

        this.xV = 10;
        this.yV = 5;

        this.width = 50;
        this.height = 60;
    }

    public void draw(Canvas canvas){
        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(255, 255, 255));
        myPaint.setStrokeWidth(10);
        canvas.drawRect(x, y, x+width, y+width, myPaint);
    }

    public void update(){
        if (x<0 && y<0){
            x = screenWidth/2;
            y = screenHeight/2;
        } else {
            x += xV;
            y += yV;

            if ((x > screenWidth - width)){
                x = screenWidth-width;
                xV*=-1;
            } else if (x < 0){
                x = 0;
                xV *= -1;
            }

            if ((y > screenHeight - height)){
                y = screenHeight-height;
                yV*=-1;
            } else if (y < 0){
                y=0;
                yV *= -1;
            }
        }
    }
}