package nl.rug.dmas.trafficdemo.bezier;

import java.util.InputMismatchException;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public class CubicBezier extends Bezier {

    /**
     *
     * @param source The source of the Bezier curve
     * @param destination The destination of the Bezier curve
     */
    public CubicBezier(Vec2 source, Vec2 destination) {
        super(source, destination);
    }

    @Override
    public Vec2 interpolate(float t, Vec2... controlPoints) {
        if (controlPoints.length != 2) {
            throw new InputMismatchException("Cubic bezier curves need two controlpoints.");
        }
        Vec2 sourceControl = controlPoints[0];
        Vec2 destinationControl = controlPoints[1];
        Vec2 intermediate1 = new QuadraticBezier(this.source, destinationControl).interpolate(t, sourceControl);
        Vec2 intermediate2 = new QuadraticBezier(sourceControl, this.destination).interpolate(t, destinationControl);

        return intermediate1.mulLocal(t).addLocal(intermediate2.mulLocal(t));
    }
}
