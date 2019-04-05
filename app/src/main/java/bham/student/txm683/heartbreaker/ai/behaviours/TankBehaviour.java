package bham.student.txm683.heartbreaker.ai.behaviours;

import bham.student.txm683.framework.ai.behaviours.BNode;
import bham.student.txm683.framework.ai.behaviours.composites.Selector;
import bham.student.txm683.framework.ai.behaviours.composites.Sequence;
import bham.student.txm683.framework.ai.behaviours.conditionals.Conditionals;
import bham.student.txm683.framework.ai.behaviours.decorators.Succeeder;
import bham.student.txm683.framework.ai.behaviours.tasks.Tasks;
import bham.student.txm683.heartbreaker.ai.behaviours.tasks.TankTasks;

import static bham.student.txm683.heartbreaker.ai.behaviours.Behaviour.*;

public class TankBehaviour {

    private TankBehaviour(){

    }

    private static BNode brokenDownTree(){
        return Conditionals.healthBelowThreshold(
                new Succeeder(
                        new Sequence(
                                TankTasks.brokenDown(),
                                shootBehaviour(),
                                Tasks.doNothing(50)
                        )
                )
        );
    }

    public static BNode droneTree(){
        return new Selector(
                brokenDownTree(),
                chaseTree()
        );
    }

    public static BNode turretTree(){
        return new Selector(
                brokenDownTree(),
                new Selector(
                        Conditionals.canSeePlayer(
                                delayedShoot(50, 10)
                        ),
                        new Sequence(
                                Tasks.findMeshAdjacentToPlayer(),
                                travelTo(false)
                        )

                )
        );
    }

    public static BNode destructDroneTree(){
        return new Selector (
                Conditionals.healthBelowThreshold(
                        selfDestructTree(100)
                ),
                chaseTree()
        );
    }
}
