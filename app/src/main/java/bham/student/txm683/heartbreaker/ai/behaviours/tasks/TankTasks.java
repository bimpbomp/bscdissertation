package bham.student.txm683.heartbreaker.ai.behaviours.tasks;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import bham.student.txm683.heartbreaker.LevelState;
import bham.student.txm683.heartbreaker.ai.AIEntity;
import bham.student.txm683.heartbreaker.ai.behaviours.BContext;
import bham.student.txm683.heartbreaker.ai.behaviours.BNode;
import bham.student.txm683.heartbreaker.ai.behaviours.Status;
import bham.student.txm683.heartbreaker.entities.TankBody;
import bham.student.txm683.heartbreaker.physics.fields.Explosion;
import bham.student.txm683.heartbreaker.rendering.ColorScheme;
import bham.student.txm683.heartbreaker.utils.Vector;

import java.util.PriorityQueue;

import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.CONTROLLED_ENTITY;
import static bham.student.txm683.heartbreaker.ai.behaviours.BKeyType.LEVEL_STATE;
import static bham.student.txm683.heartbreaker.ai.behaviours.Status.*;

public class TankTasks {
    public static BNode applyMovementForces(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                Log.d("TESTING", "reached applyMovementForces");
                if (context.containsCompulsory(CONTROLLED_ENTITY)){
                    Log.d("TESTING", "reached applyMovementForces");
                    AIEntity controlled = (AIEntity) context.getCompulsory(CONTROLLED_ENTITY);

                    Vector force = Vector.ZERO_VECTOR;

                    PriorityQueue<Pair<Integer, Vector>> forces = new PriorityQueue<>(10, (a, b) -> {
                        if (a.first < b.first)
                            return -1;
                        else if (a.second.equals(b.second))
                            return 0;
                        return 1; });

                    boolean evading = false;
                    if (context.containsVariables("evasion_steering")){
                        forces.add(new Pair<>(1, (Vector) context.getVariable("evasion_steering")));
                        force = force.vAdd((Vector) context.getVariable("evasion_steering"));
                        evading = true;
                    }

                    if (context.containsVariables("path_steering")) {
                        forces.add(new Pair<>(2, (Vector) context.getVariable("path_steering")));

                        if (force.equals(Vector.ZERO_VECTOR)) {

                            Vector v = (Vector) context.getVariable("path_steering");

                            if (evading)
                                v = v.sMult(0.5f);

                            force = force.vAdd(v);
                        }
                    }

                    if (context.containsVariables("arrival_steering")) {
                        forces.add(new Pair<>(3, (Vector) context.getVariable("arrival_steering")));
                    } else if (context.containsVariables("seek_steering")) {
                        forces.add(new Pair<>(3, (Vector) context.getVariable("seek_steering")));
                    }



                    int numForces = forces.size();

                    if (numForces > 1) {
                        while (!forces.isEmpty()) {

                            Pair<Integer, Vector> pair = forces.poll();
                            Vector currentForce = pair.second;

                            force = force.vAdd(currentForce.sMult(0.75f));
                        }
                    } else if (numForces == 1){
                        force = force.vAdd(forces.poll().second);
                    }

                    controlled.addForce(force);

                    Log.d("HEALER", "applying force to " + controlled.getName());

                    context.removeVariables("evasion_steering", "path_steering", "arrival_steering", "seek_steering");

                    setStatus(SUCCESS);
                    return SUCCESS;
                }
                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode brokenDown(){
        return new BNode() {
            @Override
            public Status process(BContext context) {

                if (context.containsCompulsory(CONTROLLED_ENTITY)){
                    AIEntity controlled = (AIEntity) context.getCompulsory(CONTROLLED_ENTITY);

                    controlled.setBrokenDown(true);

                    TankBody tankBody = (TankBody) controlled.getShape();

                    tankBody.setBodyColor(ColorScheme.manipulateColor(tankBody.getDefaultColor(), 0.7f));
                    tankBody.setTurretColor(ColorScheme.manipulateColor(tankBody.getDefaultColor(), 1.5f));

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode flashRed(int duration){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsCompulsory(CONTROLLED_ENTITY)){

                    AIEntity controlled = ((AIEntity)context.getCompulsory(CONTROLLED_ENTITY));

                    if (!context.containsVariables("flash_remaining"))
                        context.addVariable("flash_remaining", duration);

                    context.addVariable("fuse_started", true);

                    int flashRemaining = (int) context.getVariable("flash_remaining");

                    Log.d("FLASH", flashRemaining + "");

                    if (flashRemaining > 0){
                        flashRemaining--;

                        if (flashRemaining % 2 == 0){
                            controlled.setColor(Color.RED);
                        } else {
                            controlled.setColor(Color.BLACK);
                        }

                    } else {
                        setStatus(SUCCESS);
                        return SUCCESS;
                    }

                    context.addVariable("flash_remaining", flashRemaining);

                    setStatus(RUNNING);
                    return RUNNING;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }

    public static BNode detonate(){
        return new BNode() {
            @Override
            public Status process(BContext context) {
                if (context.containsCompulsory(CONTROLLED_ENTITY)){

                    AIEntity controlled = ((AIEntity) context.getCompulsory(CONTROLLED_ENTITY));

                    controlled.setHealth(0);
                    LevelState levelState = (LevelState ) context.getCompulsory(LEVEL_STATE);

                    levelState.addExplosion(new Explosion("death" + controlled.getName(), controlled.getName(),
                            controlled.getCenter(), 150f, 60, Color.RED));

                    setStatus(SUCCESS);
                    return SUCCESS;
                }

                setStatus(FAILURE);
                return FAILURE;
            }
        };
    }


}
