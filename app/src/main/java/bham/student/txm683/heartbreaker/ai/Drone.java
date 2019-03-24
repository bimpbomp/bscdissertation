package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Behaviour;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Vector;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class Drone extends AIEntity{

    private Weapon weapon;
    private BNode behaviourTreeRoot;
    private BNode shootTreeRoot;

    public Drone(String name, Point center, int size, int colorValue, float maxSpeed, int initialHealth) {
        super(name, center, size, maxSpeed, 1, constructShape(center, size, colorValue), initialHealth);

        this.weapon = new BasicWeapon(getName(), 20);

        this.behaviourTreeRoot = Behaviour.droneTree();
        this.shootTreeRoot = Behaviour.shootBehaviour();
    }

    public Drone(String name, Point center){
        this(name, center, 100, ColorScheme.CHASER_COLOR,
                300, 100);
    }

    @Override
    protected void initContext() {
        super.initContext();

        context.addVariable("evasion_magnitude", 25);
        context.addVariable("path_magnitude", 30);
    }

    private static Shape constructShape(Point center, int size, int colorValue){
        return new TankBody(center, size, colorValue);
    }

    public static Drone build(JSONObject jsonObject, int tileSize) throws JSONException {
        Point center = new Point(jsonObject.getJSONObject("sp")).sMult(tileSize);
        String name = jsonObject.getString("name");

        Drone drone = new Drone(name, center);

        if (jsonObject.has("osr")){
            Vector osr = new Vector(new Point(jsonObject.getJSONObject("osr")));
            drone.getShape().rotate(osr);

            drone.getContext().addVariable("osr", osr);
        }

        if (jsonObject.has("drops")){
            PickupType drops = PickupType.valueOf(jsonObject.getString("drops"));
            drone.setDrops(drops);
        }

        return drone;
    }

    @Override
    public void onDeath() {
        Random r = new Random();
        int i = r.nextInt(100);

        if (context.containsKeys(BKeyType.LEVEL_STATE)) {
            LevelState levelState = (LevelState) context.getValue(BKeyType.LEVEL_STATE);
            if (i < 65) {
                levelState.getPickups().add(new Pickup("HEALTH" + i, PickupType.HEALTH, getCenter()));
            }
        }
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {

        behaviourTreeRoot.process(context);
        shootTreeRoot.process(context);

        super.tick(secondsSinceLastGameTick);
    }
}
