package bham.student.txm683.heartbreaker.rendering.popups;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import bham.student.txm683.heartbreaker.entities.entityshapes.Rectangle;
import bham.student.txm683.heartbreaker.input.Button;
import bham.student.txm683.heartbreaker.rendering.RenderingTools;
import bham.student.txm683.heartbreaker.utils.Point;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Popup {

    private List<PopUpElement> elements;
    private boolean show;
    private boolean pauseOnShow;
    private Paint textPaint;

    private IPause pauseFunction;

    private Rectangle background;

    public Popup(Point center, int width, int height, boolean pauseOnShow, List<PopUpElement> elements, IPause pauseFunction){
        this.elements = new ArrayList<>();

        this.background = new Rectangle(center, width, height, Color.LTGRAY);

        if (elements == null || elements.size() == 0)
            throw new IllegalArgumentException("Popup constructor requires at least one element");

        elements.sort(Comparator.comparingInt(PopUpElement::getVerticalPosition));

        this.elements = elements;

        this.elements.forEach(element ->
                element.setCenter(new Point(center.getX(), center.getY() - (height/2f) + height * (element.getVerticalPosition()/100f))));

        show = false;
        this.pauseOnShow = pauseOnShow;
        textPaint = RenderingTools.initPaintForText(Color.DKGRAY, 30, Paint.Align.CENTER);

        this.pauseFunction = pauseFunction;
    }

    public void draw(Canvas canvas, Point renderOffset){

        if (show) {

            background.draw(canvas, renderOffset, 0, false);

            elements.forEach(element -> element.draw(canvas, renderOffset, textPaint));
        }
    }

    public List<Button> getInputElements(){
        if (!show)
            return new ArrayList<>();

        List<Button> inputs = new ArrayList<>();

        for (PopUpElement element : elements){
            if (element instanceof Button)
                inputs.add((Button) element);
        }
        return inputs;
    }

    public void show(){
        this.show = true;

        if (pauseOnShow)
            pauseFunction.setPaused(true);
    }

    public void hide(){

        this.show = false;

        if (pauseOnShow)
            pauseFunction.setPaused(false);
    }

    public interface IPause{
        void setPaused(boolean pause);
    }
}
