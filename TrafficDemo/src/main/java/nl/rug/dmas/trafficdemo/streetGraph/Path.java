package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 *
 * @author Laura & Bastiaan
 */
public class Path {

    private LinkedList<Edge> path;
    private int maxFlow;

    /**
     *
     */
    protected Path() {
        this.path = new LinkedList<>();
    }

    public int getMaxFlow() {
        return maxFlow;
    }

    /**
     *
     * @param sink
     * @param source
     */
    protected Path(Vertex sink, Vertex source) {
        //Retrace path
        this();
        Vertex currentVertex = sink;
        Edge currentEdge = sink.getVisitedFromEdge();
        int minimalCapacityOnPath = Integer.MAX_VALUE;
        while (currentVertex != source) {
            this.path.addFirst(currentEdge);
            if (currentEdge.isBackwardEdge()) {
                currentVertex = currentEdge.getDestination();
                currentEdge = currentVertex.getVisitedFromEdge();
            } else {
                currentVertex = currentEdge.getOrigin();
                currentEdge = currentVertex.getVisitedFromEdge();
            }
        }
    }

    /**
     *
     * @return
     */
    public LinkedList<Edge> getPath() {
        return path;
    }

    /**
     *
     * @param edge
     */
    public void addEdge(Edge edge) {
        this.path.add(edge);
    }

    /**
     *
     * @throws NoSuchElementException
     */
    public void removeLastEdge() throws NoSuchElementException {
        this.path.removeLast();
    }

    public Edge peek() {
        if (this.path.isEmpty()) {
            return null;
        }
        return this.path.peekLast();
    }

    public boolean isEmpty() {
        return this.path.isEmpty();
    }

    @Override
    public String toString() {
        String res = new String();
        for (final Edge edge : this.path) {
            res += edge + " ";
        }
        return res;
    }
}
