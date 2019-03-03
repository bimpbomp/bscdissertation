package bham.student.txm683.heartbreaker.physics;

import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.entities.Bomb;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.Tile;


public class EntityController {
    private LevelState levelState;

    public EntityController(LevelState levelState){
        this.levelState = levelState;
    }

    public void update(float delta){

        for (Door door : levelState.getMap().getDoors().values()){
            Tile sideSets = door.getSideSets();

            if (door.isLocked()){
                levelState.getMap().getMeshGraph().removeConnection(sideSets.getX(), door.getDoorSet());
                levelState.getMap().getMeshGraph().removeConnection(sideSets.getY(), door.getDoorSet());
            } else {
                levelState.getMap().getMeshGraph().addConnection(sideSets.getX(), door.getDoorSet());
                levelState.getMap().getMeshGraph().addConnection(sideSets.getY(), door.getDoorSet());
            }
        }

        levelState.getPlayer().tick(delta);

        levelState.getCore().tick(delta);

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