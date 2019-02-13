package bham.student.txm683.heartbreaker.map;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.Chaser;
import bham.student.txm683.heartbreaker.ai.Core;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Player;
import bham.student.txm683.heartbreaker.entities.Wall;
import bham.student.txm683.heartbreaker.entities.entityshapes.Perimeter;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomEdge;
import bham.student.txm683.heartbreaker.map.roomGraph.RoomGraph;
import bham.student.txm683.heartbreaker.pickups.Pickup;
import bham.student.txm683.heartbreaker.pickups.PickupType;
import bham.student.txm683.heartbreaker.utils.Vector;
import bham.student.txm683.heartbreaker.utils.*;

import java.util.*;

public class MapConstructor {
    private static final String TAG = "hb::MapConstructor";
    private Map map;
    private int tileSize;
    private Point centerOffset;

    private UniqueID uniqueID;

    //spacing between sets of collision points added to wall
    private int gapBetweenPoints;

    private int doorColor = Color.BLUE;
    private int wallColor = Color.rgb(32,32,32);
    private int chaserColor = Color.rgb(255, 153, 51);
    private int upperPlayerColor = Color.WHITE;
    private int lowerPlayerColor = Color.MAGENTA;

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

        ArrayList<Perimeter> perimeters = new ArrayList<>();

        perimeters.add(new Perimeter(new Point[]{
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

        ArrayList<RoomGrid> roomGrids = new ArrayList<>();
        for (Perimeter perimeter : perimeters){
            roomGrids.add(new RoomGrid(populateGrid(perimeter), tileSize));
        }

        HashMap<Integer, Room> rooms = new HashMap<>();
        int count = 0;
        for (Perimeter perimeter : perimeters){
            perimeter.convertToGlobal(tileSize);
            rooms.put(count, new Room(count, perimeter));
            count++;
        }

        RoomGraph roomGraph = new RoomGraph();

        //initiate room graph and grids
        for (Room room : rooms.values()){
            roomGraph.addNode(room);
        }

        ArrayList<Door> doors = new ArrayList<>();

        //public Door(int doorID, Point center, int width, int height, float fieldWidth,
        //              boolean primaryLocked, boolean secondaryLocked, boolean vertical, int doorColor){

        doors.add(new Door(0, new Point(4*tileSize,3*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, true, true, doorColor));
        doors.add(new Door(1, new Point(7*tileSize,3*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, false, true, doorColor));
        doors.add(new Door(2, new Point(4*tileSize,8*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, false, true, doorColor));

        /*doors.add(new Door(0, new Point(4*tileSize,3*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, Color.BLUE));
        doors.add(new Door(1, new Point(7*tileSize,3*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, Color.BLUE));
        doors.add(new Door(2, new Point(4*tileSize,8*tileSize).add(centerOffset),
                tileSize/2, tileSize, false, Color.BLUE));*/

        RoomEdge door0 = roomGraph.addConnection(0,1, doors.get(0));
        RoomEdge door1 = roomGraph.addConnection(1,2, doors.get(1));
        RoomEdge door2 = roomGraph.addConnection(1,3,  doors.get(2));

        //add doors to roomgrid's tilesets
        roomGrids.get(0).addToTileSet(doors.get(0).getCenter());
        roomGrids.get(1).addToTileSet(doors.get(0).getCenter());

        roomGrids.get(1).addToTileSet(doors.get(1).getCenter());
        roomGrids.get(2).addToTileSet(doors.get(1).getCenter());

        roomGrids.get(1).addToTileSet(doors.get(2).getCenter());
        roomGrids.get(3).addToTileSet(doors.get(2).getCenter());

        door0.getDoor().setTileBackground(tileSize, generateDoorTileColor(door0));
        door1.getDoor().setTileBackground(tileSize, generateDoorTileColor(door1));
        door2.getDoor().setTileBackground(tileSize, generateDoorTileColor(door2));

        List<List<Point>> boundaries = new ArrayList<>();

        boolean add;
        for (Room room : rooms.values()){
            //generate the boundaries for each room
            List<List<Point>> newBoundaries = generateBoundaries(room.getPerimeter());


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

        //generate wall objects
        ArrayList<Wall> walls = new ArrayList<>();
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

                            //Log.d(TAG+"ADDED POINTS AT END (RIGHTMOST)", lastPoint.add(new Point(tileSize, 0)).toString() + ", " + lastPoint.add(new Point(tileSize, tileSize)));
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
                            //Log.d(TAG+"ADDED POINTS AT END (BOTTOM)", lastPoint.add(new Point(0, tileSize)).toString() + ", " + lastPoint.add(new Point(tileSize, tileSize)));
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
                            topLeft, bottomRight, center, wallColor);
                    walls.add(wall);
                }
            }
        }

        ArrayList<AIEntity> enemies = new ArrayList<>();
        enemies.add(new Chaser("C:" + uniqueID.id(), new Point(10*tileSize, tileSize).add(centerOffset), map.getTileSize()/2, chaserColor, 300f, 100));

        //initialise map with generated contents
        map.setWalls(walls);
        map.setWidthInTiles(12);
        map.setHeightInTiles(13);
        map.setRoomGraph(roomGraph);
        map.setRooms(rooms);
        map.setDoors(doors);
        map.setPlayer(new Player("player", new Point(tileSize, tileSize).add(centerOffset), tileSize/2,
                tileSize*2, upperPlayerColor, lowerPlayerColor, 100));

        map.setEnemies(enemies);

        List<Pickup> pickups = new ArrayList<>();

        pickups.add(new Pickup(uniqueID.id()+"", PickupType.HEALTH, new Point(400, 2000).add(centerOffset), tileSize/4));
        pickups.add(new Pickup(uniqueID.id()+"", PickupType.BOMB, new Point(400, 2200).add(centerOffset), tileSize/4));
        map.setPickups(pickups);

        map.setCore(new Core("core", new Point(1800, 600).add(centerOffset), tileSize, 0));
    }

    private int generateDoorTileColor(RoomEdge edge){

        int firstColor = edge.getConnectedRoomNodes().first.getRoom().getPerimeter().getDefaultColor();
        int secondColor = edge.getConnectedRoomNodes().second.getRoom().getPerimeter().getDefaultColor();

        int red = (Color.red(firstColor)+Color.red(secondColor))/2;
        int green = (Color.green(firstColor)+Color.green(secondColor))/2;
        int blue = (Color.blue(firstColor)+Color.blue(secondColor))/2;

        return Color.rgb(red, green, blue);
    }

    private List<List<Point>> generateBoundaries(Perimeter perimeter){
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

    private static ArrayList<Tile> populateGrid(Perimeter perimeter) {
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
    }

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
