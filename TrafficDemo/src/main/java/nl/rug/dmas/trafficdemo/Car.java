package nl.rug.dmas.trafficdemo;

import nl.rug.dmas.trafficdemo.actors.Driver;
import java.awt.Color;
import java.util.ArrayList;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * A simulation of a car with a physical body, wheels and a metaphysical driver.
 * Based on http://www.iforce2d.net/b2dtut/top-down-car
 * and http://www.iforce2d.net/src/iforce2d_TopdownCar.h
 * @author jelmer
 */
public class Car {
    final float maxSteerAngleDeg = 40;
    final float power = 30;
    final float brakePower = 50;
    final float steeringSpeed = 5f;
    
    private final float width;
    private final float length;
    final Color color;

    final Driver driver;
    
    Acceleration acceleration =  Acceleration.NONE;

    float targetBodyAngle = 0f;
    float wheelAngleDeg = 0;

    Body body;
    ArrayList<Wheel> wheels;

    Fixture bodyFixture;
    Fixture visionFixture;

    private final Vec2 initialPosition;
    private final float initialAngle;

    public Car(Driver driver, float width, float length, Vec2 position, float angle) {
        this.driver = driver;
        this.width = width;
        this.length = length;
        this.initialPosition = position;
        this.initialAngle = angle;
        this.color = RandomUtil.nextRandomPastelColor();

        // Let the driver know which car to steer.
        driver.setCar(this);
    }

    public void initialize(World world) {
        // The body is the 'physics body', more or less a group of fixtures
        // (actual shapes) that together form one physical unit.
        BodyDef def = new BodyDef();
        def.type = BodyType.DYNAMIC;
        def.position = initialPosition;
        def.angle = initialAngle;
        def.linearDamping = 0.5f; // gradually reduces velocity, makes the car reduce speed slowly if neither accelerator nor brake is pressed
        def.angularDamping = 0.3f;
        def.bullet = true;  //dedicates more time to collision detection - car travelling at high speeds at low framerates otherwise might teleport through obstacles.
        body = world.createBody(def);

        // Let's also create a fixture (a solid part) for our body
        bodyFixture = body.createFixture(getBodyDef());
        bodyFixture.setUserData(this);

        // And, let's create a sensor for the car's vision (and give it the driver as data)
        visionFixture = body.createFixture(getFOVDef());
        visionFixture.setUserData(driver);

        // Finally, add some wheels.
        wheels = createWheels(world);
    }

    private double computeWheelBase() {
        double frontAxis = Double.NEGATIVE_INFINITY;
        double backAxis = Double.POSITIVE_INFINITY;
        for (Wheel wheel : this.wheels) {
            // Todo Baakman are you mixing Y and X here intentionally?
            frontAxis = Math.max(frontAxis, wheel.position.y);
            backAxis = Math.min(backAxis, wheel.position.x);
        }
        return frontAxis - backAxis;
    }

    /**
     * Compute the turning circle of a car according to this page:
     * https://goodmaths.wordpress.com/2013/07/19/turning-radius-of-a-car/
     *
     * @return turning circle of the car in meters.
     */
    public double computeTurningCircle() {
        double wheelBase = computeWheelBase();
        return wheelBase / (2 * Math.sin(this.wheelAngleDeg));
    }

    /**
     * Destroys the internal representation of a car. This method is unsafe! Do
     * not call it during
     *
     * @param world of the scenario
     */
    public void destroy(World world) {
        world.destroyBody(body);
        
        for (Wheel wheel : wheels) {
            world.destroyBody(wheel.body);
        }
    }

    /**
     * Get the driver driving this car. You should probably be careful with
     * using this method, because it is not really realistic to have access to
     * other drivers their innards. However, sometimes you just want to ask
     * the driver whether they have reached their destination.
     * @return current driver
     */
    public Driver getDriver() {
        return driver;
    }
    
    /**
     * Creates a Fixture recipe based on the width and length of the vehicle.
     *
     * @return a FixtureDef for the collision body
     */
    protected FixtureDef getBodyDef() {
        FixtureDef fixDef = new FixtureDef();
        fixDef.density = 1.0f;
        fixDef.friction = 0.3f; //friction when rubbing agaisnt other shapes
        fixDef.restitution = 0.4f;//amount of force feedback when hitting something. >0 makes the car bounce off, it's fun!

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(getWidth() / 2 , getLength() / 2);
        fixDef.shape = shape;

        return fixDef;
    }

    /**
     * Creates a Fixture recipe based on the shape of the FOV of the driver
     *
     * @return a FixtureDef for the vision sensor
     */
    protected FixtureDef getFOVDef() {
        FixtureDef visionDef = new FixtureDef();
        visionDef.shape = driver.getFOVShape();
        visionDef.isSensor = true;
        return visionDef;
    }

    /**
     * Positions wheels at the front and the back based on the width and length
     * of the vehicle.
     *
     * @param world in which to create the wheels
     * @return a list of wheels
     */
    protected ArrayList<Wheel> createWheels(World world) {
        float axisOffset = getLength() / 5f;
        ArrayList<Wheel> wheels = new ArrayList<>();
        wheels.add(new Wheel(world, this, new Vec2(getWidth() / -2f, getLength() / -2f + axisOffset), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top left
        wheels.add(new Wheel(world, this, new Vec2(getWidth() / -2f, getLength() /  2f - axisOffset), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom left
        wheels.add(new Wheel(world, this, new Vec2(getWidth() /  2f, getLength() / -2f + axisOffset), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top right
        wheels.add(new Wheel(world, this, new Vec2(getWidth() /  2f, getLength() /  2f - axisOffset), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom right
        return wheels;
    }
    
    public Vec2 getAbsoluteVelocity() {
        return this.body.getLinearVelocityFromLocalPoint(new Vec2(0, 0));
    }

    /**
     * Get the local velocity of the car. This is equal to the actual speed of
     * the car itself, but it also gives you the direction of that velocity.
     *
     * @return velocity of car
     */
    public Vec2 getLocalVelocity() {
        return this.body.getLocalVector(getAbsoluteVelocity());
    }

    /**
     * Translate a point in the world to a position as seen from the car. E.g. a
     * point in front of the car will be at Vec2(0, 1).
     *
     * @param worldPoint point in the world
     * @return the same point but then in car space.
     */
    public Vec2 getLocalPoint(Vec2 worldPoint) {
        return this.body.getLocalPoint(worldPoint);
    }

    /**
     * Position of the center of the car in world space.
     *
     * @return car position in world space.
     */
    public Vec2 getPosition() {
        return this.body.getWorldCenter();
    }

    /**
     * Get the local velocity in speed.
     *
     * @return speed of car
     */
    public float getSpeedKMH() {
        return (getLocalVelocity().length() / 1000) * 3600;
    }

    /**
     * Set the speed of the body of the car. This alters the velocity the car
     * already has gained. To change the velocity of the car in a realistic way,
     * use steering and acceleration.
     *
     * @param speedInKMH
     */
    private void setSpeed(float speedInKMH) {
        Vec2 velocity = getLocalVelocity();
        velocity.normalize();
        this.body.setLinearVelocity(velocity.mul((speedInKMH * 1000) / 3600f));
    }

    /**
     * Set the steering direction. The angle is relative to the angle of the
     * body. So if you steer 90deg and your car is pointed east, you are
     * steering south.
     *
     * @param angle relative angle in degrees
     */
    public void setSteeringDirection(float angle) {
        targetBodyAngle = body.getAngle() + angle * MathUtils.DEG2RAD;
    }

    /**
     * Set the steering direction. Direction as a vector relative to the angle
     * of the car. Really, it just calculates the angle of the vector and then
     * calls setSteeringDirection with that angle.
     *
     * @param direction as a vector relative to the body of the car.
     */
    public void setSteeringDirection(Vec2 direction) {
        direction = direction.clone();
        direction.normalize();
        setSteeringDirection((MathUtils.atan2(direction.y, direction.x) - MathUtils.HALF_PI) * MathUtils.RAD2DEG);
    }

    /**
     * Set the pedal the driver is pushing
     *
     * @param acceleration Gas! Brake! Reverse?
     */
    public void setAcceleration(Acceleration acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Updates the simulation of the car. Typically called indirectly by
     * Scenario.update(dt).
     *
     * @param dt delta time in seconds.
     */
    public void update(float dt) {
        // Kill sideway velocity
        for (Wheel wheel : wheels) {
            wheel.killSidewaysVelocity();
        }

        // Set wheel angle
        // Calculate the change in wheel's angle for this update, assuming the wheel will reach is maximum angle from zero in 200 ms
        //float increase = maxSteerAngleDeg * dt * steeringSpeed;
        float steerAngle = (targetBodyAngle - body.getAngle()) * MathUtils.RAD2DEG % 360;

        // I don't know why, but this prevents the car from steering left when
        // the target is on its right side (but the angle is larger than 90deg
        // to the right)
        if (steerAngle < -180) {
            steerAngle = 360 - steerAngle;
        }

        // Todo: incorporate increase and steeringSpeed into this very simple hack that controls steering
        wheelAngleDeg = MathUtils.clamp(steerAngle, -maxSteerAngleDeg, maxSteerAngleDeg);

        // Apply force to wheels
        Vec2 forceVec = new Vec2(0, 0); //vector pointing in the direction force will be applied to a wheel ; relative to the wheel.

        switch (this.acceleration) {
            case ACCELERATE:
                forceVec = new Vec2(0, -1).mul(power); // Note: this represents the engine power!
                break;

            case BRAKE:
                if (getLocalVelocity().y < 0)
                    forceVec = new Vec2(0, 1).mul(brakePower);
                break;
            
            case REVERSE:
                forceVec = new Vec2(0, 1).mul(power);
                break;
        }

        // apply force and steering to each wheel
        // Assume the powered wheels are the first two wheels
        for (Wheel wheel : wheels) { // Update revolving wheels
            if (wheel.revolving == Joint.REVOLVING) {
                wheel.setAngleDeg(wheelAngleDeg);
            }
            if (wheel.powered == Power.POWERED) {
                wheel.body.applyForceToCenter(wheel.body.getWorldVector(forceVec));
            }
        }

        //if going very slow, stop - to prevent endless sliding
        if (getSpeedKMH() < 4 && acceleration == Acceleration.NONE) {
            setSpeed(0);
        }
    }

    /**
     * @return the width of the car in meters
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return the length of the car in meters
     */
    public float getLength() {
        return length;
    }
}
