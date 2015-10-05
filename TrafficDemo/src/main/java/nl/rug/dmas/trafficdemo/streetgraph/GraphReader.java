package nl.rug.dmas.trafficdemo.streetgraph;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import nl.rug.dmas.trafficdemo.actors.Sign;
import nl.rug.dmas.trafficdemo.actors.StopSign;
import org.jbox2d.common.Vec2;

/**
 *
 * @author Bastiaan & Laura
 */
public class GraphReader {

    private static Scanner scanner;
    private static StreetGraph graph;
    private static int numNodes;
    private static int numEdges;
    private static HashMap<Point, ArrayList<Sign>> signs;

    private static void readHeaderRow() {
        if (GraphReader.scanner.nextLine().equals("")) {
            GraphReader.scanner.nextLine();
        }
    }

    private static int readNaturalNumber(String descriptionOfInt) throws InputMismatchException {
        int naturalNumber = GraphReader.scanner.nextInt();
        if (naturalNumber <= 0) {
            throw new InputMismatchException(descriptionOfInt + "cannot be negative or zero.");
        }
        return naturalNumber;
    }

    private static HashSet<Integer> readSetOfNaturalNumbersFromLine(int maximumElementHeight) throws InputMismatchException {
        String line = GraphReader.scanner.nextLine();
        HashSet<Integer> naturalNumbers;
        try (Scanner lineScanner = new Scanner(line)) {
            naturalNumbers = new HashSet<>();
            int naturalNumber;
            while (lineScanner.hasNextInt()) {
                naturalNumber = lineScanner.nextInt();
                if (naturalNumber >= maximumElementHeight) {
                    throw new InputMismatchException("Node " + naturalNumber + " cannot be a a description for a node since there are only" + maximumElementHeight + " nodes.");
                }
                naturalNumbers.add(naturalNumber);
            }
        }
        return naturalNumbers;
    }

    private static void readNodeLocations() throws InputMismatchException {
        readHeaderRow();
        float x, y;
        try {
            for (int nodeNumber = 0; nodeNumber < GraphReader.numNodes; nodeNumber++) {
                x = GraphReader.scanner.nextFloat();
                y = GraphReader.scanner.nextFloat();
                GraphReader.graph.setVertexLocation(new Vec2(x, y), nodeNumber);
            }
        } catch (InputMismatchException e) {
            throw e;
        }
    }

    private static void readNumNodesAndEdges() {
        readHeaderRow();
        GraphReader.numNodes = readNaturalNumber("Number of nodes");
        GraphReader.numEdges = readNaturalNumber("Number of edges");
    }

    private static HashSet<Integer> readSinks() {
        readHeaderRow();
        HashSet<Integer> sinks = readSetOfNaturalNumbersFromLine(GraphReader.numNodes);
        return sinks;
    }

    private static void addSignToHashMap(Point edge, Sign sign) {
        if (!GraphReader.signs.containsKey(edge)) {
            GraphReader.signs.put(edge, new ArrayList<Sign>());
        }
        GraphReader.signs.get(edge).add(sign);
    }

    private static void readSigns(Point edge) throws InputMismatchException {
        Pattern signPatterns = Pattern.compile("[a-zA-Z]+");
        while (GraphReader.scanner.hasNext(signPatterns)) {
            String matched = GraphReader.scanner.findInLine(signPatterns);
            if (matched != null) {
                switch (matched) {
                    case "STOP":
                        GraphReader.addSignToHashMap(edge, new StopSign());
                        break;
                    default:
                        throw new InputMismatchException(String.format("The unknown sign %s was ignored.", matched));
                }
            } else {
                break; //Leave loop
            }
        }
        //TODO necessary?
        GraphReader.scanner.nextLine();
    }

    private static void readEdges() {
        readHeaderRow();
        int source, destination;
        for (int i = 0; i < GraphReader.numEdges; i++) {
            source = checkNodeValues(GraphReader.scanner.nextInt());
            destination = checkNodeValues(GraphReader.scanner.nextInt());
            GraphReader.graph.addEdge(source, destination);
            readSigns(new Point(source, destination));
        }
    }

    private static HashSet<Integer> readSources() {
        readHeaderRow();
        HashSet<Integer> sources = readSetOfNaturalNumbersFromLine(GraphReader.numNodes);
        return sources;
    }

    private static void addSignsToGraph() {
        Iterator<Map.Entry<Point, ArrayList<Sign>>> iterator = GraphReader.signs.entrySet().iterator();
        Edge edge;
        Vec2 location;
        while (iterator.hasNext()) {
            Map.Entry<Point, ArrayList<Sign>> signEdgePair = iterator.next();
            edge = GraphReader.graph.findEdge(signEdgePair.getKey().x, signEdgePair.getKey().y);
            //TODO currently signs are drawn over each other, increase the offset every iteration
            location = Sign.computeLocation(edge.getOrigin(), edge.getDestination());
            for (Sign sign : signEdgePair.getValue()) {
                sign.setLocation(location);
                GraphReader.graph.addSign(sign);
            }
        }
    }

    /**
     * Read an input file, according to the syntax presented in the readme of
     * this project, in a graph.
     *
     * @param file The path of the file relative to the folder input
     * @return A StreetGraph with the graph represented in the file.
     */
    public static StreetGraph read(File file) {
        try {
            GraphReader.scanner = new Scanner(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }

        GraphReader.graph = new StreetGraph();
        GraphReader.signs = new HashMap<>();

        try {
            readNumNodesAndEdges();
            HashSet<Integer> sources = readSources();
            HashSet<Integer> sinks = readSinks();

            readEdges();
            GraphReader.graph.setSinks(sinks);
            GraphReader.graph.setSources(sources);

            readNodeLocations();

            addSignsToGraph();
        } catch (InputMismatchException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        GraphReader.scanner.close();
        return graph;
    }

    private static int checkNodeValues(int nodeIndex) throws InputMismatchException {
        if (nodeIndex < 0 || nodeIndex >= numNodes) {
            throw new InputMismatchException("Illigal Node index.");
        }
        return nodeIndex;
    }

    public static void main(String[] args) {
        File inputFile = new File("./input/graaf.txt");
        StreetGraph graaf = GraphReader.read(inputFile);
        try {
            LinkedList<Vertex> path = graaf.findBFSPath(graaf.getSources().get(0), graaf.getSinks().get(0));
            graaf.generatePointPath(path);
        } catch (NoPathException ex) {
            System.err.println(ex);
        }
    }

}
