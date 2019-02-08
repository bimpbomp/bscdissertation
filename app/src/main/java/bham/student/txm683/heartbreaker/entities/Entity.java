package bham.student.txm683.heartbreaker.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.physics.Collidable;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Entity  implements Collidable {
    private String name;

    private Paint paint;

    Entity(String name){
        this.name = name;

        this.paint = RenderingTools.initPaintForText(Color.GRAY, 30f, Paint.Align.CENTER);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Entity)
            return name.equals(((Entity) o).name);

        return false;
    }

    public void drawName(Canvas canvas, Point center){
        RenderingTools.renderCenteredText(canvas, paint, getName(), center);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 43).append(name).toHashCode();
    }
}