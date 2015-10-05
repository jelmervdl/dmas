package nl.rug.dmas.trafficdemo.streetgraph;

import java.util.ArrayList;
import java.util.HashSet;
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
        super();
        this.vertexListIndex = vertexListIndex;
        this.outgoingEdges = new ArrayList<>();
        this.incomingEdges = new ArrayList<>();
    }

    /**
     * Get the vertices that can be reached from his vertex by traversing one
     * edge.
     *
     * @return
     */
    public ArrayList<Vertex> getReachableVertices() {
        HashSet<Vertex> reachables = new HashSet<>();
        for (Edge outgoingEdge : this.outgoingEdges) {
            reachables.add(outgoingEdge.getDestination());
        }
        return new ArrayList<>(reachables);
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
    public int getVertexListIndex() {
        return vertexListIndex;
    }

    @Override
    public String toString() {
        return this.vertexListIndex +  this.location.toString() +  "\t Incoming: " + this.incomingEdges + "\n\t\t\t Outgoing: " + this.outgoingEdges;
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

    protected void setLocation(Vec2 location) {
        this.location = location;
    }

    public Vec2 getLocation() {
        return this.location;
    }
}
