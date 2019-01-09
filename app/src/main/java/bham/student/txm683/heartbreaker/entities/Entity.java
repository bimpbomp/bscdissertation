package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

public class Entity {

    String TAG;

    private String name;
    private int id;

    private Vector position;
    private Vector velocity;
    private float movementMaxSpeed;
    private Vector movementUnitVector;

    private int boundingWidth, boundingHeight;

    private Paint currentColor;
    private Paint textColor;
    private int defaultColor;

    private boolean collided;

    /**
     * Creates a new Entity.
     * id is set to {@link bham.student.txm683.heartbreaker.utils.UniqueID#UNASSIGNED UniqueID.UNASSIGNED} until
     * overwritten by a call to {@link #setID(int) setID}.
     * @param name Used to easily identify entity
     * @param spawnCoordinates coordinates to place new Entity specified as the top left corner
     */
    public Entity(String name, Vector spawnCoordinates, int entityColor){
        this.name = name;
        this.id = UniqueID.UNASSIGNED;
        this.TAG = "hb::Entity:" + name;

        this.position = spawnCoordinates;

        this.velocity = new Vector();

        this.movementUnitVector = new Vector();
        this.movementMaxSpeed = 150f;

        this.boundingWidth = 50;
        this.boundingHeight = 50;

        textColor = new Paint();
        textColor.setColor(Color.BLACK);
        textColor.setStrokeWidth(10f);
        textColor.setTextSize(20f);

        this.defaultColor = entityColor;

        this.currentColor = new Paint();
        this.currentColor.setColor(entityColor);
        this.currentColor.setStrokeWidth(10);

        this.collided = false;
    }

    /**
     * Creates an entity instance from a String for state restoration
     * @param saveString String containing Entity state information in JSON format
     */
    //TODO enable state restoration from savefile
    private Entity(String saveString){

    }

    //TODO fix stuttering from interpolated position being calculated from last tick's movement vector

    /**
     * Draws the entity to the given canvas
     * @param canvas Canvas to draw to.
     * @param timeSinceLastGameTick time since last game tick in seconds for interpolation of position
     */
    public void draw(Canvas canvas, float timeSinceLastGameTick){
        //Vector interpolatedPosition = getInterpolatedPosition(timeSinceLastGameTick);
        Vector interpolatedPosition = getCurrentPosition();

        if (collided){
            currentColor.setColor(Color.LTGRAY);
            collided = false;
        } else {
            currentColor.setColor(defaultColor);
        }

        canvas.drawRect(interpolatedPosition.getX(), interpolatedPosition.getY(), interpolatedPosition.getX()+ boundingWidth, interpolatedPosition.getY()+ boundingHeight, currentColor);

        canvas.drawText(name, interpolatedPosition.getX(), interpolatedPosition.getY(), textColor);
        //Log.d(TAG, position.toString());

    }

    /**
     * Updates the position vector based on time passed and current movement unit vector.
     * @param timeSinceLastGameTick time since last game tick, in seconds
     */
    public void move(float timeSinceLastGameTick){
        position = getNextPosition(timeSinceLastGameTick);
    }

    /**
     * Returns the interpolated position for rendering between game ticks
     * This method does NOT update the position with the new value, only returns it.
     * @param timeSinceLastGameTick time since the last game tick in seconds
     * @return Interpolated position of entity
     */
    public Vector getInterpolatedPosition(float timeSinceLastGameTick){
        return getNextPosition(timeSinceLastGameTick);
    }

    /**
     * Returns the new position vector for this entity based on time passed since last update.
     * This method does NOT update the position with the new value, only returns it.
     * @param timeSinceLastGameTick time passed since last game tick in seconds
     * @return New position vector = oldPositionVector + (maxSpeed*time)*movementVector
     */
    private Vector getNextPosition(float timeSinceLastGameTick){
        if (Float.compare(timeSinceLastGameTick, 0f) == 0){
            return position;
        }
        return Vector.vAdd(position, Vector.sMult(movementUnitVector, timeSinceLastGameTick*movementMaxSpeed));
    }

    public Vector getCurrentPosition(){
        return this.position;
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

    public int getID(){
        return this.id;
    }

    /**
     * Sets the id property of entity to the given value
     * @param id The new id, must be unique. Assure this by passing a {@link UniqueID#id() UniqueID.id} call.
     */
    public void setID(int id){
        this.id = id;
    }

    /**
     * Generates a String in JSON for saving state
     * @return String in JSON format
     */
    public String getSaveString(){
        //TODO implement state saving to file
        return "";
    }

    public Pair<Integer, Integer> getBoundingDimensions(){
        return new Pair<>(boundingWidth, boundingHeight);
    }

    public void setColor(int color){
        this.currentColor.setColor(color);
    }

    public void setBoundingDimensions(int bW, int bH){
        this.boundingWidth = bW;
        this.boundingHeight = bH;
    }

    public void resetToDefaultColor(){
        currentColor.setColor(defaultColor);
    }

    public boolean isCollided() {
        return collided;
    }

    public void setCollided(boolean collided) {
        this.collided = collided;
    }

    public void setName(String name) {
        this.name = name;
    }
}