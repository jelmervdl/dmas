package nl.rug.dmas.trafficdemo.streetGraph;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 *
 * @author Laura & Bastiaan
 */
public class Path {

    private LinkedList<Edge> path;

    /**
     *
     */
    protected Path() {
        this.path = new LinkedList<>();
    }

    /**
     *
     * @return
     */
    public LinkedList<Edge> getPath() {
        return path;
    }

    /**
     *
     * @param edge
     */
    public void addEdge(Edge edge) {
        this.path.add(edge);
    }

    /**
     *
     * @throws NoSuchElementException
     */
    public void removeLastEdge() throws NoSuchElementException {
        this.path.removeLast();
    }

    /**
     *
     * @return
     */
    public Edge peek() {
        if (this.path.isEmpty()) {
            return null;
        }
        return this.path.peekLast();
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return this.path.isEmpty();
    }

    @Override
    public String toString() {
        String res = new String();
        for (final Edge edge : this.path) {
            res += edge + " ";
        }
        return res;
    }
}
