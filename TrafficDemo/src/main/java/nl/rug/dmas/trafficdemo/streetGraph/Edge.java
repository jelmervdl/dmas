package nl.rug.dmas.trafficdemo.streetGraph;

/**
 * Class to represent an edge in a flow network using the adjacency list ADT.
 *
 * @author Laura & Bastiaan
 */
public class Edge {

    private final Vertex origin;
    private final Vertex destination;

    /**
     *
     * @param origin the vertex where this edge starts.
     * @param destination the vertex where this edge ends.
     */
    protected Edge(Vertex origin, Vertex destination) {
        this.origin = origin;
        this.destination = destination;
    }

    /**
     * Get the value (vertex) of destination
     *
     * @return the value of destination
     */
    public Vertex getDestination() {
        return destination;
    }

    /**
     * Get the value of origin
     *
     * @return the value of origin
     */
    public Vertex getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return this.origin.getVertexListIndex() + " -> " + this.destination.getVertexListIndex();
    }
}
