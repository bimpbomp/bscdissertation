package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.framework.ai.behaviours.BKeyType;
import bham.student.txm683.framework.input.RectButtonBuilder;
import bham.student.txm683.framework.map.MeshPolygon;
import bham.student.txm683.framework.physics.Collidable;
import bham.student.txm683.framework.rendering.popups.Popup;
import bham.student.txm683.framework.rendering.popups.TextBoxBuilder;
import bham.student.txm683.framework.utils.BenchMarker;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.FPSMonitor;
import bham.student.txm683.framework.utils.graph.Edge;
import bham.student.txm683.framework.utils.graph.Graph;
import bham.student.txm683.framework.utils.graph.Node;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.intentbundleholders.LevelEndStatus;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.map.MapLoader;
import bham.student.txm683.heartbreaker.physics.EntityController;
import bham.student.txm683.heartbreaker.physics.TankCollisionManager;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import org.json.JSONException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Level implements Runnable {

    private LevelView levelView;
    private LevelState levelState;
    private InputManager inputManager;
    private EntityController entityController;
    private TankCollisionManager collisionManager;

    private boolean running;

    private FPSMonitor gameFPSMonitor;
    private FPSMonitor renderFPSMonitor;

    private int gameTicksPerSecond = 25;
    private int gameTickTimeStepInMillis = 1000 / gameTicksPerSecond;
    private int maxSkipTick = 10;

    private long currentGameTick;
    private long nextScheduledGameTick = System.currentTimeMillis();

    private int loops;

    private String mapName;
    private String stage;

    private Popup diedPopup;
    private Popup completePopup;

    private Set<Edge<Integer>> removedConnections;

    private int countdownToEnd;

    public Level(LevelView levelView, String mapName){
        super();

        this.mapName = mapName;
        this.stage = "1";

        this.levelView = levelView;

        gameFPSMonitor = new FPSMonitor();
        renderFPSMonitor = new FPSMonitor();

        removedConnections = new HashSet<>();

        countdownToEnd = 25 * 2;
    }

    public LevelState getLevelState() {
        return levelState;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    private void load(){
        levelView.drawLoading();

        if (this.levelState == null || !levelState.getMap().getStage().equals(this.stage)) {

            this.levelView.setTileSize(200);

            Log.d("LOADING", "Starting to load...");

            MapLoader mapLoader = new MapLoader(mapName, stage, levelView.getTileSize(), (MainActivity) levelView.getContext());

            Map map;
            try {
                map = mapLoader.loadMap();
            } catch (JSONException e){
                Log.d("EXCEPTION LOADING LEVEL", e.getMessage());
                levelView.returnToMenu();
                return;
            }

            Log.d("LOADING", "map files loaded");

            this.levelState = new LevelState(map);

            this.levelState.setScreenDimensions(levelView.getWidth(), levelView.getHeight());

            this.inputManager = new InputManager(levelState, levelView);

            this.levelView.setInputManager(inputManager);
            this.levelView.setLevelState(levelState);

            levelView.initInGameUI();
            levelView.setDebugInfo(levelState.getDebugInfo());

            RectButtonBuilder[] buttonBuilders = new RectButtonBuilder[]{
                    new RectButtonBuilder("Restart", 40, () -> levelView.restartLevel()),
                    new RectButtonBuilder("Return To Menu", 60, () -> levelView.returnToMenu())
            };

            TextBoxBuilder[] textBuilders = new TextBoxBuilder[]{
                    new TextBoxBuilder("You Died...", 20, 60, Color.RED)
            };

            this.diedPopup = inputManager.createMOSPopup(buttonBuilders, textBuilders);

            buttonBuilders = new RectButtonBuilder[]{
                    new RectButtonBuilder("Return To Menu", 60, () -> levelView.returnToMenu())
            };

            textBuilders = new TextBoxBuilder[]{
                    new TextBoxBuilder("Success!", 20, 60, Color.GREEN)
            };

            this.completePopup = inputManager.createMOSPopup(buttonBuilders, textBuilders);

            //initialise systems
            entityController = new EntityController(this);
            collisionManager = new TankCollisionManager(levelState, 10, 10);

            levelState.setLevelView(levelView);

            levelState.setBenchLog(new BenchLog((MainActivity)levelView.getContext()));
            levelState.setAiManager(new AIManager(levelState, map.getOverlords()));
        }

        this.setRunning(true);
        levelState.setReadyToRender(true);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e){
            //do nothing
        }
    }

    public LevelView getLevelView() {
        return levelView;
    }

    @Override
    public void run(){

        load();

        nextScheduledGameTick = System.currentTimeMillis();

        BenchMarker benchMarker = new BenchMarker();

        while (running){

            loops = 0;

            if (!levelState.isPaused()) {

                currentGameTick = System.currentTimeMillis();

                BenchMarker gameTickTimer = new BenchMarker();
                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    gameTickTimer.begin();

                    levelState.getPlayer().setRequestedMovementVector(inputManager.getThumbstick().getMovementVector());
                    levelState.getPlayer().setRotationVector(inputManager.getRotationThumbstick().getMovementVector());

                    benchMarker.begin();
                    levelState.clearBlockedPolygons();
                    addBlockedBackToGraph();

                    int id = mapCollidableToMesh(levelState.getPlayer());

                    if (id > 0)
                        levelState.getPlayer().setMesh(id);

                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){
                        id = mapCollidableToMesh(aiEntity);
                        aiEntity.setMesh(id);

                        if (id > 0)
                            aiEntity.getContext().addCompulsory(BKeyType.CURRENT_MESH, levelState.getRootMeshPolygons().get(id));
                    }

                    removeBlockedFromGraph();
                    benchMarker.output("meshCalc");

                    entityController.update(gameTickTimeStepInMillis / 1000f);

                    benchMarker.begin();
                    levelState.getAiManager().update(gameTickTimeStepInMillis / 1000f);

                    benchMarker.output("AI");

                    benchMarker.begin();
                    collisionManager.checkCollisions();
                    benchMarker.output("Collisions: ");
                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    levelState.setReadyToRender(true);

                    //check level end conditions
                    if (levelState.getPlayer().getHealth() < 1){
                        levelState.getLevelEnder().setStatus(LevelEndStatus.PLAYER_DIED);
                        inputManager.setActivePopup(diedPopup);

                    } else if (levelState.getAiManager().overlordsDefeated()) {

                        if (countdownToEnd > 0){
                            countdownToEnd--;
                        } else {
                            levelState.getLevelEnder().setStatus(LevelEndStatus.CORE_DESTROYED);
                            inputManager.setActivePopup(completePopup);
                        }
                    }
                }
            } else {
                nextScheduledGameTick = System.currentTimeMillis();
            }

            if (!levelState.isPaused() && levelState.isReadyToRender()) {

                benchMarker.begin();
                float timeSinceLastGameTick = (System.currentTimeMillis() + gameTickTimeStepInMillis - nextScheduledGameTick) / 1000f;
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), timeSinceLastGameTick);
                benchMarker.output("render");

            } else if (levelState.isPaused() && levelState.isReadyToRender()){
                levelView.draw(renderFPSMonitor.getFPSToDisplayAndUpdate(), gameFPSMonitor.getFpsToDisplay(), 0f);
            }

            levelState.getBenchLog().tick();
        }

        Log.d("LOADING", "GAME LOOP ENDING");
    }

    private void removeBlockedFromGraph(){
        Graph<Integer> graph = levelState.getMeshGraph();

        for (int id : levelState.getBlockedPolygons()){
            Node<Integer> currentNode = graph.getNode(id);

            List<Node<Integer>> neighbours = graph.getNode(id).getNeighbours();

            for (Node<Integer> neighbour : neighbours){
                if (neighbour.hasConnectionToNode(currentNode)){
                    removedConnections.add(neighbour.getConnectionTo(currentNode));
                    neighbour.removeConnectionTo(currentNode);
                }
            }
        }
    }

    private void addBlockedBackToGraph(){
        Graph<Integer> graph = levelState.getMeshGraph();

        for (Edge<Integer> edge : removedConnections){
            Pair<Node<Integer>, Node<Integer>> nodes = edge.getConnectedNodes();

            graph.addConnection(nodes.first, nodes.second, edge.getWeight());
        }

        removedConnections.clear();
    }

    private int mapCollidableToMesh(Collidable collidable){
        BoundingBox colBox = collidable.getBoundingBox();

        int biggestOverlapId = -1;
        float biggestOverlap = Float.MIN_VALUE;
        for (MeshPolygon meshPolygon : levelState.getRootMeshPolygons().values()){

            BoundingBox meshBox =  meshPolygon.getBoundingBox();

            if (meshBox.intersecting(colBox)){

                //if collidable takes up over half of the meshpolygon,
                //add it as a blocked polygon
                if (meshBox.overlap(colBox) > 0.5f){
                    levelState.addBlockedPolygon(meshPolygon.getId());
                }

                float overlap = colBox.overlap(meshBox);
                if (overlap > biggestOverlap){
                    biggestOverlap = overlap;
                    biggestOverlapId = meshPolygon.getId();
                }
            }
        }

        return biggestOverlapId;
    }

    public void shutDown(){

    }

    public void setRunning(boolean isRunning){
        this.running = isRunning;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }
}