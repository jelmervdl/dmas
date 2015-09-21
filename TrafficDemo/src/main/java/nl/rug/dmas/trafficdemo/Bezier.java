/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public class Bezier {

    private Vec2 source;
    private Vec2 destination;

    public Bezier(Vec2 source, Vec2 destination) {
        this.source = source;
        this.destination = destination;
    }

    private Vec2 linearInterpolation(float t) {
        return this.source.mul(1 - t).add(this.destination.mul(t));
    }

    private Vec2 quadraticBezier(Vec2 control, float t) {
        Vec2 intermediate1 = new Bezier(this.source, control).linearInterpolation(t);
        Vec2 intermediate2 = new Bezier(control, this.destination).linearInterpolation(t);
        return intermediate1.mulLocal(1 - t).addLocal(intermediate2.mulLocal(t));
    }

    private Vec2 cubicBezier(Vec2 sourceControl, Vec2 destinationControl, float t) {
        Vec2 intermediate1 = new Bezier(this.source, destinationControl).quadraticBezier(sourceControl, t);
        Vec2 intermediate2 = new Bezier(sourceControl, this.destination).quadraticBezier(destinationControl, t);

        return intermediate1.mulLocal(t).addLocal(intermediate2.mulLocal(t));
    }

    private float computeStepSize(int resolution) {
        return (float) 1.0 / (resolution - 1);
    }

    public ArrayList<Vec2> computeCubicPoints(Vec2 sourceControl, Vec2 destinationControl, int resolution) {
        ArrayList<Vec2> curvePoints = new ArrayList<>();
        float stepSize = computeStepSize(resolution);
        float currentT = 0;
        while (currentT <= 1.0) {
            curvePoints.add(this.cubicBezier(sourceControl, destinationControl, currentT));
            currentT += stepSize;
        }
        return curvePoints;
    }

    public ArrayList<Vec2> computeQuadraticPoints(Vec2 control, int resolution) {
        ArrayList<Vec2> curvePoints = new ArrayList<>();
        float stepSize = computeStepSize(resolution);
        float currentT = 0;
        while (currentT <= 1.0) {
            curvePoints.add(this.quadraticBezier(control, currentT));
            currentT += stepSize;
        }
        return curvePoints;
    }
}
