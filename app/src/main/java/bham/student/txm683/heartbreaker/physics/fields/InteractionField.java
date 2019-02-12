package bham.student.txm683.heartbreaker.physics.fields;

import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.CollidableType;

public abstract class InteractionField implements Collidable {

    private String name;
    private String owner;

    public InteractionField(String owner, String name){
        this.name = name;
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CollidableType getCollidableType() {
        return CollidableType.INTERACTION_FIELD;
    }
}