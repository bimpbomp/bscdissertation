package bham.student.txm683.heartbreaker.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.physics.Damageable;
import bham.student.txm683.heartbreaker.utils.BoundingBox;
import bham.student.txm683.heartbreaker.utils.GameTickTimer;
import bham.student.txm683.heartbreaker.utils.Point;

public class HealthBar {
    private Paint paint;

    private boolean damagedLastTick;

    private boolean display;
    private int count;
    private int countMax;

    private Damageable entity;

    private GameTickTimer timer;

    public HealthBar(Damageable entity) {
        this.paint = new Paint();
        paint.setColor(Color.RED);

        this.entity = entity;

        this.damagedLastTick = false;

        this.display = false;
        this.count = 0;
        this.countMax = 60;

        this.timer = new GameTickTimer(1000);
    }

    public void damaged() {
        display = true;

        count = 0;
    }

    public void draw(Canvas canvas, Point offsetPosition){

        if (display) {
            if (count > countMax){
                display = false;
            } else
                count++;


            int width = 100;
            int height = 50;
            Rectangle outer = new Rectangle(offsetPosition, width, height, Color.LTGRAY);
            BoundingBox b = outer.getBoundingBox();

            outer.draw(canvas, new Point(), 0, false);

            paint.setColor(Color.RED);

            int currentHealth = entity.getHealth();
            int initialHealth = entity.getInitialHealth();

            Log.d("HEALTH", "current: " + currentHealth + ", init: " + initialHealth + ", ratio: " + ((float) currentHealth / initialHealth));

            int padding = 10;
            float right = b.getLeft() + padding + ((float) currentHealth / initialHealth) * (width - 2 * padding);

            canvas.drawRect(b.getLeft() + padding, b.getTop() + padding, right, b.getBottom() - padding, paint);
        }
    }
}
