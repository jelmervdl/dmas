/*
 * Documentation on JBox2D can be found at:
 * http://trentcoder.github.io/JBox2D_JavaDoc/apidocs/
 */
package nl.rug.dmas.trafficdemo;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

/**
 *
 * @author jelmer
 */
public class TrafficDemo {
    static final int hz = 60; // 60 fps
    
    static final int numberOfCars = 15;

    static public void main(String[] args) {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Could not initialize awesome Mac-specific UI, but Java UI will be sort of fine, I guess.
        }
        
        // Create a world without gravity (2d world seen from top, eh!)
        // The world is our physics simulation.
        final World world = new World(new Vec2(0, 0));

        // Create a scenario with two cars looping left and right (and colliiiddiiingg >:D )
        final Scenario scenario = new Scenario(world);
        for (int i = 0; i < numberOfCars; ++i) {
            scenario.cars.add(new Car(scenario, new Driver(scenario), 2, 4, RandomUtil.nextRandomVec(-10, 10, -10, 10)));
        }

        // Pony up a simple window, our only entrypoint to the app
        JFrame window = new JFrame();
        window.setTitle("Traffic!");
        window.setSize(500, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // The TrafficPanel draws the actual scenario (cars etc.)
        final TrafficPanel panel = new TrafficPanel(scenario);
        window.add(panel);
        
        JMenuBar menuBar = new JMenuBar();
        window.setJMenuBar(menuBar);
        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        
        final JCheckBoxMenuItem drawFOV = new JCheckBoxMenuItem("Show FOV", panel.drawFOV);
        viewMenu.add(drawFOV);
        drawFOV.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawFOV = drawFOV.isSelected();
            }
        });
        
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
