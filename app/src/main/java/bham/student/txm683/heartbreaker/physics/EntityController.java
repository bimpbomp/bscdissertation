package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Bomb;
import bham.student.txm683.heartbreaker.entities.Projectile;


public class EntityController {
    private LevelState levelState;

    public EntityController(LevelState levelState){
        this.levelState = levelState;
    }

    public void update(float delta){
        levelState.getPlayer().tick(delta);

        levelState.removeExplosions();

        for (Projectile projectile : levelState.getBullets()){
            projectile.tick(delta);

            if (projectile.outOfLife()){
                levelState.removeBullet(projectile);

                //if it's a bomb, spawn it's explosion
                if (projectile instanceof Bomb){
                    levelState.addExplosion(((Bomb) projectile).explode());
                }
            }
        }
    }
}