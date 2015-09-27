package nl.rug.dmas.trafficdemo.streetGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
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

    private static void readEdges() {
        readHeaderRow();
        for (int i = 0; i < GraphReader.numEdges; i++) {
            GraphReader.graph.addEdge(checkNodeValues(GraphReader.scanner.nextInt()),
                    checkNodeValues(GraphReader.scanner.nextInt()));
        }
    }

    private static HashSet<Integer> readSources() {
        readHeaderRow();
        HashSet<Integer> sources = readSetOfNaturalNumbersFromLine(GraphReader.numNodes);
        return sources;
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

        try {
            readNumNodesAndEdges();
            HashSet<Integer> sources = readSources();
            HashSet<Integer> sinks = readSinks();

            readEdges();
            GraphReader.graph.setSinks(sinks);
            GraphReader.graph.setSources(sources);

            readNodeLocations();
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
    }

}
