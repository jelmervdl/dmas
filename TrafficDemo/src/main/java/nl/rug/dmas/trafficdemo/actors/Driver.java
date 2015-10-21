/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo.actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import nl.rug.dmas.trafficdemo.Acceleration;
import nl.rug.dmas.trafficdemo.Actor;
import nl.rug.dmas.trafficdemo.Car;
import nl.rug.dmas.trafficdemo.DebugGraphicsQueue;
import nl.rug.dmas.trafficdemo.Observer;
import nl.rug.dmas.trafficdemo.Scenario;
import nl.rug.dmas.trafficdemo.VecUtils;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

/**
 * Abstract class that represents a driver. Missing is the act() method that
 * controls the actual behaviour. This abstract implementation offers a few
 * utilities such as getCarsInSight() for the implementations.
 * @author jelmer
 */
abstract public class Driver implements Actor, Observer {
    final protected Scenario scenario;
    
    protected Car car;
    
    final List<Vec2> path;
    
    int pathIndex = 0;
    
    float lastMovement = 0;
    
    // A set of all fixtures in the field of view. It is updated by the
    // scenario step function itself, drivers (actors) don't have access
    // to anything outside of this list and the public getters of Scenario.
    final protected Set<Fixture> fixturesInSight = new HashSet<>();
    
    final public DebugGraphicsQueue debugDraw = new DebugGraphicsQueue();
    
    final float timeOfCreation;
    
    final float viewLength;
    
    final float skipAheadPointDistance = 3.0f;
    
    final float moveToNextPointDistance = 5.0f;
    
    final float brakeDistance = 3.0f; // meter
    
    final float reverseDistance = 0.0f; // meter (0 -> disabled)
    
    final float maxSpeed = 30f; // kph
    
    final float stuckTime = 10f; // seconds
    
    final float slowDistance = 5.0f;
    
    final float slowSpeed = 5f; // kph
    
    final float patienceTime = 30; // seconds
    
    /**
     * Create a driver that will drive along a predefined route of positions
     * @param scenario
     * @param path List of Vec2's in world coordinates
     */
    public Driver(Scenario scenario, List<Vec2> path, float viewLength) {
        this.scenario = scenario;
        this.path = path;
        this.viewLength = viewLength;
        this.timeOfCreation = scenario.getTime();
    }
    
    /**
     * Called by Car when a Driver is passed in
     * @param car 
     */
    public void setCar(Car car) {
        this.car = car;
    }
    
    /**
     * Should return a shape that represents the field of view of the driver.
     * @return 
     */
    public Shape getFOVShape() {
        Shape shape = new CircleShape();
        shape.setRadius(8);
        return shape;
    }
    
    public List<Vec2> getPath() {
        return path;
    }
    
    public int getPathIndex() {
        return pathIndex;
    }
    
    public float getDrivingTime() {
        return scenario.getTime() - timeOfCreation;
    }

    /**
     * Called when a Fixture enters our FOV shape in the world.
     * Callback for the Observer interface.
     * @param fixture
     */
    @Override
    public void addFixtureInSight(Fixture fixture) {
        fixturesInSight.add(fixture);
    }

    /**
     * Called when a Fixture leaves our FOV shape in the world.
     * Callback for the Observer interface.
     * @param fixture
     */
    @Override
    public void removeFixtureInSight(Fixture fixture) {
        fixturesInSight.remove(fixture);
    }
    
    /**
     * Filters the list of fixtures in sight and only returns actual cars that
     * are not my own car.
     * @return a list of cars inside the FOV.
     */
    protected List<Car> getCarsInSight() {
        ArrayList<Car> cars = new ArrayList<>();
        for (Fixture fixture : fixturesInSight)
            if (!fixture.isSensor()
                    && fixture.getUserData() instanceof Car
                    && fixture.getUserData() != car)
                cars.add((Car) fixture.getUserData());
        
        return cars;
    }
    
    protected Vec2 steerTowardsPath() {
        if (path != null) {
            // first, try to find the closest point
            for (int i = pathIndex; i < path.size(); ++i) {
                Vec2 directionToNextPoint = car.getDriverPoint(path.get(i));
                
                // but stop after the first one because we shouldn't skip too much
                if (directionToNextPoint.length() < skipAheadPointDistance) {
                    pathIndex = i;
                    break;
                }
            }
            
            // then try to find the next point far way enough from the car to 
            // drive towards
            while (pathIndex < path.size()) {
                Vec2 directionToNextPoint = car.getDriverPoint(path.get(pathIndex));
                
                if (directionToNextPoint.length() > moveToNextPointDistance) {
                    return directionToNextPoint;
                } else {
                    pathIndex += 1;
                }
            }
        }
        
        return new Vec2(0, 0);
    }
    
    /**
     * Calculate a recommended speed for not colliding with anything in front of
     * the car. It does this by shooting rays from the bumper of the vehicle and
     * see whether they collide with any other cars. Accepts steering angle so
     * it can match the attention of the driver.
     * @param steeringAngle in radians. Same value as you pass to
     * Car.setSteeringDirection(float).
     * @param rayLength in meter. Longer rays will detect more, obviously.
     * @param numberOfRays more rays will yield better precision. About 10
     * already provides quite good results.
     * @return distance to closest object or -1 if none.
     */
    protected float speedAdjustmentToPreventColission(float steeringAngle, float rayLength, int numberOfRays) {
        float originStart = -car.getWidth() / 2;
        float originEnd = car.getWidth() / 2;
        float originStep = (originEnd - originStart) / (numberOfRays - 1);
        
        float originOffset = 0.2f; // how far away from the front of the car does the ray start.
        float originY = -car.getLength() / 2 - originOffset; // front of the car
        
        // The arcs bent with the steering of the car
        float arcStart = MathUtils.min(2.5f * MathUtils.QUARTER_PI - steeringAngle, MathUtils.PI);
        float arcStop = MathUtils.max(1.5f * MathUtils.QUARTER_PI - steeringAngle, 0);
        float arcStep = (arcStop - arcStart) / (numberOfRays - 1);
        
        final AtomicReference<Float> hit = new AtomicReference<>();
        
        for (int i = 0; i < numberOfRays; ++i) {
            final float angle = arcStart + i * arcStep;
            final Vec2 origin = car.getWorldPoint(new Vec2(originStart + i * originStep, originY));
            final Vec2 direction = car.getWorldVector(new Vec2(rayLength * MathUtils.cos(angle), rayLength * -MathUtils.sin(angle)));
            
            debugDraw.draw(origin, direction);
            
            scenario.getWorld().raycast(new RayCastCallback() {
                @Override
                public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
                    if (!(fixture.getUserData() instanceof Car))
                        return 1;
                    
                    // I have to make copies of point and normal as they are
                    // reused by raycast() for the next call to the callback.
                    debugDraw.draw(new Vec2(point), new Vec2(normal));
                    
                    float dist = point.sub(origin).length();
                    if (hit.get() == null || hit.get() > dist) {
                        hit.set(dist);
                    }
                    
                    return fraction;
                }
            }, origin, origin.add(direction));
        }
        
        return hit.get() == null ? -1 : hit.get();
    }
    
    /**
     * Behaviour that looks at the velocity and direction of all cars in sight
     * and determines if a cars path is going to intersect with our path.
     * @return time until this car will intersect its path with another car
     */
    protected float speedAdjustmentToAvoidCars() {
        VecUtils.Intersection mostImportant = null;
        
        for (Car other : getCarsInSight()) {
            // If that car is driving towards me, well, shit!
            
            debugDraw.draw(other.getPosition(), other.getAbsoluteVelocity());
            
            VecUtils.Intersection intersection = VecUtils.intersect(car.getPosition(), car.getAbsoluteVelocity(),
                other.getPosition(), other.getAbsoluteVelocity());
            
            // Is the driver coming from the left side? Then we have prevedence
            if (car.getLocalPoint(other.getPosition()).x < 0.3f)
                continue;
            
            if (intersection == null)
                continue;
            
            debugDraw.draw(intersection.position);
            
            if (mostImportant == null || Math.abs(mostImportant.u - mostImportant.v) > Math.abs(intersection.u - intersection.v))
                mostImportant = intersection;
        }
        
        if (mostImportant == null)
            return 0;
        
        // Time until I reach the intersection minus the time the other
        // reaches the intersection
        float d = mostImportant.v - mostImportant.u;

        // If there is enough time to pass safely, ignore this car
        // Todo: determine 3 using the length of our and the other car and
        // their speed. Hard math ahead?
        //if (Math.abs(d) > 4)
        //    return 0;
        
        // d < 0: I'm there first, we should speed up a bit maybe?
        // d > 0: other is there first, we should brake?
        return d;
    }

    /**
     * Test whether the destination is reached. This method is used by the sinks
     * to determine whether a car can be removed from the scenario.
     * @return whether the destination is reached right now
     */
    public boolean reachedDestination() {
        return path != null && pathIndex == path.size();
    }
    
    /**
     * Update the steering direction of the car we drive
     */
    @Override
    public void act()
    {
        debugDraw.clear();
        
        float steeringAngle = VecUtils.getAngle(steerTowardsPath().negate());
        
        car.setSteeringDirection(steeringAngle);
        
        float distance = speedAdjustmentToPreventColission((steeringAngle - MathUtils.HALF_PI) * 2f, Math.max(Math.max(slowDistance, brakeDistance), reverseDistance), 10);
        
        if (distance > 0 && distance < reverseDistance && car.getLocalVelocity().y > 0.0f && stuckTime > scenario.getTime() - lastMovement) {
            car.setAcceleration(Acceleration.REVERSE);
        } else if (distance > 0 && distance < brakeDistance) {
            car.setAcceleration(Acceleration.BRAKE);
        } else if (distance > 0 && distance < slowDistance && car.getSpeedKMH() > slowSpeed) {
            car.setAcceleration(Acceleration.BRAKE);
            lastMovement = scenario.getTime();
        } else if (steerTowardsPath().length() == 0) {
            car.setAcceleration(Acceleration.NONE);
            lastMovement = scenario.getTime();
        } else if (speedAdjustmentToAvoidCars() < 0 && patienceTime > scenario.getTime() - lastMovement) {
            car.setAcceleration(Acceleration.BRAKE);
        } else if (car.getSpeedKMH() < maxSpeed) {
            car.setAcceleration(Acceleration.ACCELERATE);
            lastMovement = scenario.getTime();
        }
        else {
            car.setAcceleration(Acceleration.NONE);
            lastMovement = scenario.getTime();
        }
    }
}
