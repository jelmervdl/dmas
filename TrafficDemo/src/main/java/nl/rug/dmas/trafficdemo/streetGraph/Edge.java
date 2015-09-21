package nl.rug.dmas.trafficdemo.streetGraph;

/**
 * Class to represent an edge in a flow network using the adjacency list ADT.
 *
 * @author Laura & Bastiaan
 */
public class Edge {

    private final Vertex origin;
    private final Vertex destination;
    private boolean backwardEdge;

    /**
     *
     * @param origin the vertex where this edge starts.
     * @param destination the vertex where this edge ends.
     */
    protected Edge(Vertex origin, Vertex destination) {
        this.origin = origin;
        this.destination = destination;
        this.backwardEdge = false;
    }

    protected boolean isBackwardEdge() {
        return backwardEdge;
    }

    protected void setBackwardEdge(boolean backwardEdge) {
        this.backwardEdge = backwardEdge;
    }

    /**
     * Get the value (vertex) of destination
     *
     * @return the value of destination
     */
    protected Vertex getDestination() {
        return destination;
    }

    /**
     * Get the value of origin
     *
     * @return the value of origin
     */
    protected Vertex getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return this.origin.getVertexListIndex() + " -> " + this.destination.getVertexListIndex();
    }
}
