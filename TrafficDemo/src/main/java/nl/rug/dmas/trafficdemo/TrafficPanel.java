/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import nl.rug.dmas.trafficdemo.actors.Driver;
import nl.rug.dmas.trafficdemo.streetgraph.Edge;
import nl.rug.dmas.trafficdemo.streetgraph.PointPath;
import nl.rug.dmas.trafficdemo.streetgraph.StreetGraph;
import nl.rug.dmas.trafficdemo.streetgraph.Vertex;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

/**
 * Draws cars.
 * @author jelmer
 */
public class TrafficPanel extends JPanel {

    Scenario scenario;
    float scale = 10f;
    
    // Options (for now)
    boolean drawFOV = true;
    boolean drawDirection = true;
    boolean drawDriverThoughts = false;
    
    final Color headlightColor = new Color(1.0f, 1.0f, 0.6f);
    final Color taillightColor = new Color(1.0f, 0.0f, 0.0f);
    final Color reverselightColor = new Color(1.0f, 1.0f, 1.0f);
    
    public TrafficPanel(Scenario scenarion) {
        this.scenario = scenarion;
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!e.isShiftDown())
                    TrafficPanel.this.scenario.selectedCars.clear();
                
                Car car = TrafficPanel.this.getCarAtPosition(e.getPoint());
                
                if (car != null)
                    TrafficPanel.this.scenario.selectedCars.add(car);
                
                repaint();
            }
        });
    }
    
    /**
     * Get the location of the mouse in World coordinates. I.e. the centre of
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
    
    public Car getCarAtPosition(Point position) {
        Point center = getCenter();
        float wx = (position.x - center.x) / scale;
        float wy = (position.y - center.y) / scale;
        
        Vec2 worldPoint = new Vec2(wx, wy);

        for (Car car : scenario.cars)
            if (car.bodyFixture.testPoint(worldPoint))
                return car;
        
        return null;
    }
    
    /**
     * Get the location in pixels of the centre of the panel, typically the
     * half of the width and height of the panel.
     * @return centre of panel in pixels
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

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        
        // This paint method gets called indirectly every 1/60th of a second
        // by the mainLoop which issues a 'repaint()' request. (AWT then
        // decides on when to do the actual painting, and at that moment this
        // method is called.)
        // World position Vec2(0,0) is the center of the screen
        // Scale translates one world point to n pixels.
        Point center = getCenter();
        g2.translate(center.x, center.y);
        g2.scale(scale, scale);
        
        g2.setStroke(new BasicStroke(1f / scale));
        g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D() / scale));
        
        // Draw the street-graph
        // Todo: draw this once and store it in a buffer that we can blit,
        // because it doesn't change that often
        if (scenario.streetGraph != null)
            drawEnvironment(g2, scenario.streetGraph);
        
        // Draw the path we paint for debugging purposes
        CopyOnWriteArrayList<Vec2> path = (CopyOnWriteArrayList<Vec2>) scenario.commonKnowledge.get("path");
        if (path != null) {
            g2.setColor(Color.RED);
            drawPath(g2, path);
        }
        
        scenario.readLock.lock();
        try {
            // Then on top of those, we draw our cars.
            drawCars(g2, scenario.cars);
            
            if (drawFOV)
                drawFOVs(g2);
            
            if (!scenario.selectedCars.isEmpty())
                drawSelection(g2);
        } finally {
            scenario.readLock.unlock();
        }
        
        g2.dispose();
    }

    /**
     * Draw all cars and appropriate debug data. This first draws all wheels,
     * then the car bodies, the lights and and then the debug data.
     * @param graphics
     * @param cars a list of cars
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawCars(Graphics2D g, List<Car> cars) {
        // Draw all wheels
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setColor(Color.BLACK);
        for (Car car : cars) {
            for (Wheel wheel : car.wheels) {
                // (Assume the body of a wheel has only one fixture, the body shape itself.)
                fillShape(g2, wheel.body.getFixtureList().getShape(), wheel.body.getTransform());
            }
        }
        
        // Then draw the body of the cars
        for (Car car : cars) {
            g2.setColor(car.color);
            fillShape(g2, car.bodyFixture.getShape(), car.body.getTransform());
        }
        
        // Draw headlights! I have too much free time.
        for (Car car : cars) {
            switch (car.acceleration) {
                case ACCELERATE:
                    drawLight(g2, headlightColor,
                        new Vec2(-car.width / 2 + 0.5f, -car.length / 2),
                        Math.round(car.body.getAngle() * MathUtils.RAD2DEG), 40, 5,
                        car.body.getTransform());
                    drawLight(g2, headlightColor,
                        new Vec2(car.width / 2 - 0.5f, -car.length / 2),
                        Math.round(car.body.getAngle() * MathUtils.RAD2DEG), 40, 5,
                        car.body.getTransform());
                    break;
                case BRAKE:
                    drawLight(g2, taillightColor,
                        new Vec2(-car.width / 2 + 0.5f, car.length / 2),
                        Math.round(car.body.getAngle() * MathUtils.RAD2DEG + 180), 120, 1,
                        car.body.getTransform());
                    drawLight(g2, taillightColor,
                        new Vec2(car.width / 2 - 0.5f, car.length / 2),
                        Math.round(car.body.getAngle() * MathUtils.RAD2DEG + 180), 120, 1,
                        car.body.getTransform());
                    break;
                case REVERSE:
                    drawLight(g2, reverselightColor,
                        new Vec2(-car.width / 2 + 0.5f, car.length / 2),
                        Math.round(car.body.getAngle() * MathUtils.RAD2DEG + 180), 120, 5,
                        car.body.getTransform());
                    drawLight(g2, reverselightColor,
                        new Vec2(car.width / 2 - 0.5f, car.length / 2),
                        Math.round(car.body.getAngle() * MathUtils.RAD2DEG + 180), 120, 5,
                        car.body.getTransform());
                    break;
            }
        }
        
        // for testing, draw the angle the car tries to achieve
        if (drawDirection) {
            g2.setColor(Color.RED);
            for (Car car : cars) {
                drawAngle(g2, car.targetBodyAngle, car.body.getPosition());
            }
        }
        
        // for gaining insight, draw the stored drawing calls that the car AI made
        if (drawDriverThoughts) {
            g2.setColor(Color.GREEN);
            for (Car car : cars)
                drawDriverDebug(g2, car.driver);
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
    private void fillShape(Graphics2D g2, Shape shape, Transform transform) {
        java.awt.Shape shape2d = getShape(shape, transform);
        g2.fill(shape2d);
    }
    
    /**
     * Draw an absolute angle as an arrow of 1.0 (i.e. 1.0 * scale) world point length
     * @param g2
     * @param angle in radians
     * @param position of the beginning of the angle
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawAngle(Graphics2D g2, float angle, Vec2 position) {
        angle -= MathUtils.HALF_PI;
        Vec2 direction = new Vec2(MathUtils.cos(angle), MathUtils.sin(angle));
        drawVec(g2, direction, position);
    }
    
    /**
     * Draw a direction vector relative to position.
     * @param g2
     * @param direction vector
     * @param position of the base of the vector
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawVec(Graphics2D g2, Vec2 direction, Vec2 position) {
        // target is the absolute world position of the tip of the vector
        Vec2 target = position.add(direction);
        
        // draw the line of the vector
        g2.draw(new Line2D.Float(
            position.x, position.y,
            target.x, target.y
        ));
        
        // and draw a little arrowhead at the tip
        float angle = MathUtils.atan2(direction.y, direction.x);
        float arrowLength = MathUtils.clamp(direction.length() / 10f, 0.5f, 1.0f);
        float arrowWidth = 0.125f * MathUtils.PI;
        
        Path2D arrow = new Path2D.Float();
        
        // Start at the tip
        arrow.moveTo(target.x, target.y);
        
        // bottom left
        arrow.lineTo(
            target.x - arrowLength * MathUtils.cos(angle + arrowWidth),
            target.y - arrowLength * MathUtils.sin(angle + arrowWidth));
        
        // bottom right
        arrow.lineTo(
            target.x - arrowLength * MathUtils.cos(angle - arrowWidth),
            target.y - arrowLength * MathUtils.sin(angle - arrowWidth));
        
        arrow.closePath();
        g2.fill(arrow);
    }

    /**
     * Draw a little headlight box with light coming out of it!
     * @param g
     * @param lightColor colour of the light
     * @param position position in car space of the point between the light and the light box I.e. the light box is 'behind' this point, and the light cone 'in front of'
     * @param angleDeg absolute rotation of the light in degrees
     * @param angleWidth width of the light beam in degrees
     * @param reach length of the light beam in pixels
     * @param transform transforms the position of the light in car space to world space
     * @param offset in pixels of 0,0 in world space
     * @param scale to scale world space coordinates to pixels
     */
    private void drawLight(Graphics2D g, Color lightColor, Vec2 position, int angleDeg, int angleWidth, float reach, Transform transform) {
        Graphics2D g2 = (Graphics2D) g.create();
        Vec2 worldPosition = new Vec2();
        Transform.mulToOutUnsafe(transform, position, worldPosition);
        
        Point2D center = new Point2D.Float(
            worldPosition.x,
            worldPosition.y);
        
        g2.translate(center.getX(), center.getY());
        g2.rotate(angleDeg * MathUtils.DEG2RAD);
        
        // First, draw the light box itself
        g2.setColor(lightColor);
        g2.fill(new Rectangle2D.Float(-0.2f, 0, 0.4f, 0.2f));
        
        // Secondly, draw the light beam
        
        angleDeg -= 90; // light is up, not to the right
        
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {lightColor, new Color(0.0f, 0.0f, 0.0f, 0.0f)};
        RadialGradientPaint p = new RadialGradientPaint(new Point(0, 0), reach / 2f, dist, colors);
        g2.setPaint(p);
        g2.fill(new Arc2D.Float(-reach, -reach, reach * 2, reach * 2, -angleWidth / 2 + 90, angleWidth, Arc2D.PIE));
        
        g2.dispose();
    }

    private void drawPath(Graphics2D g2, List<Vec2> path) {
        for (Vec2 point : path) {
            g2.fill(new Ellipse2D.Float(
                point.x - 0.2f,
                point.y - 0.2f,
                0.4f, 0.4f));
        }
    }

    private void drawVertex(Graphics2D g, Vertex vertex) {
        Graphics2D g2 = (Graphics2D) g.create();
        float radius = 0.5f;
        
        Vec2 point = vertex.getLocation();
        
        drawPosition(g2, point, radius);
        
        g2.setColor(Color.WHITE);
        g2.drawString(Integer.toString(vertex.getVertexListIndex()),
            point.x, point.y);
        
        g2.dispose();
    }

    private void drawEdge(Graphics2D g2, Edge edge) {
        Vec2 position = edge.getOrigin().getLocation();
        Vec2 direction = edge.getDestination().getLocation().sub(position);
        drawVec(g2, direction, position);
    }
    
    private void drawFOVs(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setColor(Color.BLUE);

        for (Body body = scenario.getWorld().getBodyList(); body != null; body = body.getNext()) {
            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                if (fixture.isSensor() && fixture.getUserData() instanceof Observer) {
                    fillShape(g2, fixture.getShape(), body.getTransform());
                }
            }
        }
        
        g2.dispose();
    }
    
    private void drawRoad(Graphics2D g, Edge edge) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        LinkedList<Vertex> path = new LinkedList<>();
        path.add(edge.getOrigin());
        path.add(edge.getDestination());
        
        PointPath points = StreetGraph.generatePointPath(path);
        
        // First draw the road side
        Stroke roadSideStroke = new BasicStroke(0.3f);
        g2.setStroke(roadSideStroke);
        g2.setColor(Color.DARK_GRAY);
        
        // Both left and right side
        drawLine(g2, points.translate(1.5f).iterator());
        drawLine(g2, points.translate(-1.5f).iterator());
        
        // Then draw the road itself so it overlays all the road side drawing
        // glitches which occur at the intersections
        Stroke roadStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(roadStroke);
        g2.setColor(Color.LIGHT_GRAY);
        
        drawLine(g2, points.iterator());
        
        g2.dispose();
    }
    
    private void drawLine(Graphics2D g2, Iterator<Vec2> pointIter) {
        Path2D.Float path = new Path2D.Float();
        
        Vec2 point = pointIter.next();
        path.moveTo(point.x, point.y);
        
        while (pointIter.hasNext()) {
            point = pointIter.next();
            path.lineTo(point.x, point.y);
        }
        
        g2.draw(path);
    }

    private void drawPosition(Graphics2D g2, Vec2 point, float radius) {
        g2.fill(new Ellipse2D.Float(
            point.x - radius, point.y - radius,
            radius * 2, radius * 2
        ));
    }

    /**
     * Draw the environment (aka roads)
     * @param g2
     * @param streetGraph
     * @param center
     * @param scale 
     */
    private void drawEnvironment(Graphics2D g2, StreetGraph streetGraph) {
        // First draw the actual road
        for (Edge edge : streetGraph.getEdges())
            drawRoad(g2, edge);

        // Then draw the graph as an overlay
        g2.setColor(Color.BLACK);
        for (Edge edge : streetGraph.getEdges())
            drawEdge(g2, edge);

        for (Vertex vertex : streetGraph.getVertices()) {
            if (streetGraph.isSink(vertex))
                g2.setColor(Color.RED);
            else if (streetGraph.isSource(vertex))
                g2.setColor(Color.GREEN);
            else
                g2.setColor(Color.BLUE);

            drawVertex(g2, vertex);
        }
    }

    private void drawDriverDebug(final Graphics2D g2, final Driver driver) {
        driver.debugDraw.renderTo(new DebugGraphicsQueue.Renderer() {
            @Override
            public void drawPositionVelocity(Vec2 position, Vec2 velocity) {
                TrafficPanel.this.drawVec(g2, velocity, position);
            }

            @Override
            public void drawPosition(Vec2 position) {
                TrafficPanel.this.drawPosition(g2, position, 0.3f);
            }
        });
    }
    
    private java.awt.Shape getShape(Shape shape, Transform transform) {
        switch (shape.getType()) {
            case POLYGON:
                return getPolygonShape((PolygonShape) shape, transform);
            case CIRCLE:
                return getCircleShape((CircleShape) shape, transform);
            default:
                throw new UnsupportedOperationException("drawShape can only draw POLYGON and CIRCLE for now.");
        } 
    }
    
    private java.awt.Shape getPolygonShape(PolygonShape poly, Transform transform) {
        int vertexCount = poly.m_count;
        Path2D.Float path = new Path2D.Float();

        Vec2 vertex = new Vec2();
        for (int i = 0; i < vertexCount; ++i) {
            Transform.mulToOutUnsafe(transform, poly.m_vertices[i], vertex);
            if (i == 0) {
                path.moveTo(vertex.x, vertex.y);
            } else {
                path.lineTo(vertex.x, vertex.y);
            }
        }
        
        path.closePath();
        return path;
    }
    
    private java.awt.Shape getCircleShape(CircleShape circle, Transform transform) {
        Vec2 center = new Vec2();
        Transform.mulToOutUnsafe(transform, circle.m_p, center);
        center.addLocal(-circle.getRadius(), -circle.getRadius());

        return new Ellipse2D.Float(
            center.x, center.y,
            2 * circle.getRadius(),
            2 * circle.getRadius());
    }

    private void drawSelection(Graphics2D g2) {
        Graphics2D strokePainter = (Graphics2D) g2.create();
        strokePainter.setColor(UIManager.getColor("Focus.color"));
        strokePainter.setStroke(new BasicStroke(2.0f / scale));

        for (Car car : scenario.selectedCars) {
            Area area = new Area();

            for (Wheel wheel : car.wheels)
                area.add(new Area(getShape(wheel.body.getFixtureList().getShape(), wheel.body.getTransform())));

            area.add(new Area(getShape(car.bodyFixture.getShape(), car.body.getTransform())));

            strokePainter.draw(area);
        }

        strokePainter.dispose();
    }
}
