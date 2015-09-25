/*
 * Documentation on JBox2D can be found at:
 * http://trentcoder.github.io/JBox2D_JavaDoc/apidocs/
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.jbox2d.common.Vec2;

/**
 *
 * @author jelmer
 */
public class TrafficDemo {
    static final int hz = 60; // 60 fps
    
    static final int numberOfCars = 1;

    static public void main(String[] args) {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Could not initialize awesome Mac-specific UI, but Java UI will be sort of fine, I guess.
        }
        
        final Preferences prefs = Preferences.userNodeForPackage(TrafficDemo.class);
        
        // Create a scenario with two cars looping left and right (and colliiiddiiingg >:D )
        final Scenario scenario = new Scenario();
        scenario.commonKnowledge.put("mouse", new Vec2(0, 0));
        scenario.commonKnowledge.put("path", new CopyOnWriteArrayList<Vec2>());
        
        for (int i = 0; i < numberOfCars; ++i) {
            scenario.add(new Car(new Driver(scenario), 2, 4, RandomUtil.nextRandomVec(-10, 10, -10, 10)));
        }

        // Pony up a simple window, our only entrypoint to the app
        JFrame window = new JFrame();
        window.setTitle("Traffic!");
        window.setSize(500, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // The TrafficPanel draws the actual scenario (cars etc.)
        final TrafficPanel panel = new TrafficPanel(scenario);
        panel.drawFOV = prefs.getBoolean("drawFOV", panel.drawFOV); // prefer user stored preference
        panel.drawDirection = prefs.getBoolean("drawDirection", panel.drawDirection);
        window.add(panel);
        
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                CopyOnWriteArrayList<Vec2> path = (CopyOnWriteArrayList<Vec2>) scenario.commonKnowledge.get("path");
                path.add(panel.getMouseWorldLocation());
            }
        });
        
        // Add a menu bar for some configuration toggles
        JMenuBar menuBar = new JMenuBar();
        window.setJMenuBar(menuBar);
        
        JMenu simulationMenu = new JMenu("Simulation");
        menuBar.add(simulationMenu);
        
        final JMenuItem addCar = new JMenuItem("Add a car");
        simulationMenu.add(addCar);
        addCar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        addCar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scenario.add(new Car(new Driver(scenario), 2, 4, RandomUtil.nextRandomVec(-10, 10, -10, 10)));
            }
        });
        
        final JMenuItem removeCar = new JMenuItem("Remove a car");
        simulationMenu.add(removeCar);
        removeCar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        removeCar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random generator = new Random();
                Car car = scenario.cars.get(generator.nextInt(scenario.cars.size()));
                scenario.remove(car);
            }
        });
        
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        
        final JCheckBoxMenuItem drawFOV = new JCheckBoxMenuItem("Show Field of View", panel.drawFOV);
        viewMenu.add(drawFOV);
        drawFOV.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawFOV = drawFOV.isSelected();
                prefs.putBoolean("drawFOV", panel.drawFOV);
            }
        });
        
        final JCheckBoxMenuItem drawDirection = new JCheckBoxMenuItem("Show Direction", panel.drawDirection);
        viewMenu.add(drawDirection);
        drawDirection.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                panel.drawDirection = drawDirection.isSelected();
                prefs.putBoolean("drawDirection", panel.drawDirection);
            }
        });
        
        // Run our main loop in another thread. This one updates the scenario
        // which in turn will update the cars (our agent drivers).
        // It also updates the 'world', which is our physics simulation.
        // Once finished, it sleeps until the next frame.
        // Optional todo: replace this with a scheduled repeating executor
        // so we don't have to deal with the timing of the thread sleep?
        // Other todo: SWING is not thread-safe, so we might need to add some
        // checks that we are not painting a world that is currenly updating and
        // inconsistent.
        final Thread mainLoop = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    float stepTime = 1.0f / (float) hz;

                    while (!Thread.interrupted()) {
                        long startTimeMS = System.currentTimeMillis();

                        scenario.commonKnowledge.put("mouse", panel.getMouseWorldLocation());
                        scenario.step(stepTime);
                        
                        panel.repaint();

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
