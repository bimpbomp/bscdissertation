package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.MoveableEntity;
import bham.student.txm683.heartbreaker.utils.Point;
import bham.student.txm683.heartbreaker.utils.Tile;

public class AIManager {

    private MoveableEntity controlledEntity;
    private LevelState levelState;

    public AIManager(MoveableEntity controlledEntity, LevelState levelState){
        this.controlledEntity = controlledEntity;
        this.levelState = levelState;
    }

    public void update(float secondsSinceLastGameTick){

    }

    private void applyAStar(Point start, Point target){
        Tile startTile = levelState.getMap().mapGlobalPointToTile(start);
        Tile targetTile = levelState.getMap().mapGlobalPointToTile(target);


    }
}
