package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Class to represent a flow graph
 *
 * @author Laura & Bastiaan
 */
public class StreetGraph {

    private final HashMap<Integer, Vertex> vertices;
    private final ArrayList<Edge> edges;
    private final Vertex source;
    private final Vertex sink;

    /**
     *
     * @param source the source of the graph
     * @param sink the sink of the graph
     */
    public StreetGraph(int source, int sink) {
        this.source = new Vertex(source);
        this.sink = new Vertex(sink);
        this.vertices = new HashMap<>();
        this.edges = new ArrayList<>();
        this.vertices.put(source, this.source);
        this.vertices.put(sink, this.sink);
    }

    /**
     * @return the source of the graph
     */
    public Vertex getSource() {
        return source;
    }

    /**
     *
     * @return the sink of the graph
     */
    public Vertex getSink() {
        return sink;
    }

    /**
     *
     * @param origin the origin of the edge to be added, represented by its
     * name.
     * @param destination the destination of the edge to be added, represented
     * by its name.
     * @param capacity the capacity of the edge to be added.
     */
    public void addEdge(int origin, int destination, int capacity) {
        Vertex destinationVertex = this.vertices.get(destination);
        if (destinationVertex == null) {
            //Create destination vertex because it doesn't exist yet
            destinationVertex = new Vertex(destination);
            this.vertices.put(destination, destinationVertex);
        }
        Vertex originVertex = this.vertices.get(origin);
        if (originVertex == null) {
            //Create destination vertex because it doesn't exist yet
            originVertex = new Vertex(origin);
            this.vertices.put(origin, originVertex);
        }
        Edge newEdge = new Edge(originVertex, destinationVertex);
        this.edges.add(newEdge);
        originVertex.addOugoingEdge(newEdge);
        destinationVertex.addIncomingEdge(newEdge);
    }

    /**
     * Set the visited flag on false for every vertex in the graph.
     */
    private void clearFlags() {
        for (Entry<Integer, Vertex> entry : this.vertices.entrySet()) {
            entry.getValue().clearVisitedFlags();
        }
        for (Edge edge : this.edges) {
            edge.setBackwardEdge(false);
        }
    }

    @Override
    public String toString() {
        String res = "Source:\t" + this.source.getVertexListIndex() + "\nSink:\t" + this.sink.getVertexListIndex() + "\n" + "Vertices\n";
        for (Entry<Integer, Vertex> entry : this.vertices.entrySet()) {
            res += "\t" + entry.getValue() + "\n";
        }
        return res;
    }
}
