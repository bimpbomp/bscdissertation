package bham.student.txm683.heartbreaker.ai;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.entities.entityshapes.Shape;
import bham.student.txm683.heartbreaker.entities.weapons.Weapon;
import bham.student.txm683.heartbreaker.physics.CollidableType;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.rendering.Renderable;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

public abstract class AIEntity extends MoveableEntity implements Renderable, Damageable {

    private Tile[] path;

    private LevelState levelState;

    protected BContext context;

    protected PickupType drops;

    private boolean isOnScreen;

    public AIEntity(String name, Point spawn, int maxDimension, float maxSpeed, Shape shape) {
        super(name, spawn, maxDimension, maxSpeed, shape);
        context = new BContext();

        isOnScreen = false;
    }

    public boolean isOnScreen() {
        return isOnScreen;
    }

    public void setOnScreen(boolean onScreen) {
        isOnScreen = onScreen;
    }

    public PickupType getDrops() {
        return drops;
    }

    public void setDrops(PickupType drops) {
        this.drops = drops;
    }

    public abstract Weapon getWeapon();

    public BContext getContext() {
        return context;
    }

    public abstract int getWidth();

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.AI_ENTITY;
    }

    public void setLevelState(LevelState levelState){
        Log.d("AIENTITY", getName() + " has levelState added");

        this.levelState = levelState;
        this.context.addPair(BKeyType.LEVEL_STATE, levelState);
    }

    public Tile[] getPath() {
        return path;
    }

    public void setPath(Tile[] path) {
        this.path = path;
    }
}
