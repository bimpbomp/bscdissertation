package bham.student.txm683.heartbreaker.ai;

import android.graphics.Color;
import bham.student.txm683.framework.ai.behaviours.BKeyType;
import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.entities.weapons.Weapon;
import bham.student.txm683.framework.physics.fields.Explosion;
import bham.student.txm683.framework.utils.Point;
import bham.student.txm683.framework.utils.Vector;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.TankBehaviour;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.entities.TankModifiers;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.rendering.ColorScheme;
import org.json.JSONException;
import org.json.JSONObject;

public class Turret extends AIEntity {

    private Weapon weapon;

    private BNode behaviourTreeRoot;

    private static TankModifiers modifiers;

    static {
        modifiers = new TankModifiers();
        modifiers.setBarrelLengthModifier(0.8f);
        modifiers.setBarrelWidthModifier(1.5f);
        modifiers.setTurretSizeModifier(1.4f);
    }

    public Turret(String name, Point center, int size, int colorValue, int initialHealth) {
        super(name, center, size, 200, 5, new TankBody(center, size, colorValue, modifiers),initialHealth);

        this.weapon = new BasicWeapon(name, 2000, 60, 30f, getShape().getColor());

        this.behaviourTreeRoot = TankBehaviour.turretTree();
    }

    public Turret(String name, Point center){
        this(name ,center, 150, ColorScheme.TURRET_COLOR, 200);
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
    public void onDeath() {
        if (context.containsCompulsory(BKeyType.LEVEL_STATE)){
            ((LevelState) context.getCompulsory(BKeyType.LEVEL_STATE)).addExplosion(
                    new Explosion(getName() +"death", getName(), getCenter(), getWidth()*1.5f, 100, Color.RED));
        }
    }

    @Override
    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {
        /*behaviourTreeRoot.process(context);

        super.tick(secondsSinceLastGameTick);*/
    }
}
