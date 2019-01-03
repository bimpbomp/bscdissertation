package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.LevelState;

public class PhysicsController {

    private LevelState levelState;

    public PhysicsController(LevelState levelState){
        this.levelState = levelState;
    }

    public void update(float delta){
        levelState.getPlayer().move(delta);
    }
}