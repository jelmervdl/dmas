package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Class to represent a flow graph
 *
 * @author Laura & Bastiaan
 */
public class StreetGraph {

    private final HashMap<Integer, Vertex> vertices;
    private final ArrayList<Edge> edges;
    private final HashMap<Integer, Vertex> sources;
    private final HashMap<Integer, Vertex> sinks;

    public StreetGraph() {
        this.sources = new HashMap<>();
        this.sinks = new HashMap<>();

        this.vertices = new HashMap<>();
        this.edges = new ArrayList<>();
    }

    private void vertexHashSetToHashMap(HashSet<Integer> indices, HashMap<Integer, Vertex> map) {
        Iterator<Integer> iterator = indices.iterator();
        Integer currentIndex;
        Vertex currentVertex;
        while (iterator.hasNext()) {
            currentIndex = iterator.next();
            currentVertex = this.vertices.get(currentIndex);
            if (currentVertex != null) {
                map.put(currentIndex, currentVertex);
            } else {
                throw new InputMismatchException(String.format("The selected source/sink with index %d is not defined", currentIndex));
            }

        }
    }

    public void setSources(HashSet<Integer> indices) {
        vertexHashSetToHashMap(indices, this.sources);
    }

    public void setSinks(HashSet<Integer> indices) {
        vertexHashSetToHashMap(indices, this.sinks);
    }

    /**
     * @return the sources of the graph
     */
    public ArrayList<Vertex> getSources() {
        return new ArrayList<>(sources.values());
    }

    /**
     *
     * @return the sinks of the graph
     */
    public ArrayList<Vertex> getSinks() {
        return new ArrayList<>(sinks.values());
    }

    /**
     *
     * @param origin the origin of the edge to be added, represented by its
     * name.
     * @param destination the destination of the edge to be added, represented
     * by its name.
     */
    public void addEdge(int origin, int destination) {
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

    @Override
    public String toString() {
// TODO Fix toString()
//        String res = "Source:\t" + this.sources.getVertexListIndex() + "\nSink:\t" + this.sinks.getVertexListIndex() + "\n" + "Vertices\n";
        String res = "";
        for (Entry<Integer, Vertex> entry : this.vertices.entrySet()) {
            res += "\t" + entry.getValue() + "\n";
        }
        return res;
    }
}
