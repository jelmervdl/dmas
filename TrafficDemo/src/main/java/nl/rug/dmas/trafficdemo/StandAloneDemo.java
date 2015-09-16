/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

/**
 *
 * @author jelmer
 */
public class StandAloneDemo {
    
    static class TrafficPanel extends JPanel {
        Scenario scenario;
        
        public TrafficPanel(Scenario scenarion) {
            this.scenario = scenarion;
        }
        
        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            
            // This paint method gets called indirectly every 1/60th of a second
            // by the mainLoop which issues a 'repaint()' request. (AWT then
            // decides on when to do the actual painting, and at that moment this
            // method is called.)
            
            // World position Vec2(0,0) is the center of the screen
            // Scale translates one world point to n pixels.
            
            float scale = 10f;
            Point center = new Point(getSize().width / 2, getSize().height / 2);
            
            // First we should draw (or blit, that would be awesome fast!) the
            // roads. But there are no roads yet.
            
            // Then on top of those, we draw our cars.
            for (Car car : scenario.cars) {
                drawCar(g2, car, center, scale);
            }
        }
        
        private void drawCar(Graphics2D g2, Car car, Point offset, float scale) {
            // For now this just draws the polygon of the physics body shape.
            // We might want to change this to our own polygon calculation based
            // on the car.body.getPosition() and car.body.getAngle() so we can
            // draw stuff like light and a windscreen to make the car identifyable.
            
            // Get me some wheels (draw them first because they are below)
            for (Wheel wheel : car.wheels) {
                g2.setColor(Color.BLACK);
                drawBody(g2, wheel.body, offset, scale);
            }
            
            // Then draw the body of the car
            g2.setColor(Color.RED);
            drawBody(g2, car.body, offset, scale);
        }
        
        private void drawBody(Graphics2D g2, Body body, Point offset, float scale) {
            PolygonShape poly = (PolygonShape) body.getFixtureList().getShape();
            int vertexCount = poly.m_count;
            int[] xs = new int[vertexCount];
            int[] ys = new int[vertexCount];

            Vec2 vertex = new Vec2();
            for (int i = 0; i < vertexCount; ++i) {
                Transform.mulToOutUnsafe(body.getTransform(), poly.m_vertices[i], vertex);
                xs[i] = Math.round(vertex.x * scale + offset.x);
                ys[i] = Math.round(vertex.y * scale + offset.y);
            }

            g2.fillPolygon(xs, ys, vertexCount);
        }
    }
    
    static public void main(String[] args) {
        final int hz = 60; // 60 fps
        
        // Create a world without gravity (2d world seen from top, eh!)
        // The world is our physics simulation.
        final World world = new World(new Vec2(0, 0));
        
        // Create a scenario with two cars looping left and right (and colliiiddiiingg >:D )
        final Scenario scenario = new Scenario(world);
        scenario.cars.add(new Car(world, 2, 4, new Vec2(1, 0)));
        scenario.cars.get(0).acceleration = Acceleration.ACCELERATE;
        scenario.cars.get(0).steer = SteerDirection.RIGHT;
        
        scenario.cars.add(new Car(world, 2, 4, new Vec2(-1, 0)));
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
                        
                        scenario.step(stepTime);
                        world.step(stepTime, 3, 8);
                        panel.repaint(); // todo: only repaint if the cars moved?
                        
                        long finishTimeMS = System.currentTimeMillis();
                        long sleepTimeMS = (long) (stepTime * 1000) - (finishTimeMS - startTimeMS);
                        if (sleepTimeMS > 0)
                            Thread.sleep(sleepTimeMS);
                    }
                }
                catch (InterruptedException e) {
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
