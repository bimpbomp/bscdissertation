package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.rendering.HealthBar;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.OVERLORD;
import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.PATH;

public abstract class AIEntity extends MoveableEntity implements Renderable, Damageable {

    protected BContext context;

    private PickupType drops;

    private boolean isOnScreen;

    private HealthBar healthBar;

    public AIEntity(String name, Point spawn, int maxDimension, float maxSpeed, int mass, Shape shape, int initialHealth) {
        super(name, spawn, maxDimension, maxSpeed, mass, shape);
        context = new BContext();
        initContext();

        this.healthBar = new HealthBar(initialHealth);

        isOnScreen = false;
    }

    protected void initContext(){
        context.addValue(BKeyType.CONTROLLED_ENTITY, this);

        context.addVariable("arrived", false);
        context.addVariable("arriving", false);

        context.addVariable("evasion_magnitude", 50);

        context.addVariable("seek_magnitude", 50);

        context.addVariable("arrival_distance", 200);
        context.addVariable("arrival_magnitude", 100);

        context.addVariable("path_velocity_time_step", 0.04f);
        context.addVariable("path_magnitude", 50);
        context.addVariable("path_distance_for_arrived", 75);
        context.addVariable("path_distance_for_arrival", 200);

        context.addVariable("aim_max_inaccuracy_angle", 0.03f);
    }

    public void setLevelState(LevelState levelState){
        this.context.addValue(BKeyType.LEVEL_STATE, levelState);
    }

    void setOverlord(Overlord overlord){
        context.addValue(OVERLORD, overlord);
    }

    public void onDeath(){

    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        super.draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);
        healthBar.draw(canvas, getCenter().add(renderOffset).add(0,75));

        drawPath(canvas, renderOffset);
    }

    private void drawPath(Canvas canvas, Point renderOffset){
        Paint paint = new Paint();
        if (context.containsKeys(PATH)){

            paint.setColor(getShape().getColor());

            List<Point> path = ((PathWrapper) context.getValue(PATH)).basePath();

            if (path.size() > 1){

                List<Point> iPath = ((PathWrapper) context.getValue(PATH)).getIPath();

                for (int i = 0; i < iPath.size(); i++){
                    Point p = iPath.get(i).add(renderOffset);

                    canvas.drawCircle(p.getX(), p.getY(), 15, paint);

                    if (i < iPath.size()-1) {
                        Point q = iPath.get(i + 1).add(renderOffset);

                        canvas.drawLine(p.getX(), p.getY(), q.getX(), q.getY(), paint);
                    }
                }

                //draw base path
                paint.setColor(Color.BLACK);

                for (Point p : path){
                    p = p.add(renderOffset);
                    canvas.drawCircle(p.getX(), p.getY(), 10, paint);
                }

                for (int i = 0; i < path.size()-1; i++){
                    Point p = path.get(i).add(renderOffset);
                    Point q = path.get(i+1).add(renderOffset);

                    canvas.drawLine(p.getX(), p.getY(), q.getX(), q.getY(), paint);
                }
            }

            if (context.containsVariables("closest_point")){
                paint.setColor(Color.YELLOW);

                Point p = ((Point) context.getVariable("closest_point")).add(renderOffset);
                canvas.drawCircle(p.getX(),p.getY(), 25, paint);
            }
        }

        Point p = getCenter().add(getVelocity().getRelativeToTailPoint()).add(renderOffset);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(p.getX(), p.getY(), 20, paint);
    }

    public float getRadioHealthLeft(){
        return getHealth()/ (float) getInitialHealth();
    }

    public boolean isOnScreen() {
        return isOnScreen;
    }

    public void setOnScreen(boolean onScreen) {
        isOnScreen = onScreen;
    }

    public void setDrops(PickupType drops) {
        this.drops = drops;
    }

    public abstract Weapon getWeapon();

    public BContext getContext() {
        return context;
    }

    public int getWidth(){
        return getMaxDimension();
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getHealth() {
        return healthBar.getHealth();
    }

    @Override
    public void setHealth(int health) {
        healthBar.setHealth(health);
    }

    @Override
    public int getInitialHealth() {
        return healthBar.getInitialHealth();
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        return healthBar.inflictDamage(damageToInflict);
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        healthBar.restoreHealth(healthToRestore);
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.AI_ENTITY;
    }
}
