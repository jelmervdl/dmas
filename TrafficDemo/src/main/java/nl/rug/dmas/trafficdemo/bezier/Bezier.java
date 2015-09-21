/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.bezier;

import java.util.ArrayList;
import org.jbox2d.common.Vec2;

/**
 *
 * @author laura
 */
public abstract class Bezier {

    protected Vec2 source;
    protected Vec2 destination;

    public Bezier(Vec2 source, Vec2 destination) {
        this.source = source;
        this.destination = destination;
    }

    private float computeStepSize(int resolution) {
        return (float) 1.0 / (resolution - 1);
    }

    protected abstract Vec2 interpolate(float t, Vec2... controlPoints);

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
        Vec2 source = new Vec2((float) 1.0, (float) 2.0);
        Vec2 control = new Vec2((float) 1.0, (float) 3.0);
        Vec2 destination = new Vec2((float) 4.0, (float) 3.0);
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
