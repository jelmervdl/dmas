/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision;
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
 *
 * @author jelmer
 */
public class Car {
    Scenario scenario;
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
    
    public Car(Scenario scenario, Driver driver, float width, float length, Vec2 position) {
        this.scenario = scenario;
        this.driver = driver;
        this.width = width;
        this.length = length;
        this.color = RandomUtil.nextRandomPastelColor();
        
        // Let the driver know which car to steer.
        driver.setCar(this);
        
        // The body is the 'physics body', more or less a group of fixtures
        // (actual shapes) that together form one physical unit.
        BodyDef def = new BodyDef();
        def.type = BodyType.DYNAMIC;
        def.position = position;
        def.angle = 0;
        def.linearDamping = 0.5f; // gradually reduces velocity, makes the car reduce speed slowly if neither accelerator nor brake is pressed
        def.angularDamping = 0.3f;
        def.bullet = true;  //dedicates more time to collision detection - car travelling at high speeds at low framerates otherwise might teleport through obstacles.
        body = scenario.world.createBody(def);
        
        // Let's also create a fixture (a solid part) for our body
        bodyFixture = body.createFixture(getBodyDef());
        bodyFixture.setUserData(this);
        
        // And, let's create a sensor for the car's vision (and give it the driver as data)
        visionFixture = body.createFixture(getFOVDef());
        visionFixture.setUserData(driver);

        // Finally, add some wheels.
        wheels = createWheels();
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
     * of the vehilce.
     * @return a list of wheels
     */
    protected ArrayList<Wheel> createWheels() {
        ArrayList<Wheel> wheels = new ArrayList<>();
        wheels.add(new Wheel(scenario.world, this, new Vec2(width / -2f, length / -2f + 0.8f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top left
        wheels.add(new Wheel(scenario.world, this, new Vec2(width / -2f, length /  2f - 0.8f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom left
        wheels.add(new Wheel(scenario.world, this, new Vec2(width /  2f, length / -2f + 0.8f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top right
        wheels.add(new Wheel(scenario.world, this, new Vec2(width /  2f, length /  2f - 0.8f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom right
        return wheels;
    }

    public Vec2 getLocalVelocity() {
        return this.body.getLocalVector(this.body.getLinearVelocityFromLocalPoint(new Vec2(0, 0)));
    }

    public float getSpeedInKMH() {
        return (getLocalVelocity().length() / 1000) * 3600;
    }

    public void setSpeed(float speedInKMH) {
        Vec2 velocity = getLocalVelocity();
        velocity.normalize();
        this.body.setLinearVelocity(velocity.mul((speedInKMH * 1000) / 3600f));
    }
    
    /**
     * Returns sight of the car in world space coordinates
     * @return 
     */
    public Shape getSight()
    {
        CircleShape sight = new CircleShape();
        sight.setRadius(5);
        return sight;
    }

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