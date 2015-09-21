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
    private boolean visited;
    private Edge visitedFromEdge;

    /**
     * Constructor for a vertex.
     *
     * @param vertexListIndex the `ID' of the vertex
     */
    public Vertex(int vertexListIndex) {
        this.vertexListIndex = vertexListIndex;
        this.outgoingEdges = new ArrayList<>();
        this.incomingEdges = new ArrayList<>();
        this.visited = false;
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

    /**
     *
     * @return true if the node has been visited, false if that isn't the case.
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     *
     * @param visited the new value for the member visited.
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Resets the information in this node that was relevant when traversing the
     * graph it is a vertex in. This method set the visitedFromEdge property to
     * null and set the visited flag to false.
     */
    public void clearVisitedFlags() {
        this.visited = false;
        this.visitedFromEdge = null;
    }

    /**
     *
     * @return the first found outgoing edge found that hasn't been visited yet
     * and has a residual capacity greater than zero.
     */
    public Edge getUnvisitedAdjacent() {
        for (final Edge edge : this.outgoingEdges) {
            if (!edge.getDestination().isVisited()) {
                return edge;
            }
        }
        for (final Edge edge : this.incomingEdges) {
            //Check if there is already some flow over the edge.
            if (!edge.getOrigin().isVisited()) {
                edge.setBackwardEdge(true);
                return edge;
            }
        }
        return null;
    }

    /**
     *
     * @return all unvisited adjacent vertices to this node that still have
     * residual capacity on their edge or already have some flow if they are
     * connected by a back edges. All edges returned in the collection are
     * marked as visited.
     */
    public Collection<Vertex> getAllUnvisitedAdjacent() {
        Collection<Vertex> vertices = new LinkedList<>();
        for (final Edge edge : this.outgoingEdges) {
            if (!edge.getDestination().isVisited()) {
                vertices.add(edge.getDestination());
                edge.getDestination().setVisitedFromEdge(edge);
                edge.getDestination().setVisited(true);
            }
        }
        for (final Edge edge : this.incomingEdges) {
            //Check if there is already some flow over the edge.
            if (!edge.getOrigin().isVisited()) {
                edge.setBackwardEdge(true);
                vertices.add(edge.getOrigin());
                edge.getOrigin().setVisitedFromEdge(edge);
                edge.getOrigin().setVisited(true);
            }
        }
        return vertices;
    }

    /**
     *
     * @return the edge from which this vertex has been visited last in BFS.
     */
    public Edge getVisitedFromEdge() {
        return visitedFromEdge;
    }

    /**
     *
     * @param edge the edge from which this vertex has been visited last in BFS.
     */
    void setVisitedFromEdge(Edge edge) {
        this.visitedFromEdge = edge;
    }
}
