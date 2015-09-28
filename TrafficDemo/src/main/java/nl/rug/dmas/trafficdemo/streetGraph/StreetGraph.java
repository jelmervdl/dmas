package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import org.jbox2d.common.Vec2;

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

    /**
     * A graph representing streets, vertices represent intersections, edges
     * represent roads. Sources represent locations where traffic may enter the
     * simulation, sinks where traffic may leave the simulation.
     */
    public StreetGraph() {
        this.sources = new HashMap<>();
        this.sinks = new HashMap<>();

        this.vertices = new HashMap<>();
        this.edges = new ArrayList<>();
    }

    private void vertexHashSetToHashMap(HashSet<Integer> indices, HashMap<Integer, Vertex> map) throws InputMismatchException {
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

    /**
     *
     * @param location
     * @param vertexName
     * @throws InputMismatchException
     */
    protected void setVertexLocation(Vec2 location, Integer vertexName) throws InputMismatchException {
        Vertex vertex = this.vertices.get(vertexName);
        if (vertex != null) {
            vertex.setLocation(location);
        } else {
            throw new InputMismatchException(String.format("Tried to set the location of vertex %d, but this vertex does not exist.", vertexName));
        }
    }

    /**
     * Set the sources of the StreetGraph, sources represent the locations where
     * traffic may enter the simulation.
     *
     * @param indices The 'names' of the sources
     */
    protected void setSources(HashSet<Integer> indices) {
        vertexHashSetToHashMap(indices, this.sources);
    }

    /**
     * Set the sinks of the StreetGraph, sinks represent the locations where
     * traffic may enter the simulation.
     *
     * @param indices The 'names' of the sinks
     */
    protected void setSinks(HashSet<Integer> indices) {
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
    protected void addEdge(int origin, int destination) {
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
     * Find a path from @origin to @destination using breadth first search. If
     * no path is found, either because no path exists or because the origin or
     * destination are not part of the graph an exception is thrown.
     *
     * @param origin
     * @param destination
     * @return
     * @throws nl.rug.dmas.trafficdemo.streetGraph.NoPathException
     */
    public ArrayList<Vertex> findBFSPath(Vertex origin, Vertex destination) throws NoPathException {
        LinkedList<Vertex> queue = new LinkedList<>();
        HashSet<Vertex> visited = new HashSet<>(this.vertices.size());
        HashMap<Vertex, Vertex> visitedFrom = new HashMap<>();
        Vertex currentVertex;

        queue.add(origin);
        visited.add(origin);

        if (!this.vertices.containsValue(origin)) {
            throw new NoPathException("The origin of your path could not be found in the graph.");
        } else if (!this.vertices.containsValue(origin)) {
            throw new NoPathException("The destination of your path could not be found in the graph.");
        }

        while (!queue.isEmpty()) {
            currentVertex = queue.remove();
            if (visited.contains(destination)) {
                //We have found our path, convert it to a list of vertices
                currentVertex = destination;
                LinkedList<Vertex> path = new LinkedList<>();
                while (!currentVertex.equals(origin)) {
                    path.addFirst(currentVertex);
                    currentVertex = visitedFrom.get(currentVertex);
                }
                path.addFirst(origin);
                return new ArrayList<>(path);
            } else {
                //Add all nodes that we haven't tried yet to the queue
                for (Vertex neighbour : currentVertex.getReachableVertices()) {
                    queue.add(neighbour);
                    visited.add(neighbour);
                    visitedFrom.put(neighbour, currentVertex);
                }
            }
        }
        throw new NoPathException("No path found.");
    }

    /**
     *
     * @param origin
     * @param destination
     * @return
     */
    public ArrayList<Vec2> generatePointPath(Vertex origin, Vertex destination) {
        ArrayList<Vec2> points = new ArrayList<>();

        return points;
    }

    public ArrayList<Vec2> generatePointPath(ArrayList<Vertex> path) {
        ArrayList<Vec2> points = new ArrayList<>();

        return points;
    }

    @Override
    public String toString() {
        String res = "Sources:\t" + this.sources.keySet() + "\nSinks:\t\t" + this.sinks.keySet() + "\n" + "Vertices\n";
        for (Entry<Integer, Vertex> entry : this.vertices.entrySet()) {
            res += "\t" + entry.getValue() + "\n";
        }
        return res;
    }
}
