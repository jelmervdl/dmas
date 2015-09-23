/*
 * Documentation on JBox2D can be found at:
 * http://trentcoder.github.io/JBox2D_JavaDoc/apidocs/
 */
package nl.rug.dmas.trafficdemo;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

/**
 *
 * @author jelmer
 */
public class TrafficDemo {

    static public void main(String[] args) {
        final int hz = 60; // 60 fps

        // Create a world without gravity (2d world seen from top, eh!)
        // The world is our physics simulation.
        final World world = new World(new Vec2(0, 0));

        // Create a scenario with two cars looping left and right (and colliiiddiiingg >:D )
        final Scenario scenario = new Scenario(world);
        scenario.cars.add(new Car(scenario, new Driver(scenario), 2, 4, new Vec2(5, 0)));
        scenario.cars.get(0).acceleration = Acceleration.ACCELERATE;
        scenario.cars.get(0).steer = SteerDirection.RIGHT;
        scenario.cars.add(new Car(scenario, new Driver(scenario), 2, 4, new Vec2(-5, 0)));
        scenario.cars.get(1).acceleration = Acceleration.ACCELERATE;
        scenario.cars.get(1).steer = SteerDirection.LEFT;

        // Pony up a simple window, our only entrypoint to the app
        JFrame window = new JFrame();
        window.setTitle("Traffic!");
        window.setSize(500, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // The TrafficPanel draws the actual scenario (cars etc.)
        final TrafficPanel panel = new TrafficPanel(scenario);
        window.add(panel);

        // Run our main loop in another thread. This one updates the scenario
        // which in turn will update the cars (our agent drivers).
        // It also updates the 'world', which is our physics simulation.
        // Once finished, it sleeps until the next frame.
        // Optional todo: replace this with a scheduled repeating executor
        // so we don't have to deal with the timing of the thread sleep?
        final Thread mainLoop = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    float stepTime = 1.0f / (float) hz;

                    while (!Thread.interrupted()) {
                        long startTimeMS = System.currentTimeMillis();

                        scenario.commonKnowledge.put("mouse", panel.getMouseWorldLocation());
                        scenario.step(stepTime);
                        
                        world.step(stepTime, 3, 8);
                        panel.repaint(); // todo: only repaint if the cars moved?

                        long finishTimeMS = System.currentTimeMillis();
                        long sleepTimeMS = (long) (stepTime * 1000) - (finishTimeMS - startTimeMS);
                        if (sleepTimeMS > 0) {
                            Thread.sleep(sleepTimeMS);
                        }
                    }
                } catch (InterruptedException e) {
                    // Just stop the mainloop
                }
            }
        });

        // When the main window is closed, also stop the simulation loop
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainLoop.interrupt();
            }
        });

        // Show the window, and start the loop!
        window.setVisible(true);
        mainLoop.start();
    }
}
