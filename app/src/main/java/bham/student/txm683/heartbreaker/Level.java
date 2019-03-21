package bham.student.txm683.heartbreaker;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.AIManager;
import bham.student.txm683.heartbreaker.ai.behaviours.BKeyType;
import bham.student.txm683.heartbreaker.entities.Entity;
import bham.student.txm683.heartbreaker.input.InputManager;
import bham.student.txm683.heartbreaker.input.RectButtonBuilder;
import bham.student.txm683.heartbreaker.map.Map;
import bham.student.txm683.heartbreaker.map.MapLoader;
import bham.student.txm683.heartbreaker.map.MeshPolygon;
import bham.student.txm683.heartbreaker.messaging.MessageBus;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.physics.CollisionManager;
import bham.student.txm683.heartbreaker.physics.EntityController;
import bham.student.txm683.heartbreaker.rendering.LevelView;
import bham.student.txm683.heartbreaker.rendering.popups.Popup;
import bham.student.txm683.heartbreaker.rendering.popups.TextBoxBuilder;
import bham.student.txm683.heartbreaker.utils.BenchMarker;
import bham.student.txm683.heartbreaker.utils.FPSMonitor;
import bham.student.txm683.heartbreaker.utils.graph.Edge;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Level implements Runnable {

    private LevelView levelView;
    private LevelState levelState;
    private InputManager inputManager;
    private EntityController entityController;
    private CollisionManager collisionManager;

    private MessageBus messageBus;

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

    public Level(LevelView levelView, String mapName){
        super();

        this.mapName = mapName;
        this.stage = "1";

        this.messageBus = new MessageBus();

        this.levelView = levelView;

        gameFPSMonitor = new FPSMonitor();
        renderFPSMonitor = new FPSMonitor();

        removedConnections = new HashSet<>();
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

            this.inputManager = new InputManager(levelState, levelView.getContext(), levelView);

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
            collisionManager = new CollisionManager(levelState);

            levelState.setCollisionManager(collisionManager);
            levelState.setAiManager(new AIManager(levelState, levelState.getAliveAIEntities()));
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

        //levelState.setPaused(false);
        while (running){

            loops = 0;

            if (!levelState.isPaused()) {

                currentGameTick = System.currentTimeMillis();

                while (currentGameTick > nextScheduledGameTick && loops < maxSkipTick) {

                    levelState.getPlayer().setRequestedMovementVector(inputManager.getThumbstick().getMovementVector());
                    levelState.getPlayer().setRotationVector(inputManager.getRotationThumbstick().getMovementVector());

                    benchMarker.begin();
                    levelState.clearBlockedPolygons();
                    addBlockedBackToGraph();

                    mapToMesh(levelState.getPlayer());
                    for (AIEntity aiEntity : levelState.getAliveAIEntities()){
                        mapToMesh(aiEntity);
                    }

                    //remove blocked edges from meshgraph
                    removeBlockedFromGraph();
                    benchMarker.output("meshCalc");

                    entityController.update(gameTickTimeStepInMillis / 1000f);

                    benchMarker.begin();
                    levelState.getAiManager().update(gameTickTimeStepInMillis / 1000f);
                    benchMarker.output("AI");

                    benchMarker.begin();
                    collisionManager.checkCollisions();
                    benchMarker.output("collisions");

                    gameFPSMonitor.updateFPS();

                    nextScheduledGameTick += gameTickTimeStepInMillis;
                    loops++;

                    levelState.setReadyToRender(true);

                    //check level end conditions
                    if (levelState.getPlayer().getHealth() < 1){
                        levelState.getLevelEnder().setStatus(LevelEndStatus.PLAYER_DIED);
                        inputManager.setActivePopup(diedPopup);

                    } else if (levelState.getCore() != null && levelState.getCore().getHealth() < 1) {
                        levelState.getLevelEnder().setStatus(LevelEndStatus.CORE_DESTROYED);
                        inputManager.setActivePopup(completePopup);
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

    private void mapToMesh(Entity entity){


        List<Integer> ids = mapCollidableToMesh(entity);


        if (ids.size() == 0)
            return;

        for (int id : ids) {
            MeshPolygon meshPolygon = levelState.getRootMeshPolygons().get(id);

            Log.d("RATIOO", entity.getName());
            float ratio = meshPolygon.compareDimensions(entity.getBoundingBox());

            if (ratio > 0.7f) {
                levelState.addBlockedPolygon(id);
            }

            if (entity instanceof AIEntity) {
                ((AIEntity) entity).getContext().addPair(BKeyType.CURRENT_MESH, meshPolygon);
                break;
            }
        }
    }

    private List<Integer> mapCollidableToMesh(Collidable collidable){
        List<Integer> ids = new ArrayList<>();

        for (MeshPolygon meshPolygon : levelState.getRootMeshPolygons().values()){

            if (meshPolygon.getBoundingBox().intersecting(collidable.getBoundingBox())){
                ids.add(meshPolygon.getId());
            }
        }

        return ids;
    }

    public void shutDown(){
        //levelState.getAiManager().shutDown();
    }

    public void setRunning(boolean isRunning){
        this.running = isRunning;
    }

    public void setInputManager(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setLevelState(LevelState levelState) {
        this.levelState = levelState;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }


}