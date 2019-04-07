package bham.student.txm683.framework.entities;

import android.graphics.Canvas;
import bham.student.txm683.framework.physics.Collidable;
import bham.student.txm683.framework.utils.Point;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Entity  implements Collidable {
    private String name;

    Entity(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void drawName(Canvas canvas, Point center){
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Entity)
            return name.equals(((Entity) o).name);

        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 43).append(name).toHashCode();
    }
}