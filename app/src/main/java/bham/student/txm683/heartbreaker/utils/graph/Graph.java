package bham.student.txm683.heartbreaker.utils.graph;

import android.support.annotation.NonNull;
import android.util.Pair;
import bham.student.txm683.heartbreaker.utils.UniqueID;

import java.util.*;

public class Graph <T> {
    private Map<T, Node<T>> nodes;
    private UniqueID uniqueID;

    public Graph(){
        this.nodes = new HashMap<>();
        this.uniqueID = new UniqueID();
    }

    public Node<T> addNode(T id){
        Node<T> newNode;
        if (!nodes.containsKey(id)){
            newNode = new Node<>(id);

            nodes.put(id, newNode);
            return newNode;
        }

        return null;
    }

    public Node<T> addNode(Node<T> node){
        if (!nodes.containsKey(node.getNodeID())){
            nodes.put(node.getNodeID(), node);
            return node;
        }
        return null;
    }

    public Edge<T> addConnection(Node<T> from, Node<T> to, int weight){
        Edge<T> connection = new Edge<>(uniqueID.id(), from, to, weight);

        //dont connect two nodes that are already connected
        if (from.hasConnectionToNode(to))
            return from.getConnectionTo(to);

        from.addConnection(connection);

        return connection;
    }

    public Edge<T> addConnection(T first, T second, int weight){
        if (containsNode(first) && containsNode(second)){
            return addConnection(getNode(first), getNode(second), weight);
        }

        return null;
    }

    public Edge<T> addConnection(T first, T second){
        return addConnection(first, second, 1);
    }

    public void removeConnection(T from, T to){
        Node<T> fromNode = getNode(from);
        Node<T> toNode = getNode(to);

        fromNode.removeConnectionTo(toNode);
    }

    public Node<T> getNode(T requestedID){
        return this.nodes.containsKey(requestedID) ? nodes.get(requestedID) : null;
    }

    public ArrayList<Node<T>> getNodes(){
        return new ArrayList<>(this.nodes.values());
    }

    public boolean containsNode(T id){
        return nodes.containsKey(id);
    }

    public boolean containsNode(Node<T> node){return nodes.containsKey(node.getNodeID());}

    public List<T> applyAStar(Node<T> startNode, Node<T> targetNode, boolean returnIncompletePath, IHeuristic<T> heuristic){

        //creates a priority queue based on a Tile's fCost (Lowest cost at head)
        PriorityQueue<Pair<Node<T>, Integer>> openSet;

        //each key is the tile coordinate of a tile. It's value is the gcost spent to get to that tile from the start
        Map<Node<T>, Integer> costSoFar;

        //each key is the tile coordinate of a tile, it's value is the 'parent' of this tile.
        //i.e. the tile that comes before the key tile in the basePath
        Map<Node<T>, Node<T>> cameFrom;

        openSet = new PriorityQueue<>(10, (a, b) -> {
            if (a.second < b.second)
                return -1;
            else if (a.second.equals(b.second))
                return 0;
            return 1; });
        cameFrom = new HashMap<>();
        costSoFar = new HashMap<>();

        //initialise sets by adding the start tile with 0 costs
        openSet.add(new Pair<>(startNode, 0));
        costSoFar.put(startNode, 0);

        //has a value of null so the tracePath algorithm knows when to stop backtracking
        cameFrom.put(startNode, null);

        if (startNode.equals(targetNode)){
            //Log.d(controlled.getName(), "Already at destination");
            return tracePath(formPathStack(cameFrom, targetNode));
        }

        while (!openSet.isEmpty()){

            Pair<Node<T>, Integer> currentPair = openSet.poll();
            Node<T> currentNode = currentPair.first;
            int currentCost = currentPair.second;

            for (Edge<T> connection : currentNode.getConnections()){

                Node<T> neighbour = connection.traverse();

                //Log.d(controlled.getName(), "current: " + currentNode.getNodeID().toString() + " neighbour: " + neighbour.getNodeID().toString());

                //if the next tile is the target, add it to the cameFrom map and return the basePath generated
                //by tracePath
                if (targetNode.equals(neighbour)){
                    //Log.d(controlled.getName(), "Target Reached!");

                    cameFrom.put(neighbour, currentNode);
                    return tracePath(formPathStack(cameFrom, targetNode));
                }

                int gCostToNext = currentCost + connection.getWeight();

                //If the tile hasn't been visited before, or the cost to get to this tile is cheaper than the already stored cost
                //add it to all tracking sets
                if (!costSoFar.containsKey(neighbour) || costSoFar.get(neighbour) > gCostToNext) {

                    int fCost = gCostToNext + heuristic.calc(currentNode.getNodeID(), neighbour.getNodeID());

                    costSoFar.put(neighbour, gCostToNext);
                    openSet.add(new Pair<>(neighbour, fCost));
                    cameFrom.put(neighbour, currentNode);
                }
            }
        }

        if (returnIncompletePath)
            return tracePath(formPathStack(cameFrom, targetNode));
        return new ArrayList<>();
    }

    private List<T> tracePath(Stack<T> path){

        List<T> pathList = new ArrayList<>();

        while (!path.empty()){
            T nextStep = path.pop();

            pathList.add(nextStep);
        }
        return pathList;
    }

    private Stack<T> formPathStack(Map<Node<T>, Node<T>> cameFrom, Node<T> targetNodeName){
        Stack<T> path = new Stack<>();

        Node<T> previous = targetNodeName;
        Node<T> current = cameFrom.get(targetNodeName);

        path.push(previous.getNodeID());

        while (current != null){
            path.push(current.getNodeID());
            previous = current;

            current = cameFrom.get(previous);
        }
        return path;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Graph\n");

        for (Node<T> node : nodes.values()){
            stringBuilder.append("Node ");
            stringBuilder.append(node.getNodeID().toString());

            if (node.getConnections().size() > 0){
                stringBuilder.append(" has neighbours: ");
                for (Edge<T> connection : node.getConnections()){
                    stringBuilder.append(connection.traverse().getNodeID().toString());
                    stringBuilder.append(" (");
                    stringBuilder.append(connection.getWeight());
                    stringBuilder.append("), ");
                }
                //removes final ", "
                stringBuilder.delete(stringBuilder.lastIndexOf(", "), stringBuilder.length());

                stringBuilder.append(".\n");

            } else {
                stringBuilder.append(" has no neighbours.\n");
            }
        }
        return stringBuilder.toString();
    }
}
