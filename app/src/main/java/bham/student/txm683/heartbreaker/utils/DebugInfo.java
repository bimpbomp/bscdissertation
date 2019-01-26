package bham.student.txm683.heartbreaker.utils;

public class DebugInfo {

    private boolean renderPhysicsGrid = false;
    private boolean renderMapTileGrid = false;
    private boolean reset = false;
    private boolean renderEntityNames = false;

    public DebugInfo(){
    }

    public boolean renderPhysicsGrid() {
        return renderPhysicsGrid;
    }

    public void setRenderPhysicsGrid(boolean renderPhysicsGrid) {
        this.renderPhysicsGrid = renderPhysicsGrid;
    }

    public boolean renderMapTileGrid() {
        return renderMapTileGrid;
    }

    public void setRenderMapTileGrid(boolean renderMapTileGrid) {
        this.renderMapTileGrid = renderMapTileGrid;
    }

    public boolean reset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public boolean renderEntityNames() {
        return renderEntityNames;
    }

    public void setRenderEntityNames(boolean renderEntityNames) {
        this.renderEntityNames = renderEntityNames;
    }
}
