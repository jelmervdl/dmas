/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Color;
import java.util.ArrayList;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * A simulation of a car with a physical body, wheels and a metaphysical driver.
 * @author jelmer
 */
public class Car {
    Driver driver;
    
    SteerDirection steer = SteerDirection.NONE;
    Acceleration acceleration =  Acceleration.NONE;

    float maxSteerAngleDeg = 40;
    float maxSpeedKMH = 60;
    float power = 250;
    float wheelAngleDeg = 0;
    float steeringSpeed = 5f;
    float visionRange = 8f;

    float width;
    float length;
    Color color;

    Body body;
    ArrayList<Wheel> wheels;
    
    Fixture bodyFixture;
    Fixture visionFixture;
    
    private Vec2 initialPosition;
    
    public Car(Driver driver, float width, float length, Vec2 position) {
        this.driver = driver;
        this.width = width;
        this.length = length;
        this.initialPosition = position;
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
        def.angle = 0;
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
    
    /**
     * Destroys the internal representation of a car. This method is unsafe! Do
     * not call it during 
     */
    public void destroy(World world) {
        world.destroyBody(body);
    }
    
    /**
     * Creates a Fixture recipe based on the width and length of the vehicle.
     * @return a FixtureDef for the collision body
     */
    protected FixtureDef getBodyDef() {
        FixtureDef fixDef = new FixtureDef();
        fixDef.density = 1.0f;
        fixDef.friction = 0.3f; //friction when rubbing agaisnt other shapes
        fixDef.restitution = 0.4f;//amount of force feedback when hitting something. >0 makes the car bounce off, it's fun!
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 , length / 2);
        fixDef.shape = shape;
        
        return fixDef;
    }
    
    /**
     * Creates a Fixture recipe based on the shape of the FOV of the driver
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
     * @return a list of wheels
     */
    protected ArrayList<Wheel> createWheels(World world) {
        ArrayList<Wheel> wheels = new ArrayList<>();
        wheels.add(new Wheel(world, this, new Vec2(width / -2f, length / -2f + 0.8f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top left
        wheels.add(new Wheel(world, this, new Vec2(width / -2f, length /  2f - 0.8f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom left
        wheels.add(new Wheel(world, this, new Vec2(width /  2f, length / -2f + 0.8f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top right
        wheels.add(new Wheel(world, this, new Vec2(width /  2f, length /  2f - 0.8f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom right
        return wheels;
    }

    /**
     * Get the local velocity of the car. This is equal to the actual speed of
     * the car itself, but it also gives you the direction of that velocity.
     * @return velocity of car
     */
    public Vec2 getLocalVelocity() {
        return this.body.getLocalVector(this.body.getLinearVelocityFromLocalPoint(new Vec2(0, 0)));
    }

    /**
     * Get the local velocity in speed.
     * @return speed of car
     */
    public float getSpeedInKMH() {
        return (getLocalVelocity().length() / 1000) * 3600;
    }

    /**
     * Set the speed of the body of the car. This alters the velocity the car
     * already has gained. To change the velocity of the car in a realistic way,
     * use steering and acceleration.
     * @param speedInKMH 
     */
    public void setSpeed(float speedInKMH) {
        Vec2 velocity = getLocalVelocity();
        velocity.normalize();
        this.body.setLinearVelocity(velocity.mul((speedInKMH * 1000) / 3600f));
    }
    
    /**
     * Returns the shape of the field of view the car has. Any other fixture in
     * the world that is positioned inside this shape will be available through
     * the fixturesInSight set of the driver.
     * @return a JBox2D shape representing the FOV
     */
    public Shape getSight()
    {
        CircleShape sight = new CircleShape();
        sight.setRadius(5);
        return sight;
    }

    /**
     * Updates the simulation of the car. Typically called indirectly by
     * Scenario.update(dt).
     * @param dt delta time in seconds.
     */
    public void update(float dt) {
        // Kill sideway velocity
        for (Wheel wheel : wheels)
            wheel.killSidewaysVelocity();

        // Set wheel angle

        // Calculate the change in wheel's angle for this update, assuming the wheel will reach is maximum angle from zero in 200 ms
        float increase = this.maxSteerAngleDeg * dt * this.steeringSpeed;

        switch (this.steer) {
            case RIGHT:
                this.wheelAngleDeg = Math.min(this.wheelAngleDeg + increase, this.maxSteerAngleDeg);
                break;

            case LEFT:
                this.wheelAngleDeg = Math.max(this.wheelAngleDeg - increase, -this.maxSteerAngleDeg);
                break;

            case NONE:
            default:
                if (this.wheelAngleDeg > 0)
                    this.wheelAngleDeg = Math.max(this.wheelAngleDeg - increase, 0);
                else if (this.wheelAngleDeg < 0)
                    this.wheelAngleDeg = Math.min(this.wheelAngleDeg + increase, 0);
                break;
        }

        // Apply force to wheels
        Vec2 baseVec = new Vec2(0, 0); //vector pointing in the direction force will be applied to a wheel ; relative to the wheel.

        switch (this.acceleration) {
            case ACCELERATE:
                if (this.getSpeedInKMH() < this.maxSpeedKMH)
                    baseVec = new Vec2(0, -1);
                break;

            case BRAKE:
                // braking, but still moving forwards - increased force
                if (getLocalVelocity().y < 0)
                    baseVec = new Vec2(0, 1.3f);
                //going in reverse - less force
                else
                    baseVec = new Vec2(0, 0.7f);
                break;
        }

        // multiply by engine power, which gives us a force vector relative to the wheel
        Vec2 forceVec = baseVec.mul(power);
        
        // apply force and steering to each wheel
        // Assume the powered wheels are the first two wheels
        for (Wheel wheel : wheels) { // Update revolving wheels
            if (wheel.revolving == Joint.REVOLVING)
                wheel.setAngleDeg(this.wheelAngleDeg);
            
            if (wheel.powered == Power.POWERED)
                wheel.body.applyForce(wheel.body.getWorldVector(forceVec), wheel.body.getWorldCenter());
        }

        //if going very slow, stop - to prevent endless sliding
        if (getSpeedInKMH() < 4 && acceleration == Acceleration.NONE)
            setSpeed(0);
    }
}