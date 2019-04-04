package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.*;
import bham.student.txm683.heartbreaker.physics.fields.DoorField;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.utils.BenchMarker;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.*;

public class TankCollisionManager {
    private static final String TAG = "CollisionTools";
    private BenchMarker benchMarker;

    private LevelState levelState;

    private List<SpatialBin> spatialBins;

    private HashSet<String> checkedPairNames;
    private HashSet<String> doorsToOpen;
    private HashSet<Explosion> seenExplosions;

    public TankCollisionManager(LevelState levelState){
        this.levelState = levelState;

        this.spatialBins = new ArrayList<>();
        seenExplosions = new HashSet<>();

        initSpatPatV4();

        benchMarker = new BenchMarker();
    }

    public void checkCollisions(){
        benchMarker.begin();
        applySpatPatV2();
        benchMarker.output("collision rough-grain");

        benchMarker.begin();
        fineGrainCollisionDetection();
        benchMarker.output("collision fine-grain");

        benchMarker.begin();
        aiSight();
        benchMarker.output("ai sight");

    }

    private void fillBins(){
        //add each static to the permanent list in the correct spatial bin
        for (Collidable collidable : levelState.getStaticCollidables()){

            for (SpatialBin bin : spatialBins){
                if (bin.getBoundingBox().intersecting(collidable.getBoundingBox())){

                    if (collidable instanceof Door){
                        bin.addPermanent(((Door) collidable).getPrimaryField());
                    }

                    //if the collidable intersects this bin's bounding box, add it to the bin's permanent list
                    bin.addPermanent(collidable);
                }
            }
        }
    }

    private void initSpatPatV4(){

        UniqueID uniqueID = new UniqueID();

        int numHCells = 4;
        int numVCells = 4;

        int cellWidth = (int) levelState.getMap().getWidth()/numHCells;
        int cellHeight = (int) levelState.getMap().getHeight()/numVCells;

        for (int i = 1; i <= numHCells; i++){
            int l = (i-1) * cellWidth;
            int r = i * cellWidth;

            for (int j = 1; j <= numVCells; j++){
                int t = (j-1) * cellHeight;
                int b = j * cellHeight;

                spatialBins.add(new SpatialBin(uniqueID.id(), new BoundingBox(l,t,r,b)));
            }
        }

        fillBins();
    }

    private void applySpatPatV2(){
        //clear last tick's collision bins
        for (SpatialBin bin : spatialBins){
            bin.clearTemps();
        }

        for (Collidable collidable : levelState.getNonStaticCollidables()){

            if (collidable instanceof  Explosion)
                seenExplosions.add((Explosion) collidable);

            if (!addToBin(collidable)){
                Log.d("hb::CollisionTools", collidable.getName() + " is not in a room");

                if (collidable instanceof MoveableEntity){
                    CollisionTools.moveEntityCenter(collidable, ((MoveableEntity) collidable).getSpawn());
                    addToBin(collidable);
                }
            }
        }
    }

    public List<SpatialBin> getSpatialBins() {
        return spatialBins;
    }

    private boolean addToBin(Collidable collidable){
        boolean added = false;
        for (SpatialBin bin : spatialBins){
            if (bin.getBoundingBox().intersecting(collidable.getBoundingBox())){
                //if the collidable intersects this bin's bounding box, add it to the bin's temp list
                bin.addTemp(collidable);
                added = true;
            }
        }
        return added;
    }

    private void fineGrainCollisionDetection(){
        checkedPairNames = new HashSet<>();

        doorsToOpen = new HashSet<>();

        Collidable firstCollidable;
        Collidable secondCollidable;

        Vector pushVector;

        //iterate through bins
        List<Collidable> bin;
        for (SpatialBin spatialBin : spatialBins){
            bin = spatialBin.getCollidables();

            if (bin.size() > 1){

                //check each pair of entities in current bin
                for (int i = 0; i < bin.size() - 1; i++){
                    for (int j = i+1; j < bin.size(); j++) {

                        firstCollidable = bin.get(i);
                        secondCollidable = bin.get(j);

                        //if both entities are static or not solid, skip
                        if (firstCollidable instanceof Damageable || secondCollidable instanceof Damageable){
                            //do nothing
                        } else if ((!firstCollidable.canMove() && !secondCollidable.canMove()) ||
                                (!firstCollidable.isSolid() && !secondCollidable.isSolid())) {
                            continue;
                        }

                        //if the two entities have already been checked together for collisions, skip
                        if (checkedPairNames.contains(firstCollidable.getName() + secondCollidable.getName())) {
                            continue;
                        }

                        //start of collision checking
                        if (!firstCollidable.isSolid() || !secondCollidable.isSolid()) {
                            //one of the entities is not a solid and the collision does not need to be resolved

                            Collidable solidEntity = firstCollidable.isSolid() ? firstCollidable : secondCollidable;
                            Collidable nonSolidEntity = firstCollidable.isSolid() ? secondCollidable : firstCollidable;

                            if (nonSolidEntity instanceof Explosion){
                                pushVector = CollisionTools.collisionCheckCircleAndPolygon(((Explosion) nonSolidEntity).getCircle(), solidEntity);

                                Log.d(TAG, "checking explosion with: " + solidEntity.getName());

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveExplosion((Explosion) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof Projectile){
                                pushVector = CollisionTools.collisionCheckTwoPolygonalCollidables(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveProjectileHit((Projectile) nonSolidEntity, solidEntity, pushVector);
                                }
                            } else if (nonSolidEntity instanceof DoorField){
                                pushVector = CollisionTools.collisionCheckTwoPolygonalCollidables(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveDoorFieldActivation((DoorField) nonSolidEntity);
                                }
                            } else if (nonSolidEntity instanceof Pickup){
                                pushVector = CollisionTools.collisionCheckTwoPolygonalCollidables(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolvePickupActivation((Pickup) nonSolidEntity, solidEntity);
                                }
                            }

                        } else {
                            //both collidables are solid
                            pushVector = CollisionTools.collisionCheckTwoPolygonalCollidables(firstCollidable, secondCollidable);

                            if (!pushVector.equals(Vector.ZERO_VECTOR)) {
                                //collision occurred
                                CollisionTools.resolveSolidsCollision(firstCollidable, secondCollidable, pushVector, bin);
                            }
                        }
                        //add entity names to the checked names set so that they aren't checked twice
                        addCheckedPairNames(firstCollidable, secondCollidable);
                    }
                }
            }
        }

        List<Explosion> explosions = levelState.getExplosions();
        for (Explosion explosion : seenExplosions){
            explosions.remove(explosion);
        }

        for (Door door : levelState.getMap().getDoors().values()){
            if (doorsToOpen.contains(door.getName())) {
                door.setOpen(true);
            }else
                door.setOpen(false);
        }
    }

    private void resolvePickupActivation(Pickup pickup, Collidable collidable){
        if (collidable instanceof Player){
            if (pickup.getPickupType() == PickupType.HEALTH) {
                ((Player) collidable).restoreHealth(100);
                Log.d(TAG, collidable.getName() + " gained 100 health");
            }
            levelState.removePickup(pickup);
        }
    }

    private void resolveExplosion(Explosion explosion, Collidable collidable){
        if (collidable instanceof Damageable){
            if (((Damageable) collidable).inflictDamage(explosion.getDamage())){
                Log.d(TAG, collidable.getName() + " has died");

                if (collidable instanceof AIEntity){
                    levelState.aiDied((AIEntity) collidable);
                }
            }
            Log.d(TAG, "explosion hit " + collidable.getName() + " and dealt " + explosion.getDamage() + " damage. " +
                    "health now at " + ((Damageable) collidable).getHealth());
        }
    }

    private void resolveProjectileHit(Projectile projectile, Collidable collidable, Vector pV){
        //if the projectile damages on contact and the collidable can take damage, damage it

        if (collidable instanceof Damageable && !projectile.getOwner().equals(collidable.getName())) {
            //only damage the collidable if the projectile doesn't belong to them
            if (!projectile.getOwner().equals(collidable.getName()) && ((Damageable) collidable).inflictDamage(projectile.getDamage())) {
                Log.d(TAG, collidable.getName() + " has died");

                if (collidable instanceof AIEntity) {
                    levelState.aiDied((AIEntity) collidable);
                }
            } else {
                Log.d(TAG, collidable.getName() + " hit by projectile. health now at " + ((Damageable) collidable).getHealth());
            }
        }

        if (!projectile.getOwner().equals(collidable.getName()))
            levelState.removeBullet(projectile);
    }

    private void resolveDoorFieldActivation(DoorField doorField){
        Door fieldOwner = levelState.getMap().getDoors().get(doorField.getOwner());

        if (fieldOwner != null && fieldOwner.isUnlocked()) {
            //if the interaction field belongs to a door, and it's unlocked
            doorsToOpen.add(fieldOwner.getName());
        }
    }

    private void addCheckedPairNames(Collidable entity1, Collidable entity2){
        checkedPairNames.add(entity1.getName() + entity2.getName());
        checkedPairNames.add(entity2.getName() + entity1.getName());
    }

    private void aiSight(){
        for (AIEntity aiEntity : levelState.getAliveAIEntities()){

            Vector ray = new Vector(aiEntity.getCenter(), levelState.getPlayer().getCenter());
            boolean blocked;
            boolean friendlyBlocking;

            if (ray.getLength() < 1500){
                blocked = false;
                friendlyBlocking = false;

                for (Wall wall : levelState.getMap().getWalls()){
                    if (CollisionTools.collisionCheckRay(wall, ray)){
                        blocked = true;
                        break;
                    }

                }

                if (!blocked) {
                    for (Door door : levelState.getMap().getDoors().values()) {
                        if (!door.isOpen()) {
                            if (CollisionTools.collisionCheckRay(door, ray)) {
                                blocked = true;
                                break;
                            }
                        }
                    }
                }

                if (!blocked){

                    for (AIEntity entity : levelState.getAliveAIEntities()){
                        if (!entity.equals(aiEntity)){
                            if (CollisionTools.collisionCheckRay(entity, ray)){
                                friendlyBlocking = true;
                                break;
                            }
                        }
                    }
                }


            } else {
                blocked = true;
                friendlyBlocking = false;
            }

            Log.d("SIGHT", "vector: " + ray + ", blocked: " + blocked);
            aiEntity.getContext().addCompulsory(SIGHT_VECTOR, ray);
            aiEntity.getContext().addCompulsory(SIGHT_BLOCKED, blocked);
            aiEntity.getContext().addCompulsory(FRIENDLY_BLOCKING_SIGHT, friendlyBlocking);
        }
    }
}
