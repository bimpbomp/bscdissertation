package bham.student.txm683.heartbreaker.rendering.popups;

public class TextBoxBuilder {
    private String label;
    private int verticalPosition;
    private int textSize;
    private int color;

    public TextBoxBuilder(String label, int verticalPosition, int textSize, int color) {
        this.label = label;
        this.verticalPosition = verticalPosition;
        this.textSize = textSize;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getVerticalPosition() {
        return verticalPosition;
    }

    public int getTextSize() {
        return textSize;
    }

    public int getColor() {
        return color;
    }
}
