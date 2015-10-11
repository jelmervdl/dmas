package nl.rug.dmas.trafficdemo.streetgraph;

import java.util.Objects;

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
    
    public Edge reversed() {
        return new Edge(destination, origin);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof Edge) {
            Edge other = (Edge) obj;
            return origin == other.origin
                    && destination == other.destination;
        }
        else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.origin);
        hash = 17 * hash + Objects.hashCode(this.destination);
        return hash;
    }
    
    @Override
    public String toString() {
        return this.origin.getVertexListIndex() + " -> " + this.destination.getVertexListIndex();
    }
}
