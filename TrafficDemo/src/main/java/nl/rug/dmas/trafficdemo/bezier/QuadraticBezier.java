package nl.rug.dmas.trafficdemo.bezier;

import java.util.InputMismatchException;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public class QuadraticBezier extends Bezier {

    /**
     *
     * @param source The source of the Bezier curve
     * @param destination The destination of the Bezier curve
     */
    public QuadraticBezier(Vec2 source, Vec2 destination) {
        super(source, destination);
    }

    @Override
    protected Vec2 interpolate(float t, Vec2... controlPoints) {
        if (controlPoints.length != 1) {
            throw new InputMismatchException("Quadratic bezier curves need one controlpoint.");
        }
        Vec2 control = controlPoints[0];
        Vec2 intermediate1 = new LinearBezier(this.source, control).interpolate(t);
        Vec2 intermediate2 = new LinearBezier(control, this.destination).interpolate(t);
        return intermediate1.mulLocal(1 - t).addLocal(intermediate2.mulLocal(t));
    }
}
