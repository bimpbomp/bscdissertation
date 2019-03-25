package bham.student.txm683.heartbreaker.ai;

import bham.student.txm683.heartbreaker.LevelState;

import java.util.List;


public class AIManager {
    private LevelState levelState;
    private List<Overlord> overlords;


    public AIManager(LevelState levelState, List<Overlord> overlords) {
        this.levelState = levelState;
        this.overlords = overlords;

        for (Overlord overlord : overlords){
            overlord.setLevelState(levelState);
        }
    }

    public void update(float secondsSinceLastGameTick){
        for (Overlord overlord : overlords){
            overlord.update(secondsSinceLastGameTick);
        }
    }

    public boolean overlordsDefeated(){
        for (Overlord overlord : overlords){
            if (!overlord.isDefeated()){
                return false;
            }
        }
        return true;
    }

    public void removeAI(AIEntity ai){
        for (Overlord overlord : overlords){
            overlord.removeAI(ai);
        }
    }
}
