package bham.student.txm683.framework.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import bham.student.txm683.framework.entities.entityshapes.Rectangle;
import bham.student.txm683.framework.utils.BoundingBox;
import bham.student.txm683.framework.utils.Point;

public class HealthBar {
    private Paint paint;

    private boolean display;
    private int count;
    private int countMax;

    private int health;
    private int initialHealth;

    public HealthBar(int initialHealth) {
        this.paint = new Paint();
        paint.setColor(Color.RED);

        this.health = initialHealth;
        this.initialHealth = initialHealth;

        this.display = false;
        this.count = 0;
        this.countMax = 60;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;

        display();

        if (this.health > initialHealth)
            this.health = initialHealth;
    }

    private void display(){
        display = true;
        count = 0;
    }

    public boolean inflictDamage(int damageToInflict) {
        health -= damageToInflict;

        display();

        boolean dead = health <= 0;

        if (dead){
            health = 0;
        }

        return dead;
    }

    public void restoreHealth(int healthToRestore) {
        health += healthToRestore;

        display();

        if (health > initialHealth)
            health = initialHealth;
    }

    public int getInitialHealth() {
        return initialHealth;
    }

    public void draw(Canvas canvas, Point offsetPosition){

        if (display && health > 0) {
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

            Log.d("HEALTH", "current: " + health + ", init: " + initialHealth + ", ratio: " + ((float) health / initialHealth));

            int padding = 10;
            float right = b.getLeft() + padding + ((float) health / initialHealth) * (width - 2 * padding);

            canvas.drawRect(b.getLeft() + padding, b.getTop() + padding, right, b.getBottom() - padding, paint);
        }
    }
}
