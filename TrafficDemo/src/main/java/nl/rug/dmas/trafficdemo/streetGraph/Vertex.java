package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.jbox2d.common.Vec2;

/**
 * Class to represent a vertex in a flow network using the adjacency list ADT.
 *
 * @author Laura & Bastiaan
 */
public class Vertex {

    private final int vertexListIndex;
    private final ArrayList<Edge> outgoingEdges;
    private final ArrayList<Edge> incomingEdges;
    private Vec2 location;

    /**
     * Constructor for a vertex.
     *
     * @param vertexListIndex the `ID' of the vertex
     */
    public Vertex(int vertexListIndex) {
        this.vertexListIndex = vertexListIndex;
        this.outgoingEdges = new ArrayList<>();
        this.incomingEdges = new ArrayList<>();
        this.location = null;
    }
    
    /**
     * Get the location of this part of the road in world coordinates.
     * @return location of this vertex in world coordinates
     */
    public Vec2 getLocation() {
        return this.location;
    }

    /**
     * Set the location of this part of the road network in world coordinates.
     * @param location in world coordinates.
     */
    public void setLocation(Vec2 location) {
        this.location = location;
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
