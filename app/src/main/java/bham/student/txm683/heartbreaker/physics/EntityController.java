package bham.student.txm683.heartbreaker.physics;

import android.util.Log;
import bham.student.txm683.heartbreaker.Level;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.entities.Bomb;
import bham.student.txm683.heartbreaker.entities.Door;
import bham.student.txm683.heartbreaker.entities.Portal;
import bham.student.txm683.heartbreaker.entities.Projectile;
import bham.student.txm683.heartbreaker.utils.Tile;

import java.util.List;


public class EntityController {
    private LevelState levelState;
    private Level level;

    public EntityController(Level level){
        this.level = level;
        this.levelState = level.getLevelState();
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

        levelState.removeExplosions();

        for (Projectile projectile : levelState.getBullets()){
            projectile.tick(delta);

            if (projectile.outOfLife()){
                levelState.removeBullet(projectile);

                //if it's a bomb, spawn its explosion
                if (projectile instanceof Bomb){
                    levelState.addExplosion(((Bomb) projectile).explode());
                }
            }
        }

        Portal portal = levelState.getPortal();
        if (portal != null){

            List<String> guards = portal.getGuards();
            boolean guardAlive = false;
            for (AIEntity aiEntity : levelState.getAliveAIEntities()){
                if (guards.contains(aiEntity.getName())){
                    guardAlive = true;
                    break;
                }
            }

            if (!guardAlive){
                Log.d("PORTAL", "GUARDS DEAD");
                portal.setActive(true);
            }

            if (portal.isActive() && portal.getPortalType() == Portal.PortalType.EXIT && portal.isPlayerInBounds()){
                Log.d("LOADING", "loading stage: " + portal.getLeadsTo());
                level.getLevelView().loadStage(portal.getLeadsTo());
            }
        }
    }
}