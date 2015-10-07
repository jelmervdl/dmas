/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import nl.rug.dmas.trafficdemo.Actor;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.Observer;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.streetgraph.NoPathException;
import nl.rug.dmas.trafficdemo.streetgraph.PointPath;
import nl.rug.dmas.trafficdemo.streetgraph.Vertex;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;

/**
 *
 * @author jelmer
 */
public class StreetGraphSource implements Actor, Observer {

    private final Vertex vertex;
    private final Scenario scenario;
    private int fixturesInSight = 0;

    private float timeOfLastSpawn;
    final private float timeout;

    /**
     *
     * @param scenario 
     * @param vertex vertex on which the spawn point is located
     * @param timeout time between spawns in seconds
     */
    public StreetGraphSource(Scenario scenario, Vertex vertex, float timeout) {
        this.scenario = scenario;
        this.vertex = vertex;
        this.timeout = timeout;

        BodyDef def = new BodyDef();
        def.type = BodyType.STATIC;
        def.position = vertex.getLocation();

        FixtureDef fovDef = new FixtureDef();
        fovDef.shape = getFOVShape();
        fovDef.isSensor = true;
        fovDef.userData = this;

        Body body = scenario.getWorld().createBody(def);
        body.createFixture(fovDef);
    }

    @Override
    public int getActPeriod() {
        return 0;
    }

    private Shape getFOVShape() {
        Shape shape = new CircleShape();
        shape.setRadius(4);
        return shape;
    }

    protected Driver getMeADriver() throws NoPathException {
        List<Vertex> destinations = scenario.getStreetGraph().getSinks();
        Collections.shuffle(destinations);

        Iterator<Vertex> destIter = destinations.iterator();

        PointPath path = null;

        while (path == null && destIter.hasNext()) {
            try {
                path = scenario.getStreetGraph().generatePointPath(vertex, destIter.next());
            } catch (NoPathException e) {
                // Try the next one
            }
        }

        if (path == null) {
            throw new NoPathException();
        }

        return scenario.createDriver(path);
    }

    protected Car getMeACar(Driver driver) {
        return new Car(driver, 2, 4, vertex.getLocation());
    }

    @Override
    public void act() {
        Boolean enabled = (Boolean) scenario.getCommonKnowledge().get("spawnCars");
        
        if ((enabled == null || enabled) && fixturesInSight == 0) {
            try {
                if (scenario.getTime() - timeOfLastSpawn > timeout) {
                    Driver driver = getMeADriver();
                    scenario.add(getMeACar(driver));
                    timeOfLastSpawn = scenario.getTime();
                }
            } catch (NoPathException e) {
                System.err.println("Cannot spawn car because there is no destination that can be reached");
            }
        }
    }

    @Override
    public void addFixtureInSight(Fixture fixture) {
        fixturesInSight++;
    }

    @Override
    public void removeFixtureInSight(Fixture fixture) {
        fixturesInSight--;
    }
}
