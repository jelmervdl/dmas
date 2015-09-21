package nl.rug.dmas.trafficdemo.streetGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.Scanner;
import nl.rug.dmas.trafficdemo.streetGraph.FlowGraph;

/**
 *
 * @author Bastiaan & Laura
 */
public class GraphReader {

    private static Scanner scanner;
    private static FlowGraph graph;
    private static int numNodes;
    private static int numEdges;

    /**
     *
     * @param file
     * @return
     */
    public static FlowGraph read(File file) {
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }

        try {
            numNodes = scanner.nextInt();
            if (numNodes <= 0) {
                throw new InputMismatchException("Number of nodes can not be negative or zero.");
            }

            numEdges = scanner.nextInt();

            if (numEdges <= 0) {
                throw new InputMismatchException("Number of edges can not be negative or zero.");
            }

            int source = checkNodeValues(scanner.nextInt());
            int sink = checkNodeValues(scanner.nextInt());
            if (source == sink) {
                throw new InputMismatchException("Sink can not be the same node as the source.");
            }

            graph = new FlowGraph(source, sink);

            // Read in all the edges and add them to the graph.
            for (int i = 0; i < numEdges; i++) {
                graph.addEdge(checkNodeValues(scanner.nextInt()),
                        checkNodeValues(scanner.nextInt()),
                        checkNodeWeight(scanner.nextInt()));
            }
            // All edges have been read, rest of input (if any) will be ignored.

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

    private static int checkNodeWeight(int nodeWeight) {
        if (nodeWeight < 0) {
            throw new InputMismatchException("Illigal Node weight.");
        }
        return nodeWeight;
    }
}
