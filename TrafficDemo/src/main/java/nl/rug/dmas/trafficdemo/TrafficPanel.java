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
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
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
     * @param g
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
        
        // Draw headlights! I have too much free time.
        switch (car.acceleration) {
            case ACCELERATE:
                drawHeadlight(g2, Color.YELLOW,
                    new Vec2(-car.width / 2 + 0.5f, -car.length / 2),
                    Math.round(car.body.getAngle() * MathUtils.RAD2DEG), 40, 50,
                    car.body.getTransform(), offset, scale);
                drawHeadlight(g2, Color.YELLOW,
                    new Vec2(car.width / 2 - 0.5f, -car.length / 2),
                    Math.round(car.body.getAngle() * MathUtils.RAD2DEG), 40, 50,
                    car.body.getTransform(), offset, scale);
                break;
            case BRAKE:
                drawHeadlight(g2, Color.RED,
                    new Vec2(-car.width / 2 + 0.5f, car.length / 2),
                    Math.round(car.body.getAngle() * MathUtils.RAD2DEG + 180), 120, 10,
                    car.body.getTransform(), offset, scale);
                drawHeadlight(g2, Color.RED,
                    new Vec2(car.width / 2 - 0.5f, car.length / 2),
                    Math.round(car.body.getAngle() * MathUtils.RAD2DEG + 180), 120, 10,
                    car.body.getTransform(), offset, scale);
                break;
        }
        
        // And overlay the vision of the driver
        if (drawFOV) {
            if (car.driver.seesOtherCars())
                g2.setColor(Color.BLUE);
            else
                g2.setColor(Color.YELLOW);
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            drawShape(g2, car.visionFixture.getShape(), car.body.getTransform(), offset, scale);
        }

        // for testing, draw the angle the car tries to achieve
        g2.setColor(Color.RED);
        drawAngle(g2, car.targetBodyAngle, car.body.getPosition(), offset, scale);

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
    
    private void drawAngle(Graphics2D g2, float angle, Vec2 position, Point offset, float scale) {
        angle -= MathUtils.HALF_PI;
        Vec2 direction = new Vec2(MathUtils.cos(angle), MathUtils.sin(angle));
        drawVec(g2, direction, position, offset, scale);
    }
    
    private void drawVec(Graphics2D g2, Vec2 direction, Vec2 position, Point offset, float scale) {
        // target is the absolute world position of the tip of the vector
        Vec2 target = position.add(direction);
        
        // draw the line of the vector
        g2.drawLine(
            Math.round(position.x * scale + offset.x),
            Math.round(position.y * scale + offset.y),
            Math.round(target.x * scale + offset.x),
            Math.round(target.y * scale + offset.y)
        );
        
        // and draw a little arrowhead at the tip
        float angle = MathUtils.atan2(direction.y, direction.x);
        float arrowLength = MathUtils.clamp(direction.length() / 10f, 0.5f, 1.0f);
        float arrowWidth = 0.125f * MathUtils.PI;
        
        int[] xs = new int[]{
            Math.round(target.x * scale + offset.x), // tip
            Math.round((target.x - arrowLength * MathUtils.cos(angle + arrowWidth)) * scale + offset.x), // bottom left
            Math.round((target.x - arrowLength * MathUtils.cos(angle - arrowWidth)) * scale + offset.x) // bottom right
        };
        
        int[] ys = new int[] {
            Math.round(target.y * scale + offset.y),
            Math.round((target.y - arrowLength * MathUtils.sin(angle + arrowWidth)) * scale + offset.y),
            Math.round((target.y - arrowLength * MathUtils.sin(angle - arrowWidth)) * scale + offset.y)
        };
        
        g2.fillPolygon(xs, ys, xs.length);
    }

    private void drawHeadlight(Graphics2D g, Color lightColor, Vec2 position, int angleDeg, int angleWidth, int reach, Transform transform, Point offset, float scale) {
        Graphics2D g2 = (Graphics2D) g.create();
        Vec2 worldPosition = new Vec2();
        Transform.mulToOutUnsafe(transform, position, worldPosition);
        
        Point center = new Point(
            Math.round(worldPosition.x * scale) + offset.x,
            Math.round(worldPosition.y * scale) + offset.y);
        
        g2.translate(center.x, center.y);
        g2.rotate(angleDeg * MathUtils.DEG2RAD);
        
        // First, draw the light box itself
        g2.setColor(lightColor);
        g2.fillRect(-2, 0, 4, 2);
        
        // Secondly, draw the light beam
        
        /*
        angleDeg %= 360;
        angleDeg *= -1;
        */
        angleDeg -= 90;
        
        float radius = reach / 2;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {lightColor, new Color(0.0f, 0.0f, 0.0f, 0.0f)};
        RadialGradientPaint p = new RadialGradientPaint(new Point(0, 0), radius, dist, colors);
        g2.setPaint(p);
        g2.fillArc(-reach, -reach, reach * 2, reach * 2, -angleWidth / 2 + 90, angleWidth);
        
        g2.dispose();
    }
}
