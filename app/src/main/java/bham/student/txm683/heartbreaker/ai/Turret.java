package bham.student.txm683.heartbreaker.ai;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Behaviour;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.PATH;

public class Turret extends AIEntity {

    private int health;

    private int width;

    private Weapon weapon;

    private BNode behaviourTreeRoot;

    public Turret(String name, Point center, int size, int colorValue, int initialHealth) {
        super(name, center, size, 200, new TankBody(center, size, colorValue));

        health = initialHealth;

        this.weapon = new BasicWeapon(name, 50, 30, 1.5f);

        this.width = size;

        this.behaviourTreeRoot = Behaviour.turretTree();

        context.addPair(BKeyType.VIEW_RANGE, 600);
        context.addPair(BKeyType.CONTROLLED_ENTITY, this);
        context.addPair(BKeyType.TIME_PER_IDLE, 25);
        context.addPair(BKeyType.HEALTH_BOUND, 50);
        context.addPair(BKeyType.ROT_DAMP, 0.5f);
    }

    public Turret(String name, Point center){
        this(name ,center, 150, ColorScheme.CHASER_COLOR, 300);
    }

    public static Turret build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);
        String name = jsonObject.getString("name");

        Turret turret = new Turret(name, center);

        if (jsonObject.has("osr")){
            Vector initialRotation = new Vector(new Point(jsonObject.getJSONObject("osr")));
            turret.getContext().addVariable("osr", initialRotation);

            turret.getShape().rotate(initialRotation);
        }

        if (jsonObject.has("rotation_lock")){
            turret.getContext().addVariable("rotation_lock", (float) jsonObject.getDouble("rotation_lock"));
        }

        if (jsonObject.has("drops")){
            PickupType drops = PickupType.valueOf(jsonObject.getString("drops"));
            turret.setDrops(drops);
        }

        return new Turret(name, center);
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        behaviourTreeRoot.process(context);

        super.tick(secondsSinceLastGameTick);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean isSolid() {
        return true;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;
        return health < 1;
    }

    @Override
    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;
    }

    @Override
    public void draw(Canvas canvas, Point renderOffset, float secondsSinceLastRender, boolean renderEntityName) {
        getShape().draw(canvas, renderOffset, secondsSinceLastRender, renderEntityName);

        if (renderEntityName)
            drawName(canvas, getCenter().add(renderOffset));

        Paint paint = new Paint();
        if (context.containsKeys(PATH)){
            List<Point> path = ((PathWrapper) context.getValue(PATH)).path();


            paint.setColor(getShape().getColor());

            for (Point p : path){
                p = p.add(renderOffset);
                canvas.drawCircle(p.getX(), p.getY(), 10, paint);
            }

            for (int i = 0; i < path.size()-1; i++){
                Point p = path.get(i).add(renderOffset);
                Point q = path.get(i+1).add(renderOffset);

                canvas.drawLine(p.getX(), p.getY(), q.getX(), q.getY(), paint);
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

    @Override
    public void setColor(int color) {
        getShape().setColor(color);
    }

    @Override
    public void revertToDefaultColor() {
        getShape().revertToDefaultColor();
    }
}
