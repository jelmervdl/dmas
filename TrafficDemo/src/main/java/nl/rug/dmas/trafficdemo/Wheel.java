/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

/**
 * A car wheel, to be attached to (and instantiated by) a Car. A wheel can be
 * revolving (meaning it is used for steering) and powered (meaning it is used
 * for gaining velocity).
 * @author jelmer
 */
public class Wheel {
    Car car;
    Joint revolving;
    Power powered;

    Body body;
    Vec2 position;

    /**
     * Create a wheel
     * @param world physics simulation in which the wheel is created
     * @param car car to which the wheel is attached
     * @param position of the wheel relative to the center of the car
     * @param width of the wheel
     * @param length (or radius) of the wheel
     * @param joint type of joint, either fixed or revolving
     * @param power type of axel, either powered or not powered.
     */
    public Wheel(World world, Car car, Vec2 position, float width, float length, Joint joint, Power power) {
        this.car = car;
        this.position = position;
        this.revolving = joint;
        this.powered = power;

        // Initialize body
        BodyDef def = new BodyDef();
        def.type = BodyType.DYNAMIC;
        def.position = car.body.getWorldPoint(position);
        def.angle = car.body.getAngle();
        this.body = world.createBody(def);

        // Initialize shape
        FixtureDef fixDef = new FixtureDef();
        fixDef.density = 1f;
        fixDef.isSensor = true; //wheel does not participate in collision calculations: resulting complications are unnecessary

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, length / 2);
        fixDef.shape = shape;
        this.body.createFixture(fixDef);

        // Create joint to connect wheel to body
        if (revolving == Joint.REVOLVING) {
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.initialize(car.body, this.body, this.body.getWorldCenter());
            jointDef.enableMotor = false; // we'll be controlling the wheel's angle manually
            world.createJoint(jointDef);
        } else {
            PrismaticJointDef jointDef = new PrismaticJointDef();
            jointDef.initialize(car.body, this.body, this.body.getWorldCenter(), new Vec2(1, 0));
            jointDef.enableLimit = true;
            jointDef.lowerTranslation = 0;
            jointDef.upperTranslation = 0;
            world.createJoint(jointDef);
        }
    }

    /**
     * Set the angle of the wheel relative to the body of the car.
     * @param angle in degrees
     */
    public void setAngleDeg(float angle) {
        this.body.setTransform(this.body.getPosition(), this.car.body.getAngle() + (float) ((angle / 180f) * Math.PI));
    }

    public Vec2 getLocalVelocity() {
        return this.car.body.getLocalVector(this.car.body.getLinearVelocityFromLocalPoint(this.position));
    }

    /**
     * Returns a world unit vector pointing in the direction this wheel is moving
     * @return
     */
    public Vec2 getDirectionVector() {
        //return rotate new Vec2((-?)1,0) by this.body.angle() depending on moving forward or backward, namely getLocalVelocity().x
        return VecUtils.rotate(new Vec2(-body.getMass(), 0), body.getAngle());
    }

    /**
     * Subtracts sideways velocity from this wheel its velocity vector and returns the remaining front-facing velocity vector
     * @return
     */
    private Vec2 getLateralVelocity() {
        // Take the normal on the right side of the car
        Vec2 currentRightNormal = body.getWorldVector(new Vec2(1, 0));
        
        // Now take the velocity of the body, and use the dot product to get the component of
        // the velocity in the direction of the normal.
        return currentRightNormal.mul(Vec2.dot(currentRightNormal, body.getLinearVelocity()));
    }

    /**
     * Removes all sideways velocity from this wheels velocity
     */
    void killSidewaysVelocity() {
        Vec2 impulse = getLateralVelocity().negate().mul(body.getMass());
        this.body.applyLinearImpulse(impulse, body.getWorldCenter());
    }
}
