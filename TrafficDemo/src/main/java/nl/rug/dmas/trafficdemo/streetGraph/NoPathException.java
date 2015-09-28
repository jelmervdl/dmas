package nl.rug.dmas.trafficdemo.streetGraph;

/**
 *
 * @author laura
 */
public class NoPathException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoPathException() {
        super();
    }

    public NoPathException(String message) {
        super(message);
    }
}
