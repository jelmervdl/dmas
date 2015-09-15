/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author jelmer
 */
public class Car {
    SteerDirection steer = SteerDirection.NONE;
    Acceleration acceleration =  Acceleration.NONE;

    float maxSteerAngleDeg = 40;
    float maxSpeedKMH = 60;
    float power = 250;
    float wheelAngleDeg = 0;

    float width;
    float length;

    Body body;
    ArrayList<Wheel> wheels;

    public Car(World world, float width, float length, Vec2 position) {
        this.width = width;
        this.length = length;

        BodyDef def = new BodyDef();
        def.type = BodyType.DYNAMIC;
        def.position = position;
        def.angle = 0;
        def.linearDamping = 0.5f; // gradually reduces velocity, makes the car reduce speed slowly if neither accelerator nor brake is pressed
        def.angularDamping = 0.3f;
        def.bullet = true;  //dedicates more time to collision detection - car travelling at high speeds at low framerates otherwise might teleport through obstacles.
        body = world.createBody(def);

        FixtureDef fixDef = new FixtureDef();
        fixDef.density = 1.0f;
        fixDef.friction = 0.3f; //friction when rubbing agaisnt other shapes
        fixDef.restitution = 0.4f;//amount of force feedback when hitting something. >0 makes the car bounce off, it's fun!
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2 , length / 2);
        fixDef.shape = shape;
        body.createFixture(fixDef);

        wheels = new ArrayList<>();
        wheels.add(new Wheel(world, this, new Vec2(-1f, -1.2f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top left
        wheels.add(new Wheel(world, this, new Vec2(-1f,  1.2f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom left
        wheels.add(new Wheel(world, this, new Vec2( 1f, -1.2f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top right
        wheels.add(new Wheel(world, this, new Vec2( 1f,  1.2f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom right
    }

    public Vec2 getLocalVelocity() {
        return this.body.getLocalVector(this.body.getLinearVelocityFromLocalPoint(new Vec2(0, 0)));
    }

    public float getSpeedInKMH() {
        return (getLocalVelocity().length() / 1000) * 3600;
    }

    public void setSpeed(float speed) {
        Vec2 velocity = getLocalVelocity();
        velocity.normalize();
        this.body.setLinearVelocity(velocity.mul((speed * 1000) / 3600f));
    }

    public void update(int duration) {
        // Kill sideway velocity
        for (Wheel wheel : wheels)
            wheel.killSidewaysVelocity();

        // Set wheel angle

        // Calculate the change in wheel's angle for this update, assuming the wheel will reach is maximum angle from zero in 200 ms
        float increase = (this.maxSteerAngleDeg / 200f) * duration;

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

        // Update revolving wheels
        // This assumes the first two wheels are the revolving wheels
        for (int i = 0; i < 2; ++i)
            wheels.get(i).setAngleDeg(this.wheelAngleDeg);

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

        // apply force to each wheel
        // Assume the powered wheels are the first two wheels
        for (int i = 0; i < 2; ++i) {
            Body wheelBody = wheels.get(i).body;
            wheelBody.applyForce(wheelBody.getWorldVector(forceVec), wheelBody.getWorldCenter());
        }

        //if going very slow, stop - to prevent endless sliding
        if (getSpeedInKMH() < 4 && acceleration == Acceleration.NONE)
            setSpeed(0);
    }
}