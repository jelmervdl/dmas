package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Class to represent a vertex in a flow network using the adjacency list ADT.
 *
 * @author Laura & Bastiaan
 */
public class Vertex {

    private final int vertexListIndex;
    private final ArrayList<Edge> outgoingEdges;
    private final ArrayList<Edge> incomingEdges;

    /**
     * Constructor for a vertex.
     *
     * @param vertexListIndex the `ID' of the vertex
     */
    public Vertex(int vertexListIndex) {
        this.vertexListIndex = vertexListIndex;
        this.outgoingEdges = new ArrayList<>();
        this.incomingEdges = new ArrayList<>();
    }

    /**
     * Get the value of outgoingEdges
     *
     * @return a copy of the list of outgoing edges of this vertex
     */
    protected ArrayList<Edge> getOutgoingEdges() {
        return new ArrayList<>(this.outgoingEdges);
    }

    /**
     * Get the value of vertexListIndex
     *
     * @return the value of vertexListIndex
     */
    protected int getVertexListIndex() {
        return vertexListIndex;
    }

    @Override
    public String toString() {
        return this.vertexListIndex + "\t Incoming: " + this.incomingEdges + "\n\t\t Outgoing: " + this.outgoingEdges;
    }

    /**
     *
     * @param edge
     */
    protected void addIncomingEdge(Edge edge) {
        this.incomingEdges.add(edge);
    }

    /**
     *
     * @param edge
     */
    protected void addOugoingEdge(Edge edge) {
        this.outgoingEdges.add(edge);
    }
}