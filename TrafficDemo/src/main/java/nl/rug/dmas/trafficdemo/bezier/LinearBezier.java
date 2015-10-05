package nl.rug.dmas.trafficdemo.bezier;

import java.util.InputMismatchException;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public class LinearBezier extends Bezier {

    /**
     *
     * @param source The source of the Bezier curve
     * @param destination The destination of the Bezier curve
     */
    public LinearBezier(Vec2 source, Vec2 destination) {
        super(source, destination);
    }

    @Override
    public Vec2 interpolate(float t, Vec2... controlPoints) {
        if (controlPoints.length != 0) {
            throw new InputMismatchException("Linear bezier curves don't need controlpoints.");
        }
        return this.source.mul(1 - t).add(this.destination.mul(t));
    }
}
