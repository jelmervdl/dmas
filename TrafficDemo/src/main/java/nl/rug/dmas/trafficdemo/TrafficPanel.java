/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

/**
 * Draws cars.
 * @author jelmer
 */
public class TrafficPanel extends JPanel {

    Scenario scenario;
    float scale = 10f;
    
    // Options (for now)
    boolean drawFOV = true;

    public TrafficPanel(Scenario scenarion) {
        this.scenario = scenarion;
    }

    /**
     * Get the location of the mouse in World coordinates. I.e. the center of
     * this panel is world coordinate 0,0. Scaling is also taken into account.
     * @return Vec2 with the mouse coordinates in world space.
     */
    public Vec2 getMouseWorldLocation() {
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        Point panelLoc = getLocationOnScreen();
        int mx = mouseLoc.x - panelLoc.x;
        int my = mouseLoc.y - panelLoc.y;

        Point center = getCenter();
        float wx = (mx - center.x) / scale;
        float wy = (my - center.y) / scale;

        return new Vec2(wx, wy);
    }

    /**
     * Get the location in pixels of the center of the panel, typically the
     * half of the width and height of the panel.
     * @return center of panel in pixels
     */
    private Point getCenter() {
        return new Point(getSize().width / 2, getSize().height / 2);
    }

    /**
     * Our custom painting of awesome cars. Do not call directly, but call
     * repaint() instead.
     * @param Graphics
     */
    @Override
    public void paint(Graphics g) {
        paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        
        // This paint method gets called indirectly every 1/60th of a second
        // by the mainLoop which issues a 'repaint()' request. (AWT then
        // decides on when to do the actual painting, and at that moment this
        // method is called.)
        // World position Vec2(0,0) is the center of the screen
        // Scale translates one world point to n pixels.
        Point center = getCenter();
        
        scenario.readLock.lock();
        try {
            // First we should draw (or blit, that would be awesome fast!) the
            // roads. But there are no roads yet.
            // Then on top of those, we draw our cars.
            for (Car car : scenario.cars) {
                drawCar(g2, car, center, scale);
            }
        } finally {
            scenario.readLock.unlock();
        }
    }

    /**
     * Draw a car! Or, the body, wheels and if needed any debugging data.
     * @param graphics
     * @param car
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawCar(Graphics2D g2, Car car, Point offset, float scale) {
        g2 = (Graphics2D) g2.create();
        // For now this just draws the polygon of the physics body shape.
        // We might want to change this to our own polygon calculation based
        // on the car.body.getPosition() and car.body.getAngle() so we can
        // draw stuff like light and a windscreen to make the car identifyable.

        // Get me some wheels (draw them first because they are below)
        for (Wheel wheel : car.wheels) {
            g2.setColor(Color.BLACK);
            // (Assume the body of a wheel has only one fixture, the body shape itself.)
            drawShape(g2, wheel.body.getFixtureList().getShape(), wheel.body.getTransform(), offset, scale);
        }

        // Then draw the body of the car
        g2.setColor(car.color);
        drawShape(g2, car.bodyFixture.getShape(), car.body.getTransform(), offset, scale);

        // And overlay the vision of the driver
        if (drawFOV) {
            if (car.driver.seesOtherCars())
                g2.setColor(Color.BLUE);
            else
                g2.setColor(Color.YELLOW);
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            drawShape(g2, car.visionFixture.getShape(), car.body.getTransform(), offset, scale);
        }
        
        g2.dispose();
    }

    /**
     * Draws a filled JBox2D shape using fillPolygon or fillOval. Only supports
     * PolygonShape and CircleShape at this moment.
     * @param graphics
     * @param shape JBox2D shape
     * @param transform applied to the shape to get the location and rotation
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawShape(Graphics2D g2, Shape shape, Transform transform, Point offset, float scale) {
        switch (shape.getType()) {
            case POLYGON:
                drawPolygonShape(g2, (PolygonShape) shape, transform, offset, scale);
                break;
            case CIRCLE:
                drawCircleShape(g2, (CircleShape) shape, transform, offset, scale);
                break;
            default:
                throw new UnsupportedOperationException("drawShape can only draw POLYGON and CIRCLE for now.");
        } 
    }
    
    /**
     * Draws a filled JBox2D PolygonShape using fillPolygon.
     * @param graphics
     * @param poly JBox2D PolygonShape
     * @param transform applied to the shape to get the location and rotation
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawPolygonShape(Graphics2D g2, PolygonShape poly, Transform transform, Point offset, float scale) {
        int vertexCount = poly.m_count;
        int[] xs = new int[vertexCount];
        int[] ys = new int[vertexCount];

        Vec2 vertex = new Vec2();
        for (int i = 0; i < vertexCount; ++i) {
            Transform.mulToOutUnsafe(transform, poly.m_vertices[i], vertex);
            xs[i] = Math.round(vertex.x * scale + offset.x);
            ys[i] = Math.round(vertex.y * scale + offset.y);
        }

        g2.fillPolygon(xs, ys, vertexCount);
    }

    /**
     * Draws a filled JBox2D CircleShape using fillOval.
     * @param graphics
     * @param circle JBox2D CircleShape
     * @param transform applied to the shape to get the location and rotation
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawCircleShape(Graphics2D g2, CircleShape circle, Transform transform, Point offset, float scale) {   
        Vec2 center = new Vec2();
        Transform.mulToOutUnsafe(transform, circle.m_p, center);
        center.addLocal(-circle.getRadius(), -circle.getRadius());

        g2.fillOval(
            Math.round(center.x * scale + offset.x),
            Math.round(center.y * scale + offset.y),
            Math.round(2 * circle.getRadius() * scale),
            Math.round(2 * circle.getRadius() * scale));
    }
}
