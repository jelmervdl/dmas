package nl.rug.dmas.trafficdemo.bezier;

import java.util.ArrayList;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public abstract class Bezier {

    /**
     * The source of the Bezier curve
     */
    protected Vec2 source;

    /**
     * The destination of the Bezier curve
     */
    protected Vec2 destination;

    /**
     *
     * @param source The source of the Bezier curve
     * @param destination The destination of the Bezier curve
     */
    public Bezier(Vec2 source, Vec2 destination) {
        this.source = source;
        this.destination = destination;
    }

    private float computeStepSize(int resolution) {
        return (float) 1.0 / (resolution - 1);
    }

    /**
     * Compute the point on the Bezier curve for a specific value of t given the
     * source, destination and possible some control points.
     *
     * @param t 0 <= t <= 1, indicates how far along the Bezier curve we are
     * @param controlPoints The controlpoints for th
     * e Bezier curve, the number of control points is equal to the degree of
     * the Bezier curve, i.e. the quadratic Bezier curve has one control point.
     * @return A point on the Bezier curve a t
     */
    protected abstract Vec2 interpolate(float t, Vec2... controlPoints);

    /**
     * Compute a list of resolution points representing the Bezier curve from
     * this.source to this.destination.
     *
     * @param resolution The number of points used to represent the curve.
     * @param controlPoints The control points for th e Bezier curve, the number
     * of control points is equal to the degree of the Bezier curve, i.e. the
     * quadratic Bezier curve has one control point.
     * @return A point on the Bezier curve a t
     * @return ArrayList of Vec2 representing a Bezier curve.
     */
    public ArrayList<Vec2> computePointsOnCurve(int resolution, Vec2... controlPoints) {
        ArrayList<Vec2> curvePoints = new ArrayList<>();
        float stepSize = computeStepSize(resolution);
        float currentT = 0;
        while (currentT <= 1) {
            curvePoints.add(this.interpolate(currentT, controlPoints));
            currentT += stepSize;
        }
        return curvePoints;
    }
    
    public static void main(String[] args) {
        Vec2 source = new Vec2((float) 1.0, (float) 0.0);
        Vec2 control = new Vec2((float) 0.0, (float) 2.0);
        Vec2 destination = new Vec2((float) 2.0, (float) 1.0);
        int resolution = 20;

        Bezier bluh = new QuadraticBezier(source, destination);
        ArrayList<Vec2> points = bluh.computePointsOnCurve(resolution, control);

        System.out.print("[");
        for (Vec2 point : points) {
            System.out.println(point.x + ", " + point.y + ";");
        }
        System.out.println("];");
    }
    
}
