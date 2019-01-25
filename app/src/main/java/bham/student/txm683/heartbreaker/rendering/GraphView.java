package bham.student.txm683.heartbreaker.rendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import bham.student.txm683.heartbreaker.utils.graph.Edge;
import bham.student.txm683.heartbreaker.utils.graph.Graph;
import bham.student.txm683.heartbreaker.utils.graph.Node;

import java.util.ArrayList;

public class GraphView extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = "hb::GraphView";

    private int viewWidth;
    private int viewHeight;

    private Paint textPaint;

    public GraphView(Context context){
        super(context);

        getHolder().addCallback(this);

        setFocusable(true);

        textPaint = RenderingTools.initPaintForText(Color.BLACK, 48f, Paint.Align.CENTER);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        Graph graph = new Graph();

        int numberOfNodesOnOneSide = 6;
        float widthOffset = viewWidth/((numberOfNodesOnOneSide/2f)+1);
        float heightOffset = viewHeight/((numberOfNodesOnOneSide/2f)+1);

        for (int i = 1; i <= numberOfNodesOnOneSide/2; i++){
            for (int j = 1; j <= numberOfNodesOnOneSide/2; j++){

                graph.addNode(j + ":" + i);
            }
        }

        ArrayList<Node> nodes = graph.getNodes();

        graph.addConnection(nodes.get(0), nodes.get(1), 5);
        graph.addConnection(nodes.get(2), nodes.get(1), 5);

        Canvas canvas = getHolder().lockCanvas();

        //draw background
        canvas.drawRGB(255,255,255);

        Paint nodePaint = new Paint();
        nodePaint.setColor(Color.BLUE);

        for (Node node : nodes){

            for (Edge edge : node.getConnections()){
                Log.d(TAG, node.getName() + ": " + edge.toString());
            }

            String[] coors = node.getName().split(":");
            float x = Integer.parseInt(coors[0])*widthOffset;
            float y = Integer.parseInt(coors[1])*heightOffset;

            canvas.drawCircle(x, y, 50, nodePaint);
            canvas.drawText(node.getName(), x, y, textPaint);
        }

        try {
            getHolder().unlockCanvasAndPost(canvas);
        } catch (IllegalArgumentException e){
            //canvas is destroyed already
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.viewWidth = width;
        this.viewHeight = height;

        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged");
        this.viewWidth = w;
        this.viewHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
