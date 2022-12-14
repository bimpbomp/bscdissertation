package bham.student.txm683.framework.physics;

import bham.student.txm683.framework.utils.BoundingBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpatialBin {

    private int id;
    private BoundingBox boundingBox;
    private Set<Collidable> permanentEntities;
    private Set<Collidable> temporaryEntities;

    public SpatialBin(int id, BoundingBox boundingBox){
        this.id = id;
        this.boundingBox = boundingBox;

        this.permanentEntities = new HashSet<>();
        this.temporaryEntities = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public void addPermanent(Collidable collidable){
        this.permanentEntities.add(collidable);
    }

    public void addTemp(Collidable temp){
        this.temporaryEntities.add(temp);
    }

    public void clearTemps(){
        this.temporaryEntities.clear();
    }

    public List<Collidable> getCollidables(){
        List<Collidable> collidables = new ArrayList<>(permanentEntities);
        collidables.addAll(temporaryEntities);
        return collidables;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public boolean contains(Collidable collidable){
        return (temporaryEntities.contains(collidable)) || (permanentEntities.contains(collidable));
    }

    public List<Collidable> getTemps(){
        return new ArrayList<>(temporaryEntities);
    }
}
