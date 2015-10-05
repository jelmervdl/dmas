package nl.rug.dmas.trafficdemo.streetgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import nl.rug.dmas.trafficdemo.bezier.LinearBezier;
import nl.rug.dmas.trafficdemo.bezier.QuadraticBezier;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

/**
 * Class to represent a flow graph
 *
 * @author Laura &amp; Bastiaan
 */
public class StreetGraph {

    private final HashMap<Integer, Vertex> vertices;
    private final ArrayList<Edge> edges;
    private final HashMap<Integer, Vertex> sources;
    private final HashMap<Integer, Vertex> sinks;
    private static int resolution = 50;
    private final static float turningRadius = 5.0f; 

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
                throw new InputMismatchException(String.format("The selected vertex with 'name' %d is not defined", currentIndex));
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
     * @return the sinks of the graph
     */
    public ArrayList<Vertex> getSinks() {
        return new ArrayList<>(sinks.values());
    }

    public List<Vertex> getVertices() {
        return new ArrayList<>(vertices.values());
    }

    public List<Edge> getEdges() {
        return new ArrayList<>(edges);
    }

    public boolean isSource(Vertex vertex) {
        return getSources().contains(vertex);
    }

    public boolean isSink(Vertex vertex) {
        return getSinks().contains(vertex);
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
     * @throws nl.rug.dmas.trafficdemo.streetgraph.NoPathException
     */
    public LinkedList<Vertex> findBFSPath(Vertex origin, Vertex destination) throws NoPathException {
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
                return path;
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

    private static PointPath generatePointPathUsingVec2(LinkedList<Vec2> path, float someControlParameter) throws NoPathException {
        PointPath points = new PointPath();
        if (path.size() == 2) {
            return generatePointLineSegment(path.poll(), path.poll());
        }
        Vec2 destinationPoint = path.getLast();
        path.addLast(destinationPoint);
        path.addLast(destinationPoint);

        Vec2 origin = path.poll();
        Vec2 intermediate = path.poll();
        Vec2 destination = path.poll();

        PointPath segmentPoints;
        //TODO: Dubbele punten vermijden
        while (!origin.equals(destinationPoint)) {
            if (pathIsStraight(origin, intermediate, destination)) {
                segmentPoints = generatePointLineSegment(origin, intermediate);
                origin = intermediate;
                intermediate = destination;
                destination = path.poll();
            } else {
                segmentPoints = generatePointCurve(origin, intermediate, destination, someControlParameter);
                origin = destination;
                intermediate = path.poll();
                destination = path.poll();
            }
            points.addAll(segmentPoints);
        }
        return points;
    }

    public PointPath generatePointPath(Vertex origin, Vertex destination) throws NoPathException {
        LinkedList<Vertex> path = this.findBFSPath(origin, destination);
        PointPath pointPath = StreetGraph.generatePointPath(path);
        return pointPath;
    }    
    
    public static PointPath generatePointPath(LinkedList<Vertex> path) throws NoPathException {
        LinkedList<Vec2> pathOfLocations = new LinkedList<>();
        ListIterator<Vertex> pathIter = path.listIterator();
        while (pathIter.hasNext()) {
            pathOfLocations.add(pathIter.next().getLocation());
        }
        return generatePointPathUsingVec2(pathOfLocations, turningRadius);
    }

    private static boolean pathIsStraight(Vec2 origin, Vec2 intermediate, Vec2 destination) {
        //TODO If the lines are parallel their cross product is zero, since three points can't define two parallel lines the three points must lie on one line.
        Vec2 originIntermediate = intermediate.sub(origin);
        Vec2 intermediateDestination = destination.sub(intermediate);
        double cross = Vec2.cross(originIntermediate, intermediateDestination);
        return cross == 0;
    }

    private static Vec2 computeSupportPoint(Vec2 pointA, Vec2 pointB, float someControlParameter){
        Vec2 abVector = pointB.sub(pointA);
        float t = 1 - (someControlParameter / MathUtils.distance(pointA, pointB));
        Vec2 supportPoint = pointA.add(abVector.mul(t));
        return supportPoint;
    }
    
    private static void buildHalvePointQueue(LinkedList<Vec2> queue, Vec2 pointA, Vec2 pointB, float someControlParameter) {
        if (MathUtils.distance(pointA, pointB) > someControlParameter) {
            queue.add(computeSupportPoint(pointA, pointB, someControlParameter));
        }
        queue.add(pointB);
    }

    private static PointPath generatePointCurve(Vec2 origin, Vec2 intermediate, Vec2 destination, float someControlParameter) throws NoPathException {
        LinkedList<Vec2> queue = new LinkedList<>();
        queue.addFirst(origin);
        buildHalvePointQueue(queue, origin, intermediate, someControlParameter);
        buildHalvePointQueue(queue, intermediate, destination, someControlParameter);
        //We are actually checking if origin and destination lie in a circle with radius someControlParameter placed on intermediate.
        if(queue.size() == 3){
            return new PointPath(new QuadraticBezier(origin, destination).computePointsOnCurve(StreetGraph.resolution, intermediate));
        } else {
            return generatePointPathUsingVec2(queue, someControlParameter);    
        }
//        float turningRadius = 7.0f;
//        Vec2 controlPoint = intermediate.add(new Vec2(turningRadius, turningRadius));
//        return new PointPath(new QuadraticBezier(origin, destination).computePointsOnCurve(StreetGraph.resolution, controlPoint));
    }

    public static PointPath generatePointLineSegment(Vec2 origin, Vec2 destination) throws NoPathException {
        if (origin == null || destination == null) {
            throw new NoPathException("Need at least two locations to draw a path.");
        }
        return new PointPath(new LinearBezier(origin, destination).computePointsOnCurve(StreetGraph.resolution));
    }

    public static PointPath generatePointLineSegment(Edge edge) throws NoPathException {
        return StreetGraph.generatePointLineSegment(
                edge.getOrigin().getLocation(),
                edge.getDestination().getLocation());
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
