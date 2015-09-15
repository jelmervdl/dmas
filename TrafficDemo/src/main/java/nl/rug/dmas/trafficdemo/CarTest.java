/**
 * This is a rather literal translation of
 * https://github.com/domasx2/gamejs-box2d-car-example/blob/master/javascript/main.js
 * to Java.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

enum SteerDirection {
    NONE, LEFT, RIGHT
}

enum Acceleration {
    NONE, ACCELERATE, BRAKE
}

enum Joint {
    REVOLVING, FIXED
}

enum Power {
    POWERED, UNPOWERED
}

/**
 *
 * @author jelmer
 */
public class CarTest extends TestbedTest {
    protected class Wheel {
        Car car;
        Joint revolving;
        Power powered;
        
        Body body;
        Vec2 position;
        
        public Wheel(Car car, Vec2 position, float width, float length, Joint joint, Power power) {
            this.car = car;
            this.position = position;
            this.revolving = joint;
            this.powered = power;
            
            // Initialize body
            BodyDef def = new BodyDef();
            def.type = BodyType.DYNAMIC;
            def.position = car.body.getWorldPoint(position);
            def.angle = car.body.getAngle();
            this.body = getWorld().createBody(def);
            
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
                getWorld().createJoint(jointDef);
            } else {
                PrismaticJointDef jointDef = new PrismaticJointDef();
                jointDef.initialize(car.body, this.body, this.body.getWorldCenter(), new Vec2(1, 0));
                jointDef.enableLimit = true;
                jointDef.lowerTranslation = 0;
                jointDef.upperTranslation = 0;
                getWorld().createJoint(jointDef);
            }
        }
        
        public void setAngleDeg(float angleDelta) {
            this.body.setTransform(this.body.getPosition(), this.car.body.getAngle() + (float) ((angleDelta / 180f) * Math.PI));
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
            return new Vec2();
        }
        
        /**
         * Subtracts sideways velocity from this wheel its velocity vector and returns the remaining front-facing velocity vector
         * @return
         */
        public Vec2 getKillVelocityVector() {
            /*
            var velocity=this.body.GetLinearVelocity();
            var sideways_axis=this.getDirectionVector();
            var dotprod=vectors.dot([velocity.x, velocity.y], sideways_axis);
            return [sideways_axis[0]*dotprod, sideways_axis[1]*dotprod];
            */
            return new Vec2();
        }
        
        /**
         * Removes all sideways velocity from this wheels velocity
         */
        public void killSidewaysVelocity() {
            this.body.setLinearVelocity(getKillVelocityVector());
        }
    }
    
    protected class Car {
        SteerDirection steer = SteerDirection.NONE;
        Acceleration acceleration =  Acceleration.NONE;
        
        float maxSteerAngleDeg = 20;
        float maxSpeedKMH = 60;
        float power = 60;
        float wheelAngleDeg = 0;
        
        float width;
        float length;
        
        Body body;
        ArrayList<Wheel> wheels;
        
        public Car(float width, float length) {
            this.width = width;
            this.length = length;
            
            BodyDef def = new BodyDef();
            def.type = BodyType.DYNAMIC;
            def.position = new Vec2(10, 10);
            def.angle = 0;
            def.linearDamping = 0.5f; // gradually reduces velocity, makes the car reduce speed slowly if neither accelerator nor brake is pressed
            def.angularDamping = 0.3f;
            def.bullet = true;  //dedicates more time to collision detection - car travelling at high speeds at low framerates otherwise might teleport through obstacles.
            body = getWorld().createBody(def);
            
            FixtureDef fixDef = new FixtureDef();
            fixDef.density = 1.0f;
            fixDef.friction = 0.3f; //friction when rubbing agaisnt other shapes
            fixDef.restitution = 0.4f;//amount of force feedback when hitting something. >0 makes the car bounce off, it's fun!
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(width / 2 , length / 2);
            fixDef.shape = shape;
            body.createFixture(fixDef);
            
            wheels = new ArrayList<>();
            wheels.add(new Wheel(this, new Vec2(-1f, -1.2f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top left
            wheels.add(new Wheel(this, new Vec2( 1f, -1.2f), 0.4f, 0.8f, Joint.REVOLVING, Power.POWERED)); // top right
            wheels.add(new Wheel(this, new Vec2(-1f,  1.2f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom left
            wheels.add(new Wheel(this, new Vec2( 1f,  1.2f), 0.4f, 0.8f, Joint.FIXED, Power.UNPOWERED)); // bottom right
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
                    this.wheelAngleDeg = Math.min(Math.max(this.wheelAngleDeg, 0) + increase, this.maxSteerAngleDeg);
                    break;
                    
                case LEFT:
                    this.wheelAngleDeg = Math.max(Math.min(this.wheelAngleDeg, 0) - increase, -this.maxSteerAngleDeg);
                    break;
                   
                case NONE:
                default:
                    this.wheelAngleDeg = 0;
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
    
    Car car;
    
    @Override
    public void initTest(boolean argDeserialized) {
        getWorld().setGravity(new Vec2());
        car = new Car(2, 4);
    }
    
    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);
        
        Vec2 worldMouse = getWorldMouse();
        Vec2 carMouse = car.body.getLocalPoint(worldMouse);
        
        if (carMouse.x < -3)
            car.steer = SteerDirection.LEFT;
        else if (carMouse.x > 3)
            car.steer = SteerDirection.RIGHT;
        else
            car.steer = SteerDirection.NONE;
        
        if (carMouse.y < -2)
            car.acceleration = Acceleration.ACCELERATE;
        else if (carMouse.y > 2)
            car.acceleration = Acceleration.BRAKE;
        else
            car.acceleration = Acceleration.NONE;
        
        car.update(50);
        
        addTextLine("Steer: " + car.steer.toString());
        addTextLine("Acceleration: " + car.acceleration.toString());
        addTextLine("Speed: " + car.getSpeedInKMH() + "km/h");
    }

    @Override
    public String getTestName() {
        return "Topdown Car";
    }
}