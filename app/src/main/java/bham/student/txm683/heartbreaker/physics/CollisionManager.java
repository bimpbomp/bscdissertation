package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.TileSet;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.Bomb;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.entities.entityshapes.Circle;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier;
import bham.student.txm683.heartbreaker.entities.weapons.AmmoType;
import bham.student.txm683.heartbreaker.physics.fields.DoorField;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.pickups.Key;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.SIGHT_BLOCKED;
import static bham.student.txm683.heartbreaker.entities.entityshapes.ShapeIdentifier.RECTANGLE;

public class CollisionManager {

    private List<SpatialBin> spatialBins;

    private static final float PUSH_VECTOR_ERROR = 0.001f;

    private LevelState levelState;

    private HashSet<String> checkedPairNames;
    private HashSet<String> doorsToOpen;

    private static String TAG = "hb::CollisionManager";

    public CollisionManager(LevelState levelState){
        this.levelState = levelState;

        this.spatialBins = new ArrayList<>();

        initSpatPatV3();
    }

    public void checkCollisions(){
        applySpatPatV2();

        fineGrainCollisionDetection();

        //checkAIsLineOfSight();

        //generatePlayerVisibleSet();
    }

    private void generatePlayerVisibleSet(){

        Player player = levelState.getPlayer();
        int tileSize = levelState.getMap().getTileSize();
        TileSet tileSet = levelState.getTileSet();

        //clear previous tick's visibility set
        tileSet.clearVisibleSet();

        //get the center of the tile that the player's center lies in
        Tile playerTile = Tile.mapToCenterOfTile(player.getCenter(), tileSize);

        tileSet.addVisibleTile(Tile.mapToTile(player.getCenter(), tileSize));

        //Log.d("hb::TilePlayer", "playercenter: " + player.getCenter() + ", playertile: " + playerTile);
        int numberOfRays = 20;

        //6.3f roughly equals 2Pi.
        float angleBetweenRays = 6.3f / 20;
        float cos = (float) Math.cos(angleBetweenRays);
        float sin = (float) Math.sin(angleBetweenRays);

        //generate rays
        Vector[] rays = new Vector[numberOfRays];

        rays[0] = new Vector(0,-1);
        //Log.d("hb::RayStart", rays[0].getRelativeToTailPoint()+"");
        for (int i = 1; i < rays.length; i++){
            rays[i] = rays[i-1].rotate(cos,sin);
            //Log.d("hb::Ray", "" + rays[i].getRelativeToTailPoint());
        }

        //loop through rays, adding any empty tiles they cross to the visibility set
        Point rayCurrentPoint;
        Tile rayCurrentTile;
        boolean blocked;
        List<Collidable> blockingObjects;

        for (Vector ray : rays){
            ray = ray.sMult(tileSize);
            rayCurrentPoint = new Point(playerTile);

            blocked = false;
            //Log.d("hb::Ray", ray.getRelativeToTailPoint().toString());

            //add on a tileSize to the ray to move to the next tile in it's path
            while(!blocked){
                //get center of the tile that the ray currently lies in
                rayCurrentPoint = rayCurrentPoint.add(ray.getRelativeToTailPoint());
                rayCurrentTile = Tile.mapToTile(rayCurrentPoint, tileSize);

                //if ray is definitely out of map bounds, break
                if (rayCurrentPoint.getX() < 0 || rayCurrentPoint.getX() > levelState.getMap().getWidth()
                        || rayCurrentPoint.getY() < 0 || rayCurrentPoint.getY() > levelState.getMap().getHeight())
                    break;

                //Log.d("hb::Tile", "ray point: " + rayCurrentPoint + ", tile: " + rayCurrentTile);

                //get list of solid objects (currently walls, doors, cores) present in this tile
                blockingObjects = tileSet.getViewBlockingObjectsAtTile(rayCurrentTile);

                //iterate through the blocking objects for this tile, if any of them occupy the center of a tile,
                //consider it blocked
                for (Collidable collidable : blockingObjects){
                    //Log.d("hb::BlockingObject", collidable.getName());
                    if (collidable.getBoundingBox().intersecting(new Point(rayCurrentTile.add(tileSize/2, tileSize/2)))){
                        //Log.d("hb::Ray", rayCurrentTile.toString() + " is blocked");
                        blocked = true;
                        break;
                    }
                }

                //if the tile is not considered blocked, add it to the visible set
                if (!blocked)
                    tileSet.addVisibleTile(rayCurrentTile);
            }
        }
    }

    private void checkAIsLineOfSight(){
        for (AIEntity ai : levelState.getAliveAIEntities()){

            if (levelState.getTileSet().tileIsVisibleToPlayer(Tile.mapToTile(ai.getCenter(), levelState.getTileSet().getTileSize()))){
                ai.getContext().addPair(SIGHT_BLOCKED, false);
            } else {
                ai.getContext().addPair(SIGHT_BLOCKED, true);
            }
        }
    }

   /* private boolean checkIfLOSIsBlocked(Vector vectorToPlayer, SpatialBin bin, Player player, AIEntity ai){
        Vector toPlayerNormal = getEdgeNormal(vectorToPlayer);
        Pair<Float, Float> minMaxAi = projectOntoAxis(toPlayerNormal, ai.getCollisionVertices());
        Pair<Float, Float> minMaxPlayer = projectOntoAxis(toPlayerNormal, player.getCollisionVertices());

        Pair<Float, Float> sightMinMax = new Pair<>(
                Math.min(minMaxAi.first, minMaxPlayer.first),
                Math.max(minMaxPlayer.second, minMaxAi.second)
        );
        //will be altered if the collidable intersects the LOS
        float sightMin = sightMinMax.first;
        float sightMax = sightMinMax.second;

        //construct viewShape
        Point[] viewShapeVertices = getViewShape(ai, player, toPlayerNormal);

        Pair<Float, Float> collidableMinMax;
        for (Collidable collidable : bin.getCollidables()){
            if (!collidable.isSolid() || collidable.getName().equals(player.getName()) || collidable.getName().equals(ai.getName())){
                //if the collidable is the ai, the player, or is not solid, skip to next collidable
                //Log.d("hb::CollidableAiPlayerOrNonSolid", collidable.getName());
                continue;
            }

            //if the collidable is not in between the ai and player, skip to next collidable
            if (applySAT(getEdgeNormals(getEdges(collidable.getShapeIdentifier(), collidable.getCollisionVertices())), viewShapeVertices, collidable.getCollisionVertices()).size() != collidable.getCollisionVertices().length){
                //Log.d("hb::NotBetweenAIAndPlayer", collidable.getName());
                continue;
            }

            collidableMinMax = projectOntoAxis(toPlayerNormal, collidable.getCollisionVertices());
            //Log.d("hb::AiMinMax", "(" + minMaxAi.first + "," + minMaxAi.second + ")");
            //Log.d("hb::PlayerMinMax", "(" + minMaxPlayer.first + "," + minMaxPlayer.second + ")");
            //Log.d("hb::CollidableMinMax", collidable.getName() +  "(" + collidableMinMax.first + "," + collidableMinMax.second + ")");

            if (collidableMinMax.first < sightMin  && collidableMinMax.second > sightMax){
                //complete intersection
                //Log.d("hb::CompleteIntersection", collidable.getName());
                return true;
            }

            if (collidableMinMax.second < sightMin  || collidableMinMax.first > sightMax) {
                //Log.d("hb::NoIntersection", collidable.getName());
                //no intersection
                continue;
            } else if (collidableMinMax.first < sightMax && collidableMinMax.second > sightMax){
                //Log.d("hb::MinPointIntersectsLOS", collidable.getName());
                //min point intersects LOS
                sightMax = collidableMinMax.first;
            } else if (collidableMinMax.second > sightMin && collidableMinMax.first < sightMin){
                //max point intersects LOS
                //Log.d("hb::MaxPointIntersectsLOS", collidable.getName());
                sightMin = collidableMinMax.second;
            }
        }

        //Log.d("hb::ReturnValue", (sightMax - sightMin)+"");
        return Math.abs(sightMax - sightMin) < 1;
    }*/

    /*private Point[] getViewShape(Collidable ai, Collidable player, Vector aiToPlayerNormal){

        Pair<Point, Point> aiMinMax = getMinMaxPointsForAxis(aiToPlayerNormal, ai.getCollisionVertices());
        Pair<Point, Point> playerMinMax = getMinMaxPointsForAxis(aiToPlayerNormal, player.getCollisionVertices());

        return new Point[]{
                playerMinMax.second,
                playerMinMax.first,
                aiMinMax.first,
                aiMinMax.second
        };
    }*/

    /**
     * Adds all the static entities in the game world into any and all spatial bins they overlap's
     * permanent list
     */
    private void initSpatPatV2(){
        SpatialBin spatialBin;
        UniqueID uniqueID = new UniqueID();

        for (Perimeter room : levelState.getMap().getRoomPerimeters()){
            spatialBin = new SpatialBin(uniqueID.id(), room.getBoundingBox());

            spatialBins.add(spatialBin);
        }

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

    private void initSpatPatV3(){
        SpatialBin spatialBin = new SpatialBin(0, levelState.getLevelBoundingBox());

        spatialBins.add(spatialBin);

        for (Collidable collidable : levelState.getStaticCollidables()){
            spatialBin.addPermanent(collidable);

            if (collidable instanceof Door){
                spatialBin.addPermanent(((Door) collidable).getPrimaryField());
            }
        }
    }

    private void applySpatPatV2(){
        //clear last tick's collision bins
        for (SpatialBin bin : spatialBins){
            bin.clearTemps();
        }

        boolean addedToBin;
        for (Collidable collidable : levelState.getNonStaticCollidables()){
            addedToBin = false;

            for (SpatialBin bin : spatialBins){
                if (bin.getBoundingBox().intersecting(collidable.getBoundingBox())){
                    //if the collidable intersects this bin's bounding box, add it to the bin's temp list
                    bin.addTemp(collidable);
                    addedToBin = true;
                }
            }

            if (!addedToBin){
                //TODO add flag for entities that aren't in a room, for correction later on
                Log.d("hb::CollisionManager", collidable.getName() + " is not in a room");
            }
        }
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
                                pushVector = collisionCheckCircleAndPolygon(((Explosion) nonSolidEntity).getCircle(), solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveExplosion((Explosion) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof Projectile){
                                pushVector = collisionCheckCircleAndPolygon(((Projectile) nonSolidEntity).getCircle(), solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveProjectileHit((Projectile) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof DoorField){
                                pushVector = collisionCheckTwoPolygons(nonSolidEntity, solidEntity);
                                //Log.d("hb::SolidNonSolid", "solid: " + solidEntity.getName() + ", and nonSolid: " + nonSolidEntity.getName() + " with owner " + ((DoorField) nonSolidEntity).getOwner());
                                //Log.d("hb:: DoorCollision", pushVector.relativeToString());

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolveDoorFieldActivation((DoorField) nonSolidEntity, solidEntity);
                                }
                            } else if (nonSolidEntity instanceof Pickup){
                                pushVector = collisionCheckTwoPolygons(nonSolidEntity, solidEntity);

                                if (!pushVector.equals(Vector.ZERO_VECTOR)){
                                    //collision occurred
                                    resolvePickupActivation((Pickup) nonSolidEntity, solidEntity);
                                }
                            }

                        } else {
                            //both collidables are solid
                            pushVector = collisionCheckTwoPolygons(firstCollidable, secondCollidable);

                            if (!pushVector.equals(Vector.ZERO_VECTOR)) {
                                //collision occurred
                                resolveSolidsCollision(firstCollidable, secondCollidable, pushVector, bin);
                            }
                        }
                        //add entity names to the checked names set so that they aren't checked twice
                        addCheckedPairNames(firstCollidable, secondCollidable);
                    }
                }
            }
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
            switch (pickup.getPickupType()){

                case KEY:
                    if (pickup instanceof Key)
                        levelState.getPlayer().addKey((Key) pickup);
                    Log.d(TAG, collidable.getName() + " picked up a key");
                    break;
                case BOMB:
                    //add one bomb to any bomb weapons the player is carrying
                    if (((Player) collidable).getAmmoType() == AmmoType.BOMB)
                        ((Player) collidable).addAmmo(1);
                    if (((Player) collidable).getSecondaryAmmoType() == AmmoType.BOMB)
                        ((Player) collidable).addSecondaryAmmo(1);
                    Log.d(TAG, collidable.getName() + " gained a bomb");
                    break;
                case HEALTH:
                    ((Player) collidable).restoreHealth(50);
                    Log.d(TAG, collidable.getName() + " gained 50 health");
                    break;
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

    private void resolveProjectileHit(Projectile projectile, Collidable collidable){
        //if the projectile damages on contact and the collidable can take damage, damage it

        if (!(projectile instanceof Bomb)) {

            if (collidable instanceof Damageable) {
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
                levelState.getBullets().remove(projectile);


        }

    }

    private void resolveDoorFieldActivation(DoorField doorField, Collidable collidable){
        Door fieldOwner = levelState.getMap().getDoors().get(doorField.getOwner());

        if (fieldOwner != null && collidable instanceof Player){
            //if it's locked and the player has the key, then unlock it
            if (fieldOwner.isLocked()){
                for (Key key : ((Player) collidable).getKeys()){

                    if (key.getUnlocks().equals(fieldOwner.getName())) {
                        fieldOwner.setLocked(false);
                        break;
                    }
                }
            }
        }

        if (fieldOwner != null && fieldOwner.isUnlocked()) {
            //if the interaction field belongs to a door, and it's unlocked
            doorsToOpen.add(fieldOwner.getName());
        }
    }

    private void resolveSolidsCollision(Collidable firstCollidable, Collidable secondCollidable, Vector pushVector, List<Collidable> bin){
        //true if the entity doesnt collide with any statics after applying
        //pushVector.
        boolean firstAbleToMove;
        boolean secondAbleToMove;

        Point newCenter;

        //amount added to the entity's center to allow reverting to previous state
        Point firstAmountMoved;
        Point secondAmountMoved;

        //both entities are solid and the collision needs to be resolved.
        if (firstCollidable.canMove() && secondCollidable.canMove()) {

            //resolve collisions with any statics that share a cell with either entity
            isStaticCollision(firstCollidable, bin, true);
            isStaticCollision(secondCollidable, bin, true);

            //update position of first entity with half of the push vector
            firstAmountMoved = pushVector.sMult(0.5f).getRelativeToTailPoint();
            newCenter = firstCollidable.getCenter().add(firstAmountMoved);
            firstCollidable.setCenter(newCenter);

            //update position of second entity with half of the inverted pushVector
            //i.e pushes second entity away from first
            secondAmountMoved = pushVector.sMult(-0.5f).getRelativeToTailPoint();
            newCenter = secondCollidable.getCenter().add(secondAmountMoved);
            secondCollidable.setCenter(newCenter);

            //check if the entities now overlap any statics with their new positions
            firstAbleToMove = isStaticCollision(firstCollidable, bin, false);
            secondAbleToMove = isStaticCollision(secondCollidable, bin, false);

            if (!secondAbleToMove) {
                //if the second entity now overlaps a static, tick it back to it's original position
                //and tick the first entity the other half of the distance so it doesn't overlap the second
                //collision is resolved, tick to next entity pair
                moveEntityCenter(firstCollidable, firstAmountMoved);
                moveEntityCenter(secondCollidable, secondAmountMoved.sMult(-1f));

            } else if (!firstAbleToMove) {
                //if the first entity now overlaps a static, tick it back to it's original position
                //and tick the second entity the other half of the distance so it doesn't overlap the first
                //collision is resolved, tick to next entity pair
                moveEntityCenter(firstCollidable, firstAmountMoved.sMult(-1f));
                moveEntityCenter(secondCollidable, secondAmountMoved);
            }

        } else if (firstCollidable.canMove()) {

            //if only the first entity can tick (is not static), resolution is to add all push
            //vector to first entity
            //collision is resolved, tick to next entity pair
            firstAmountMoved = pushVector.getRelativeToTailPoint();
            newCenter = firstCollidable.getCenter().add(firstAmountMoved);
            firstCollidable.setCenter(newCenter);

        } else if (secondCollidable.canMove()) {
            //if only the second entity can tick (is not static), resolution is to add all push
            //vector to second entity
            //collision is resolved, tick to next entity pair
            secondAmountMoved = pushVector.sMult(-1).getRelativeToTailPoint();
            newCenter = secondCollidable.getCenter().add(secondAmountMoved);
            secondCollidable.setCenter(newCenter);
        }
    }

    //checks that two circles are not intersecting by checking the distance between radii,
    //returns the overlap or the empty vector (if they dont overlap)
    public static Vector collisionCheckTwoCircles(Circle circle1, Circle circle2){

        Vector center2ToCenter1 = new Vector(circle2.getCenter(), circle1.getCenter());
        float distanceBetweenCenters = center2ToCenter1.getLength();
        float radiiSum = circle1.getRadius() + circle2.getRadius();

        if (distanceBetweenCenters < radiiSum){
            return center2ToCenter1.getUnitVector().sMult(radiiSum - distanceBetweenCenters + PUSH_VECTOR_ERROR);
        }
        return Vector.ZERO_VECTOR;
    }

    private static Vector collisionCheckCircleAndPolygon(Circle circle, Collidable polygon){
        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon.getShapeIdentifier(), polygon.getCollisionVertices()));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        Vector pushVector;
        ArrayList<Vector> pushVectors = new ArrayList<>();
        for (Vector axis : orthogonalAxes){
            pushVector = isSeparatingAxis(axis, polygon.getCollisionVertices(),
                    getCircleVerticesForAxis(axis, circle.getCenter(), circle.getRadius()));

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                return Vector.ZERO_VECTOR;
            }
            pushVectors.add(pushVector);
        }

        return getMinimumPushVector(pushVectors, circle.getCenter(), polygon.getCenter());
    }

    //Separating axis theorem for two polygons
    //returns the overlap or the empty vector
    public static Vector collisionCheckTwoPolygons(Collidable polygon1, Collidable polygon2){
        Point[] firstEntityVertices = polygon1.getCollisionVertices();
        Point[] secondEntityVertices = polygon2.getCollisionVertices();

        ArrayList<Vector> edges = new ArrayList<>(getEdges(polygon1.getShapeIdentifier(), firstEntityVertices));
        edges.addAll(getEdges(polygon2.getShapeIdentifier(), secondEntityVertices));

        Vector[] orthogonalAxes = getEdgeNormals(edges);

        List<Vector> pushVectors = applySAT(orthogonalAxes, firstEntityVertices, secondEntityVertices);

        if (pushVectors.size() == orthogonalAxes.length) {
            return getMinimumPushVector(pushVectors, polygon1.getCenter(), polygon2.getCenter());
        }
        return Vector.ZERO_VECTOR;
    }

    private static List<Vector> applySAT(Vector[] axes, Point[] firstEntityVertices, Point[] secondEntityVertices){
        Vector pushVector;
        List<Vector> pushVectors = new ArrayList<>();

        for (Vector axis : axes){
            pushVector = isSeparatingAxis(axis, firstEntityVertices, secondEntityVertices);

            if (pushVector.equals(Vector.ZERO_VECTOR)){
                break;
            } else {
                pushVectors.add(pushVector);
            }
        }

        return pushVectors;
    }

    private static Vector reflectVectorAcrossPushVector(Vector initialVector, Vector pushVector){
        float angle = Vector.calculateAngleBetweenVectors(pushVector, initialVector);
        return initialVector.rotate((float)Math.cos(angle), (float) Math.cos(angle)).sMult(-1f);
    }

    private static Point[] getCircleVerticesForAxis(Vector axis, Point center, float radius){

        Vector radiusVector = axis.sMult(radius);

        return new Point[]{center.add(radiusVector.getRelativeToTailPoint()),
                center.add(radiusVector.sMult(-1f).getRelativeToTailPoint())};
    }

    private static void moveEntityCenter(Collidable entity, Point amountToMove){
        Point newCenter = entity.getCenter().add(amountToMove);
        entity.setCenter(newCenter);
    }

    private void addCheckedPairNames(Collidable entity1, Collidable entity2){
        checkedPairNames.add(entity1.getName() + entity2.getName());
        checkedPairNames.add(entity2.getName() + entity1.getName());
    }
    
    /*private boolean isEntityAbleToBePushed(Collidable entity, Pair<Integer, Integer> binReference, boolean resolveCollision){

        //look at adjacent cells in grid that the entity exists in (if they exist).
        for (int i = binReference.first - 1; i < binReference.first + 2; i++){
            for (int j = binReference.second - 1; j < binReference.second + 2; j++){

                ArrayList<Collidable> bin = broadPhaseGrid.getBin(i, j);
                //only check the grid references that the entity exists in
                if (bin != null && bin.contains(entity)){
                    //check if the entity collides with any statics in this cell
                    boolean collided = isStaticCollision(entity, bin, resolveCollision);
                    if (collided)
                        return false;
                }
            }
        }
        return true;
    }*/

    //checks if the given entity collides with any statics in the given bin.
    //resolveCollision determines if a collision should be resolved if one is detected or not
    private boolean isStaticCollision(Collidable entity, List<Collidable> bin, boolean resolveCollision){

        //TODO change to include non static entities
        for (Collidable entityInBin : bin) {
            if (!entityInBin.canMove()) {
                //check if the two entities collide
                Vector pushVector;

                pushVector = collisionCheckTwoPolygons(entity, entityInBin);

                if (!pushVector.equals(Vector.ZERO_VECTOR)) {
                    if (resolveCollision){
                        //if the collision is meant to be resolved at this stage, resolve it and mark
                        //the entities as checked
                        moveEntityCenter(entity, pushVector.getRelativeToTailPoint());
                        addCheckedPairNames(entity, entityInBin);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static Vector getMinimumPushVector(List<Vector> pushVectors, Point center1, Point center2){
        Vector minPushVector = Vector.ZERO_VECTOR;
        if (pushVectors.size() > 0) {
            minPushVector = pushVectors.get(0);

            for (Vector pushVector : pushVectors) {
                minPushVector = minPushVector.getLength() < pushVector.getLength() ? minPushVector : pushVector;
            }
        }

        if (minPushVector.dot(new Vector(center1, center2)) > 0) {
            minPushVector = minPushVector.sMult(-1f);
        }
        return minPushVector;
    }

    private static Vector[] convertToVectorsFromOrigin(Point[] vertices){
        Vector[] verticesVectors = new Vector[vertices.length];
        for (int i = 0; i < vertices.length; i++){
            verticesVectors[i] = new Vector(vertices[i]);
        }
        return verticesVectors;
    }

    //checks if the max and min points on the given direction axis overlap, returns the overlap
    //this function is the variation for checking TWO polygons
    private static Vector isSeparatingAxis(Vector axis, Point[] firstEntityVertices, Point[] secondEntityVertices){
        Pair<Float, Float> minMaxResult = projectOntoAxis(axis, firstEntityVertices);

        float firstEntityMinLength = minMaxResult.first;
        float firstEntityMaxLength = minMaxResult.second;

        minMaxResult = projectOntoAxis(axis, secondEntityVertices);
        float secondEntityMinLength = minMaxResult.first;
        float secondEntityMaxLength = minMaxResult.second;

        if (firstEntityMaxLength >= secondEntityMinLength && secondEntityMaxLength >= firstEntityMinLength) {
            float pushVectorLength = Math.min((secondEntityMaxLength - firstEntityMinLength), (firstEntityMaxLength - secondEntityMinLength));

            //push a bit more than needed so they dont overlap in future tests to compensate for float precision error
            pushVectorLength += PUSH_VECTOR_ERROR;

            return axis.getUnitVector().sMult(pushVectorLength);
        }
        return Vector.ZERO_VECTOR;
    }
    /*private static Vector isSeparatingAxis(Vector axis, Vector[] firstEntityVertices, Vector[] secondEntityVertices){
        float firstEntityMinLength = Float.POSITIVE_INFINITY;
        float firstEntityMaxLength = Float.NEGATIVE_INFINITY;

        float secondEntityMinLength = Float.POSITIVE_INFINITY;
        float secondEntityMaxLength = Float.NEGATIVE_INFINITY;

        float projection;
        for (Vector vertexVector : firstEntityVertices){
            projection = vertexVector.dot(axis);

            firstEntityMinLength = Math.min(firstEntityMinLength, projection);
            firstEntityMaxLength = Math.max(firstEntityMaxLength, projection);
        }

        for (Vector vertexVector : secondEntityVertices){
            projection = vertexVector.dot(axis);

            secondEntityMinLength = Math.min(secondEntityMinLength, projection);
            secondEntityMaxLength = Math.max(secondEntityMaxLength, projection);
        }

        if (firstEntityMaxLength >= secondEntityMinLength && secondEntityMaxLength >= firstEntityMinLength) {
            float pushVectorLength = Math.min((secondEntityMaxLength - firstEntityMinLength), (firstEntityMaxLength - secondEntityMinLength));

            //push a bit more than needed so they dont overlap in future tests to compensate for float precision error
            pushVectorLength += PUSH_VECTOR_ERROR;

            return axis.getUnitVector().sMult(pushVectorLength);
        }
        return Vector.ZERO_VECTOR;
    }*/

    private static Pair<Float, Float> projectOntoAxis(Vector axis, Point... vertices){
        float minLength = Float.POSITIVE_INFINITY;
        float maxLength = Float.NEGATIVE_INFINITY;

        float projection;

        for (Vector vertexVector : convertToVectorsFromOrigin(vertices)){
            projection = vertexVector.dot(axis);

            minLength = Math.min(minLength, projection);
            maxLength = Math.max(maxLength, projection);
        }

        return new Pair<>(minLength, maxLength);
    }

    private static Pair<Point, Point> getMinMaxPointsForAxis(Vector axis, Point... vertices){
        float minLength = Float.POSITIVE_INFINITY;
        float maxLength = Float.NEGATIVE_INFINITY;

        Point maxVertex = new Point();
        Point minVertex = new Point();

        float projection;

        for (Vector vertexVector : convertToVectorsFromOrigin(vertices)){
            projection = vertexVector.dot(axis);

            if (projection < minLength){
                minVertex = vertexVector.getHead();
                minLength = projection;
            }

            if (projection > maxLength){
                maxVertex = vertexVector.getHead();
                maxLength = projection;
            }
        }

        return new Pair<>(minVertex, maxVertex);
    }

    //Returns the unit normals for the given edges.
    //Rotated anticlockwise, so assumes the edges given cycle clockwise around a shape
    private static Vector[] getEdgeNormals(ArrayList<Vector> edges){
        Vector[] orthogonals = new Vector[edges.size()];

        for (int i = 0; i < edges.size(); i++){
            orthogonals[i] = getEdgeNormal(edges.get(i));
        }
        return orthogonals;
    }

    private static Vector getEdgeNormal(Vector edge){
        return edge.rotateAntiClockwise90().getUnitVector();
    }

    //joins the vertices together to form edge vectors.
    //joins the last and the first vertex to form a closed shape
    //if the shape is a rectangle, only the orthogonal edges are returned
    private static ArrayList<Vector> getEdges(ShapeIdentifier shapeIdentifier, Point[] vertices){
        ArrayList<Vector> edges = new ArrayList<>();

        if (shapeIdentifier != RECTANGLE) {

            for (int i = 0; i < vertices.length - 1; i++) {
                edges.add(new Vector(vertices[i], vertices[i + 1]));
            }
            edges.add(new Vector(vertices[vertices.length - 1], vertices[0]));
        } else {
            edges.add(new Vector(vertices[0], vertices[1]));
            edges.add(new Vector(vertices[1], vertices[2]));
        }

        return edges;
    }
}