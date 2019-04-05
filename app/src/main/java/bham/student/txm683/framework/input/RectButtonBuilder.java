package bham.student.txm683.framework.input;

public class RectButtonBuilder {
    private String label;
    private int verticalPosition;
    private Click buttonFunction;

    public RectButtonBuilder(String label, int verticalPosition, Click buttonFunction) {
        this.label = label;
        this.verticalPosition = verticalPosition;
        this.buttonFunction = buttonFunction;
    }

    public String getLabel() {
        return label;
    }

    public int getVerticalPosition() {
        return verticalPosition;
    }

    public Click getButtonFunction() {
        return buttonFunction;
    }
}
