package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Entity {

    private final String TAG;

    private String name;
    private Vector position;
    private Vector velocity;
    private int width, height;

    public Entity(String name, Vector spawnCoordinates){
        this.name = name;
        this.TAG = "hb::Entity:" + name;

        this.position = spawnCoordinates;

        this.velocity = new Vector(100, 50);

        this.width = 50;
        this.height = 50;
    }

    public void draw(Canvas canvas){
        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(0, 0, 0));
        myPaint.setStrokeWidth(10);
        canvas.drawRect(position.getX(), position.getY(), position.getX()+width, position.getY()+height, myPaint);

        //Log.d(TAG, position.toString());

    }

    public void move(float delta){
        position = Vector.vAdd(position, Vector.sMult(velocity, delta));
    }

    public String getName(){
        return this.name;
    }
}