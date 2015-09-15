/**
 * This is a rather literal translation of
 * https://github.com/domasx2/gamejs-box2d-car-example/blob/master/javascript/main.js
 * to Java.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import java.util.Random;
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
public class CarTest extends TestbedTest
{
    ArrayList<Car> cars = new ArrayList<>();
    
    @Override
    public void initTest(boolean argDeserialized) {
        getWorld().setGravity(new Vec2());
        Random gen = new Random();
        
        for (int i = 0; i < 5; ++i) {
            float x = 10 * (gen.nextFloat() - 0.5f);
            float y = 10 * (gen.nextFloat() - 0.5f);
            cars.add(new Car(getWorld(), 2, 4, new Vec2(x, y)));
        }
    }
    
    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);
        
        Vec2 worldMouse = getWorldMouse();
        
        for (Car car : cars) {
            Vec2 carMouse = car.body.getLocalPoint(worldMouse);

            if (carMouse.x < -2)
                car.steer = SteerDirection.LEFT;
            else if (carMouse.x > 2)
                car.steer = SteerDirection.RIGHT;
            else
                car.steer = SteerDirection.NONE;

            if (carMouse.y < -2)
                car.acceleration = Acceleration.ACCELERATE;
            else if (carMouse.y > 2)
                car.acceleration = Acceleration.ACCELERATE; //Acceleration.BRAKE;
            else
                car.acceleration = Acceleration.NONE;

            car.update(10);

            addTextLine("Steer: " + car.steer.toString());
            addTextLine("Acceleration: " + car.acceleration.toString());
            addTextLine("Speed: " + car.getSpeedInKMH() + "km/h");
        }
    }

    @Override
    public String getTestName() {
        return "Topdown Car";
    }
}