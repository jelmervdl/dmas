/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.streetgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class PointPath extends ArrayList<Vec2> {

    public PointPath() {
        //
    }

    public PointPath(int size) {
        super(size);
    }

    public PointPath(Collection<Vec2> points) {
        super(points);
    }

    public PointPath translate(float move) {
        List<Vec2> normals = getNormals(this);
        PointPath out = new PointPath(size());
        for (int i = 0; i < size() - 1; ++i) {
            out.add(get(i).add(normals.get(i).mul(move)));
        }

        out.add(get(size() - 1).add(normals.get(normals.size() - 1).mul(move)));
        return out;
    }

    private Vec2 getNormal(Vec2 line) {
        Vec2 normal = new Vec2(-line.y, line.x);
        normal.normalize();
        return normal;
    }

    private List<Vec2> getNormals(List<Vec2> line) {
        List<Vec2> normals = new ArrayList<>(line.size());
        Iterator<Vec2> pointIter = line.iterator();

        Vec2 segmentStart;
        Vec2 segmentEnd = pointIter.next();

        while (pointIter.hasNext()) {
            segmentStart = segmentEnd;
            segmentEnd = pointIter.next();

            normals.add(getNormal(segmentEnd.sub(segmentStart)));
        }

        return normals;
    }

    @Override
    public String toString() {
        Iterator<Vec2> pointIterator = this.iterator();
        String output = "Pointpath:\n";
        while(pointIterator.hasNext()){
            output = output + "\t" + pointIterator.next().toString() + "\n";
        }
        return output;
    }
    
    
}
