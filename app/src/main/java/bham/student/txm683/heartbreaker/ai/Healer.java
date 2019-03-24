package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Behaviour;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.entities.TankModifiers;
import bham.student.txm683.heartbreaker.entities.weapons.BasicWeapon;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.map.ColorScheme;
import bham.student.txm683.heartbreaker.utils.Point;

public class Healer extends AIEntity {

    private BNode treeRoot;

    private static TankModifiers modifiers;
    static {
        modifiers = new TankModifiers();
        modifiers.setTurretSizeModifier(1);
        modifiers.setBarrelWidthModifier(0);
        modifiers.setBarrelLengthModifier(0);
    }

    public Healer(String name, Point spawn, int maxDimension, float maxSpeed, int mass, int initialHealth) {
        super(name, spawn, maxDimension, maxSpeed, mass, new TankBody(spawn, maxDimension, ColorScheme.HEALER_COLOR, modifiers), initialHealth);

        treeRoot = Behaviour.healerTree();
    }

    @Override
    protected void initContext() {
        super.initContext();
        context.addVariable("healing_min_ratio", 0.04f);
    }

    public Healer(String name, Point spawn){
        this(name, spawn, 100, 400, 1, 50);
    }

    @Override
    public void tick(float secondsSinceLastGameTick) {
        treeRoot.process(context);

        super.tick(secondsSinceLastGameTick);
    }

    @Override
    public Weapon getWeapon() {
        return new BasicWeapon(getName());
    }
}
