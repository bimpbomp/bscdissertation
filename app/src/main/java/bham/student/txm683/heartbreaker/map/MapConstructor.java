package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import bham.student.txm683.heartbreaker.TileSet;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.ai.Drone;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomEdge;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;
import bham.student.txm683.heartbreaker.utils.UniqueID;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.*;

public class MapConstructor {
    private static final String TAG = "hb::MapConstructor";
    private Map map;
    private int tileSize;
    private Point centerOffset;

    private UniqueID uniqueID;

    //spacing between sets of collision points added to wall
    private int gapBetweenPoints;

    public MapConstructor(){
        this.uniqueID = new UniqueID();
    }

    public Map loadMap(String name, int tileSize){
        map = new Map(name, tileSize);
        this.tileSize = tileSize;

        this.gapBetweenPoints = tileSize;

        this.centerOffset = new Point(tileSize/2f, tileSize/2f);

        loadTestMap();

        return map;
    }

    @SuppressLint("UseSparseArrays")
    private void loadTestMap(){

        /*
         * Specify Map Layout
         */

        int mapWidth = 10;
        int mapHeight = 10;

        ArrayList<Perimeter> perimeters = new ArrayList<>();
        ArrayList<Door> doors = new ArrayList<>();
        List<Pickup> pickups = new ArrayList<>();
        ArrayList<AIEntity> enemies = new ArrayList<>();
        List<Wall> obstacles = new ArrayList<>();

        Player player;
        Core core;

        perimeters.add(new Perimeter(new Point[]{
                new Point(0,0),
                new Point(10,0),
                new Point(10,10),
                new Point(0,10)
        }, Color.GREEN));

        player = new Player("player", new Point(2*tileSize,2*tileSize), tileSize/2, tileSize*3,
                ColorScheme.UPPER_PLAYER_COLOR, ColorScheme.LOWER_PLAYER_COLOR, 100);

        enemies.add(new Drone("D:"+uniqueID.id(), new Point(5*tileSize, 2*tileSize).add(centerOffset), tileSize/2, ColorScheme.CHASER_COLOR, tileSize*1.5f, 100));

        core = new Core("core", new Point(8*tileSize,8*tileSize).add(centerOffset), tileSize/2);

        obstacles.add(new Wall("W"+uniqueID.id(), new Point(6*tileSize,6*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));
        obstacles.add(new Wall("W"+uniqueID.id(), new Point(6*tileSize,5*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));
        obstacles.add(new Wall("W"+uniqueID.id(), new Point(5*tileSize,5*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));
        obstacles.add(new Wall("W"+uniqueID.id(), new Point(4*tileSize,5*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));
        obstacles.add(new Wall("W"+uniqueID.id(), new Point(3*tileSize,5*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));
        obstacles.add(new Wall("W"+uniqueID.id(), new Point(4*tileSize,4*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));
        obstacles.add(new Wall("W"+uniqueID.id(), new Point(4*tileSize,3*tileSize).add(centerOffset), tileSize, ColorScheme.WALL_COLOR));


        /*
         * Generate Map
         */

        //hardcoded mesh tileList for now till algorithm implemented
        List<List<Integer>> tileList = new ArrayList<>();

        tileList.add(Arrays.asList(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,-1,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,-1,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,-1,-1,-1,-1,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,-1,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,0,0,0,0,0,0,0,0,-1));
        tileList.add(Arrays.asList(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1));

        MeshConstructor meshConstructor = new MeshConstructor();
        meshConstructor.constructMesh(tileList);

        Log.d("hb::Mesh", meshConstructor.tileListToString());

        map.setMeshGraph(meshConstructor.getMeshGraph());
        map.setNSetMap(meshConstructor.getExistingSets());

        //convert perimeter coordinates to global with tileSize and add them to a room
        HashMap<Integer, Room> rooms = new HashMap<>();
        int count = 0;
        for (Perimeter perimeter : perimeters){
            perimeter.convertToGlobal(tileSize);
            rooms.put(count, new Room(count, perimeter));
            count++;
        }


        //GenerateBoundaryWalls
        List<Wall> walls = generateWallsForRooms(rooms, doors);
        walls.addAll(obstacles);

        //add all statics to the tileset
        TileSet tileSet = new TileSet(tileSize);

        for (Wall wall : walls){
            tileSet.addPermanentToGrid(wall);
        }

        for (Door door : doors){
            tileSet.addPermanentToGrid(door);
        }

        //init map object
        map.setTileSet(tileSet);
        map.setWalls(walls);
        map.setWidthInTiles(mapWidth);
        map.setHeightInTiles(mapHeight);

        map.setRooms(rooms);
        map.setDoors(doors);
        map.setPlayer(player);

        map.setEnemies(enemies);
        map.setPickups(pickups);
        map.setCore(core);
    }

    private List<Wall> generateWallsForRooms(HashMap<Integer, Room> rooms, List<Door> doors){
        List<List<Point>> boundaries = new ArrayList<>();

        boolean add;
        for (Room room : rooms.values()){
            //generate the boundaries for each room
            List<List<Point>> newBoundaries = generateBoundariesForRoom(room.getPerimeter());


            //iterates through the new boundaries and only adds points to boundaries that
            //arent already in it
            for (List<Point> newBoundary : newBoundaries){
                List<Point> newPointsInBoundaryToAdd = new ArrayList<>();

                for (Point newBoundaryPoint : newBoundary){
                    add = true;

                    for (List<Point> existingBoundary : boundaries){
                        if (existingBoundary.contains(newBoundaryPoint))
                            add = false;
                    }

                    //if this tile isn't in any existing boundaries
                    if (add)
                        newPointsInBoundaryToAdd.add(newBoundaryPoint);
                }

                //add all points in the new boundary that aren't already in a boundary, to boundaries
                if (newPointsInBoundaryToAdd.size() > 0)
                    boundaries.add(newPointsInBoundaryToAdd);
            }
        }

        //remove any boundary walls on same tile as doors
        //split that wall into two separate walls
        int doorPositionIndex;
        for (Door door : doors){
            boolean removedTile = true;

            while (removedTile) {

                ArrayList<Integer> removedTileIndexes = new ArrayList<>();
                ArrayList<List<Point>> sublistsToAdd = new ArrayList<>();
                removedTile = false;

                //for each boundary in the current room
                for (List<Point> wallPoints : boundaries) {
                    doorPositionIndex = wallPoints.indexOf(door.getCenter().add(centerOffset.sMult(-1)));
                    if (doorPositionIndex >= 0) {

                        if (doorPositionIndex == 0) {
                            //door is at start of wall, and so there will only be one new wall
                            sublistsToAdd.add(wallPoints.subList(doorPositionIndex + 1, wallPoints.size()));

                        } else if (doorPositionIndex == wallPoints.size() - 1) {
                            //door is at end of wall, and so there will only be one new wall
                            sublistsToAdd.add(wallPoints.subList(0, doorPositionIndex));

                        } else {
                            //get wall on one side of door, wall on other
                            //add each new wall to the list, remove the old wall
                            sublistsToAdd.add(wallPoints.subList(0, doorPositionIndex));
                            sublistsToAdd.add(wallPoints.subList(doorPositionIndex + 1, wallPoints.size()));
                        }

                        removedTileIndexes.add(boundaries.indexOf(wallPoints));
                        removedTile = true;
                    }
                }

                //remove any tiles that lie on the same space as a door
                if (removedTile) {
                    for (int index : removedTileIndexes) {
                        boundaries.remove(index);
                    }

                    boundaries.addAll(sublistsToAdd);
                }
            }
        }
        return generateWalls(boundaries);
    }

    private List<Wall> generateWalls(List<List<Point>> boundaries){
        //generate wall objects
        List<Wall> walls = new ArrayList<>();
        //at this point, each list in boundaries should be a continuous vertical/horizontal wall
        for (List<Point> boundary : boundaries){
            //stores the unique points as an outline for the wall in collisions
            List<Point> collisionPoints = new ArrayList<>();
            //stores the outline for rendering (topleft, bottom right points)
            List<Point> renderingPoints = new ArrayList<>();

            StringBuilder stringBuilder = new StringBuilder();
            for (Point point : boundary) {
                stringBuilder.append(point.toString());
                stringBuilder.append(" - ");
            }
            stringBuilder.append("END\n");
            //Log.d(TAG + "BOUNDARY: ", stringBuilder.toString());

            //bounds of the wall (used for rendering)
            if (boundary.size() > 1){
                //more than one block in wall

                Point firstPoint = boundary.get(0);
                Point lastPoint = boundary.get(boundary.size()-1);

                Point directionOfWall = new Vector(firstPoint, lastPoint).getUnitVector().getRelativeToTailPoint();
                int directionModifier;

                boolean addPointsAtEnd;
                float length;
                int numberOfEntitiesInWall;

                LinkedList<Point> pointQueue = new LinkedList<>();
                Stack<Point> pointStack = new Stack<>();

                if (Math.abs(lastPoint.getX() - firstPoint.getX()) > 0.001){
                    //horizontal wall, as no change in y
                    //Log.d(TAG+"DIRECTION", "HORIZONTALWALL");

                    if (directionOfWall.getX() > 0){
                        //first point is at left of wall
                        pointQueue.add(firstPoint);
                        pointStack.add(firstPoint.add(new Point(0, tileSize)));

                        directionModifier = 1;
                    } else {
                        //first point is at right of wall
                        pointStack.add(firstPoint.add(new Point(tileSize, 0)));
                        pointQueue.add(firstPoint.add(new Point(tileSize, tileSize)));
                        directionModifier = -1;
                    }

                    length = Math.abs(firstPoint.getX()-lastPoint.getX());

                    numberOfEntitiesInWall = (int) length / gapBetweenPoints;

                    addPointsAtEnd = (length % gapBetweenPoints != 0 || numberOfEntitiesInWall < 1);

                    Point lastTopLeftPoint = firstPoint;

                    while (numberOfEntitiesInWall > 0){
                        lastTopLeftPoint = lastTopLeftPoint.add(new Point(gapBetweenPoints * directionModifier, 0));

                        if (directionModifier > 0){
                            pointQueue.add(lastTopLeftPoint);

                            //gets point on other side of wall at same x value
                            pointStack.add(lastTopLeftPoint.add(new Point(0, tileSize)));
                        } else {
                            pointStack.add(lastTopLeftPoint);

                            //gets point on other side of wall at same x value
                            pointQueue.add(lastTopLeftPoint.add(new Point(0, tileSize)));
                        }

                        //Log.d(TAG+"ADDED POINTS", lastTopLeftPoint.toString() + ", " + lastTopLeftPoint.add(new Point(0, tileSize)));

                        numberOfEntitiesInWall--;
                    }

                    if (addPointsAtEnd){
                        if (directionModifier > 0) {
                            //if wall grows right, add the rightmost two vertices
                            pointQueue.add(lastPoint.add(new Point(tileSize, 0)));
                            pointStack.add(lastPoint.add(new Point(tileSize, tileSize)));

                            //Log.d(TAG+"ADDED POINTS AT END (RIGHTMOST)", lastPoint.add(new Point(tileSize, 0)).toString()
                            // + ", " + lastPoint.add(new Point(tileSize, tileSize)));
                        } else {
                            //if the wall grows left, add the leftmost two vertices
                            pointStack.add(lastPoint);
                            pointQueue.add(lastPoint.add(new Point(0, tileSize)));

                            //Log.d(TAG+"ADDED POINTS AT END (LEFTMOST)", lastPoint.toString() + ", " + lastPoint.add(new Point(0, tileSize)));
                        }
                    }

                    if (directionModifier > 0){
                        /*
                        Since the wall is growing to the right, the last point describes the
                        start of the last tile in the wall, so another tile needs to be added on
                         */
                        pointQueue.add(lastPoint.add(new Point(tileSize, 0)));
                        pointStack.push(lastPoint.add(new Point(tileSize, tileSize)));

                        renderingPoints.add(pointQueue.peek());
                        renderingPoints.add(pointStack.peek());

                        collisionPoints.addAll(pointQueue);

                        while(!pointStack.isEmpty()){
                            collisionPoints.add(pointStack.pop());
                        }
                    } else {

                        renderingPoints.add(pointStack.peek());
                        renderingPoints.add(pointQueue.peek());

                        while(!pointStack.isEmpty()){
                            collisionPoints.add(pointStack.pop());
                        }
                        collisionPoints.addAll(pointQueue);
                    }

                } else {
                    //vertical wall, as no change in x
                    //Log.d(TAG+"DIRECTION", "VERTICALWALL");

                    if (directionOfWall.getY() > 0){

                        //first point is at top of wall
                        pointQueue.add(firstPoint);
                        pointQueue.add(firstPoint.add(new Point(tileSize, 0)));

                        directionModifier = 1;
                    } else {
                        //first point is at bottom of wall
                        pointQueue.add(firstPoint.add(new Point(0, tileSize)));
                        pointStack.add(firstPoint.add(new Point(tileSize, tileSize)));

                        directionModifier = -1;
                    }

                    length = Math.abs(firstPoint.getY()-lastPoint.getY());

                    numberOfEntitiesInWall = (int) length / gapBetweenPoints;

                    addPointsAtEnd = (length % gapBetweenPoints != 0 || numberOfEntitiesInWall < 1);

                    Point lastTopLeftPoint = firstPoint;

                    while (numberOfEntitiesInWall > 0){
                        lastTopLeftPoint = lastTopLeftPoint.add(new Point(0, gapBetweenPoints * directionModifier));

                        if (directionModifier > 0){
                            pointStack.add(lastTopLeftPoint);

                            //gets point on other side of wall at same y value
                            pointQueue.add(lastTopLeftPoint.add(new Point(tileSize, 0)));
                        } else {
                            pointQueue.add(lastTopLeftPoint);

                            //gets point on other side of wall at same y value
                            pointStack.add(lastTopLeftPoint.add(new Point(tileSize, 0)));
                        }

                        //Log.d(TAG+"ADDED POINTS", lastTopLeftPoint.toString() + ", " + lastTopLeftPoint.add(new Point(tileSize, 0)));

                        numberOfEntitiesInWall--;
                    }

                    if (addPointsAtEnd){
                        if (directionModifier > 0) {
                            //if wall grows down, add the bottom two vertices
                            pointStack.add(lastPoint.add(new Point(0, tileSize)));
                            pointQueue.add(lastPoint.add(new Point(tileSize, tileSize)));
                            //Log.d(TAG+"ADDED POINTS AT END (BOTTOM)", lastPoint.add(new Point(0, tileSize)).toString()
                            // + ", " + lastPoint.add(new Point(tileSize, tileSize)));
                        } else {
                            //if the wall grows up, add the top two vertices
                            pointQueue.add(lastPoint);
                            pointStack.add(lastPoint.add(new Point(tileSize, 0)));

                            //Log.d(TAG+"ADDED POINTS AT END (TOP)", lastPoint.toString() + ", " + lastPoint.add(new Point(tileSize, 0)));
                        }
                    }

                    if (directionModifier > 0){

                        /*
                        Since the wall is growing to down, the last point describes the
                        start of the last tile in the wall, so another tile needs to be added on
                         */
                        pointStack.push(lastPoint.add(new Point(0, tileSize)));
                        pointQueue.add(lastPoint.add(new Point(tileSize, tileSize)));

                        renderingPoints.add(pointQueue.peek());
                        renderingPoints.add(pointQueue.peekLast());

                        collisionPoints.addAll(pointQueue);

                        while(!pointStack.isEmpty()){
                            collisionPoints.add(pointStack.pop());
                        }
                    } else {

                        renderingPoints.add(pointQueue.peekLast());
                        renderingPoints.add(pointStack.firstElement());

                        collisionPoints.add(pointQueue.pollLast());

                        while(!pointStack.isEmpty()){
                            collisionPoints.add(pointStack.pop());
                        }

                        collisionPoints.addAll(pointQueue);
                    }
                }
            } else if (boundary.size() == 1){
                //wall is one block
                //Log.d(TAG+"WALL ONE BLOCK", boundary.get(0).toString());
                Point wallPoint = boundary.get(0);
                collisionPoints.add(wallPoint);
                collisionPoints.add(wallPoint.add(new Point(tileSize, 0)));
                collisionPoints.add(wallPoint.add(new Point(tileSize, tileSize)));
                collisionPoints.add(wallPoint.add(new Point(0, tileSize)));

                renderingPoints.add(wallPoint);
                renderingPoints.add(wallPoint.add(new Point(tileSize, tileSize)));

            }

            if (collisionPoints.size() > 0){

                //public Wall(String name, Point[] collisionVertices, Point topLeft, Point bottomRight,
                //              Point center, int colorValue){
                if (renderingPoints.size() == 2){
                    Point topLeft = renderingPoints.get(0);
                    Point bottomRight = renderingPoints.get(1);
                    Point center = topLeft.add(new Point(bottomRight.getX()-topLeft.getX(),
                            bottomRight.getY()-topLeft.getY()).sMult(0.5f));

                    Wall wall = new Wall("W:"+uniqueID.id(), collisionPoints.toArray(new Point[0]),
                            topLeft, bottomRight, center, ColorScheme.WALL_COLOR);
                    walls.add(wall);
                }
            }
        }
        return walls;
    }

    private int generateDoorTileColor(RoomEdge edge){

        int firstColor = edge.getConnectedRoomNodes().first.getRoom().getPerimeter().getDefaultColor();
        int secondColor = edge.getConnectedRoomNodes().second.getRoom().getPerimeter().getDefaultColor();

        int red = (Color.red(firstColor)+Color.red(secondColor))/2;
        int green = (Color.green(firstColor)+Color.green(secondColor))/2;
        int blue = (Color.blue(firstColor)+Color.blue(secondColor))/2;

        return Color.rgb(red, green, blue);
    }

    private List<List<Point>> generateBoundariesForRoom(Perimeter perimeter){
        List<List<Point>> boundaries = new ArrayList<>();

        Point[] vertices = perimeter.getCollisionVertices();

        List<Point> boundary;
        Point lastBoundaryTile = vertices[0];
        for (int i = 0; i < vertices.length - 1; i++){
            //generates the boundaries between the current two vertices
            boundary = generateBoundary(vertices[i], vertices[i+1], lastBoundaryTile);
            boundaries.add(boundary);

            lastBoundaryTile = boundary.get(boundary.size()-1);
        }
        boundaries.add(generateBoundary(vertices[vertices.length-1], vertices[0], lastBoundaryTile));

        return boundaries;
    }

    private List<Point> generateBoundary(Point startVertex, Point endVertex, Point lastPlacedTile){
        ArrayList<Point> boundary = new ArrayList<>();

        Vector directionToNextVertex = new Vector(map.mapGlobalPointToTilePoint(startVertex), map.mapGlobalPointToTilePoint(endVertex));
        int tilesToAdd = (int) directionToNextVertex.getLength();

        Point currentPoint = lastPlacedTile;

        while (tilesToAdd > 0){
            boundary.add(currentPoint);

            currentPoint = currentPoint.add(directionToNextVertex.getUnitVector().getRelativeToTailPoint().sMult(tileSize));
            tilesToAdd--;
        }

        return boundary;
    }

    /*private static ArrayList<Tile> populateGrid(Perimeter perimeter) {
        ArrayList<Tile> tileSet = new ArrayList<>();

        //visited tiles are added here
        HashSet<Tile> closedSet = new HashSet<>();

        Tile startingPosition = new Tile((int)perimeter.getCollisionVertices()[0].getX(),(int)perimeter.getCollisionVertices()[0].getY());

        //contains tiles that are valid and have been seen (as a neighbour) but not visited.
        //ordered by the integer cost to get to that tile from the starting position
        PriorityQueue<Pair<Tile, Integer>> openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
                return 0;
            return 1; });
        openSet.add(new Pair<>(startingPosition, 0));

        //initialise variables needed in loop
        Pair<Tile, Integer> tileAndCost;
        Tile currentTile;
        int currentCost;
        while (!openSet.isEmpty()) {

            //get the tile with the lowest cost
            tileAndCost = openSet.poll();
            currentTile = tileAndCost.first;
            currentCost = tileAndCost.second;

            //add it to the closed set so it isn't inspected again
            closedSet.add(currentTile);

            ArrayList<Tile> neighbours = TileBFS.getNeighbours(currentTile);

            for (Tile neighbour : neighbours) {

                if (closedSet.contains(neighbour) || !tileIsInPerimeter(perimeter, neighbour)) {
                    //if the tile is out of bounds, or has already been inspected, tick on
                    continue;
                }

                //neighbour is a valid tile but not target, calc it's cost and add to openset
                int neighbourCost = currentCost + 1;
                openSet.add(new Pair<>(neighbour, neighbourCost));

                tileSet.add(neighbour);
            }
        }

        return tileSet;
    }*/

    private static boolean tileIsInPerimeter(Perimeter perimeter, Tile tile){

        if (perimeter.getCollisionVertices().length > 3) {
            Point topLeft = perimeter.getCollisionVertices()[0].add(new Point(1,1));
            Point bottomRight = perimeter.getCollisionVertices()[2].add(new Point(-1,-1));

            return (tile.getX() >= topLeft.getX()) && (tile.getY() >= topLeft.getY())
                    && (tile.getX() < bottomRight.getX()) && (tile.getY() < bottomRight.getY());
        }
        return false;
    }


}

/*perimeters.add(new Perimeter(new Point[]{
        new Point(0,0),
                new Point(5,0),
                new Point(5, 5),
                new Point(0, 5)
    }, Color.rgb(0, 147, 175)));

        perimeters.add(new Perimeter(new Point[]{
        new Point(4,1),
                new Point(8,1),
                new Point(8,11),
                new Point(4,11)
    }, Color.rgb(175, 216, 245)));

        perimeters.add(new Perimeter(new Point[]{
        new Point(7,0),
                new Point(12,0),
                new Point(12,6),
                new Point(7,6)
    }, Color.rgb(31, 117, 254)));

        perimeters.add(new Perimeter(new Point[]{
        new Point(1,7),
                new Point(5,7),
                new Point(5,13),
                new Point(1,13)
    }, Color.rgb(0, 112, 184)));



        doors.add(new Door(0, new Point(4*tileSize,3*tileSize).add(centerOffset),
    tileSize/2, tileSize, false, true, true, ColorScheme.DOOR_COLOR));
        doors.add(new Door(1, new Point(7*tileSize,3*tileSize).add(centerOffset),
    tileSize/2, tileSize, false, false, true, ColorScheme.DOOR_COLOR));
        doors.add(new Door(2, new Point(4*tileSize,8*tileSize).add(centerOffset),
    tileSize/2, tileSize, false, false, true, ColorScheme.DOOR_COLOR));



        pickups.add(new Pickup(uniqueID.id()+"", PickupType.HEALTH, new Point(400, 2000).add(centerOffset), tileSize/4));
        pickups.add(new Pickup(uniqueID.id()+"", PickupType.BOMB, new Point(400, 2200).add(centerOffset), tileSize/4));

    core = new Core("core", new Point(1800, 600).add(centerOffset), tileSize, 0);

    player = new Player("player", new Point(tileSize, tileSize).add(centerOffset), tileSize/2,
    tileSize*2, ColorScheme.UPPER_PLAYER_COLOR, ColorScheme.LOWER_PLAYER_COLOR, 100);


        enemies.add(new Drone("C:" + uniqueID.id(), new Point(10*tileSize, tileSize).add(centerOffset),
                map.getTileSize()/2, ColorScheme.CHASER_COLOR, 300f, 100));*/
