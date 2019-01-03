package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Entity {

    String TAG;

    private String name;
    private Vector position;
    private Vector velocity;
    private float movementMaxSpeed;
    private Vector movementUnitVector;
    private int width, height;

    private Paint color;

    public Entity(String name, Vector spawnCoordinates){
        this.name = name;
        this.TAG = "hb::Entity:" + name;

        this.position = spawnCoordinates;

        this.velocity = new Vector(10, 5);

        this.movementUnitVector = new Vector();
        this.movementMaxSpeed = 500f;

        this.width = 50;
        this.height = 50;

        color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(10);
    }

    public void draw(Canvas canvas, float timeSinceLastGameTick){
        Vector interPolatedPosition = interpolatedPosition(timeSinceLastGameTick);
        //Vector interPolatedPosition = position;
        canvas.drawRect(interPolatedPosition.getX(), interPolatedPosition.getY(), interPolatedPosition.getX()+width, interPolatedPosition.getY()+height, color);

        //Log.d(TAG, position.toString());

    }

    public void move(float timeSinceLastGameTick){
        position = getNextPosition(timeSinceLastGameTick);
    }

    public Vector interpolatedPosition(float timeSinceLastGameTick){
        return getNextPosition(timeSinceLastGameTick);
    }

    public Vector getCurrentPosition(){
        return this.position;
    }

    public Vector getNextPosition(float timeSinceLastGameTick){
        return Vector.vAdd(position, Vector.sMult(movementUnitVector, timeSinceLastGameTick*movementMaxSpeed));
    }

    public String getName(){
        return this.name;
    }

    public void setMovementUnitVector(Vector movementUnitVector){
        this.movementUnitVector = movementUnitVector;
    }

    public void setMovementMaxSpeed(float movementMaxSpeed){
        this.movementMaxSpeed = movementMaxSpeed;
    }

}