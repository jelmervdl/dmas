package nl.rug.dmas.trafficdemo.streetGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * @author Bastiaan & Laura
 */
public class GraphReader {

    private static Scanner scanner;
    private static StreetGraph graph;
    private static int numNodes;
    private static int numEdges;

    private static void readHeaderRow(Scanner scanner) {
        if (scanner.nextLine().equals("")) {
            scanner.nextLine();
        }
    }

    private static int readNaturalNumber(Scanner scanner, String descriptionOfInt) {
        int naturalNumber = scanner.nextInt();
        if (naturalNumber <= 0) {
            throw new InputMismatchException(descriptionOfInt + "cannot be negative or zero.");
        }
        return naturalNumber;
    }

    private static HashSet<Integer> readSetOfNaturalNumbersFromLine(Scanner scanner, int maximumElementHeight) {
        String line = scanner.nextLine();
        Scanner lineScanner = new Scanner(line);
        HashSet<Integer> naturalNumbers = new HashSet<>();
        int naturalNumber;
        while (lineScanner.hasNextInt()) {
            naturalNumber = lineScanner.nextInt();
            if (naturalNumber >= maximumElementHeight) {
                throw new InputMismatchException("Node " + naturalNumber + " cannot be a a description for a node since there are only" + maximumElementHeight + " nodes.");
            }
            naturalNumbers.add(naturalNumber);
        }
        lineScanner.close();
        return naturalNumbers;
    }

    private static void readNumNodesAndEdges(Scanner scanner) {
        readHeaderRow(scanner);
        GraphReader.numNodes = readNaturalNumber(scanner, "Number of nodes");
        GraphReader.numEdges = readNaturalNumber(scanner, "Number of edges");
    }

    private static HashSet<Integer> readSinks(Scanner scanner) {
        readHeaderRow(scanner);
        HashSet<Integer> sinks = readSetOfNaturalNumbersFromLine(scanner, GraphReader.numNodes);
        return sinks;
    }

    private static void readEdges(Scanner scanner) {
        readHeaderRow(scanner);
        for (int i = 0; i < numEdges; i++) {
            graph.addEdge(checkNodeValues(scanner.nextInt()),
                    checkNodeValues(scanner.nextInt()));
        }
    }

    private static HashSet<Integer> readSources(Scanner scanner) {
        readHeaderRow(scanner);
        HashSet<Integer> sources = readSetOfNaturalNumbersFromLine(scanner, GraphReader.numNodes);
        return sources;
    }

    public static StreetGraph read(File file) {
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }

        graph = new StreetGraph();

        try {
            readNumNodesAndEdges(scanner);
            HashSet<Integer> sources = readSources(scanner);
            HashSet<Integer> sinks = readSinks(scanner);

            readEdges(scanner);
            graph.setSinks(sinks);
            graph.setSources(sources);

        } catch (InputMismatchException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
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
        System.out.println(graaf);
    }
}
