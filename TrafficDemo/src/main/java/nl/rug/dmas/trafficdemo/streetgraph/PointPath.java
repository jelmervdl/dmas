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
import java.util.Objects;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class PointPath extends ArrayList<Vec2> {

    final Vertex origin;
    
    final Vertex destination;
    
    public PointPath(Vertex origin, Vertex destination) {
        this.origin = origin;
        this.destination = destination;
    }
    
    public PointPath(Vertex origin, Vertex destination, int size) {
        super(size);
        this.origin = origin;
        this.destination = destination;
    }

    public PointPath(Vertex origin, Vertex destination, Collection<Vec2> points) {
        super(points);
        this.origin = origin;
        this.destination = destination;
    }
    
    public Vertex getOrigin() {
        return origin;
    }

    public Vertex getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PointPath)
            return ((PointPath) o).getOrigin() == getOrigin()
                    && ((PointPath) o).getDestination() == getDestination();
        else
            return super.equals(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.origin);
        hash = 97 * hash + Objects.hashCode(this.destination);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("From %d to %d",
            getOrigin().getVertexListIndex(),
            getDestination().getVertexListIndex());
    }
    
    public PointPath translate(float move) {
        List<Vec2> normals = getNormals(this);
        PointPath out = new PointPath(origin, destination, size());
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
}
