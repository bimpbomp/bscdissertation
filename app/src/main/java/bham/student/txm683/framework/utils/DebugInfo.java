package bham.student.txm683.framework.utils;

public class DebugInfo {

    private boolean renderPhysicsGrid = false;
    private boolean renderMapTileGrid = false;
    private boolean renderEntityNames = false;
    private boolean activateAI = true;

    public DebugInfo(){
    }

    public boolean renderVisSet() {
        return renderPhysicsGrid;
    }

    public void invertRenderVisSet() {
        this.renderPhysicsGrid = !renderPhysicsGrid;
    }

    public boolean renderMapTileGrid() {
        return renderMapTileGrid;
    }

    public void invertRenderMapTileGrid() {
        this.renderMapTileGrid = !renderMapTileGrid;
    }

    public boolean renderEntityNames() {
        return renderEntityNames;
    }

    public void invertRenderEntityNames() {
        this.renderEntityNames = !renderEntityNames;
    }

    public boolean isAIActivated(){return this.activateAI;}

    public void invertActivateAI(){this.activateAI = !this. activateAI;}
}