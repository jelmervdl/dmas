/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JPanel;
import javax.swing.UIManager;
import nl.rug.dmas.trafficdemo.actors.Driver;
import nl.rug.dmas.trafficdemo.streetgraph.Edge;
import nl.rug.dmas.trafficdemo.streetgraph.NoPathException;
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
 *
 * @author jelmer
 */
public class TrafficPanel extends JPanel {

    Scenario scenario;
    float scale = 10f;
    
    final int offset = 6;

    // Options (for now)
    boolean drawFOV = true;
    boolean drawDirection = true;
    
    boolean drawDriverThoughts = false;
    
    final Color headlightColor = new Color(1.0f, 1.0f, 0.6f);
    final Color taillightColor = new Color(1.0f, 0.0f, 0.0f);
    final Color reverselightColor = new Color(1.0f, 1.0f, 1.0f);
    
    final Stroke roadStroke = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    
    ScheduledThreadPoolExecutor backgroundThreadPool = new ScheduledThreadPoolExecutor(1);
    private Future<?> environmentBufferUpdateTask = null;
    
    private Image environmentBufferImage = null;
    private Rectangle2D.Float environmentBufferBounds;
    
    final ScenarioListener scenarioListener;
    
    final private List<Collision> collisions = Collections.synchronizedList(new ArrayList<Collision>());

    static private class Collision {
        final Vec2 position;
        final float time;
        
        Collision(Vec2 position, float time) {
            this.position = position;
            this.time = time;
        }
    }
    
    public TrafficPanel(Scenario scenarion) {
        this.scenario = scenarion;
        
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Listen to the scenario for repaints and collisions
        scenarioListener = new ScenarioAdapter() {
            @Override
            public void scenarioStepped() {
                repaint();
            }

            @Override
            public void selectionChanged() {
                repaint();
            }

            @Override
            public void carsCollided(Car carA, Car carB, Vec2 position) {
                collisions.add(new Collision(
                    carA.body.getWorldPoint(position),
                    scenario.getTime()
                ));
            }
        };
        
        // Listen for [tab] key to modify the selected car
        // (Shift + [tab] will do the selection in the other direction.)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!isFocusOwner())
                    return;
                
                if (e.getKeyChar() == '\t') {
                    if (scenario.cars.isEmpty())
                        return;
                    
                    Car next = scenario.cars.get(0);
                    
                    if (scenario.getSelection().size() > 0) {
                        Car current = scenario.getSelection().iterator().next();
                        int pos = scenario.cars.indexOf(current);
                        int direction = e.isShiftDown() ? -1 : 1;
                        next = scenario.cars.get((pos + direction) % scenario.cars.size());
                    }

                    scenario.setSelection(Collections.singleton(next));
                    e.consume();
                }
            }
        });
        
        // Allow clicking on cars to select them
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isConsumed())
                    return;
                
                if (!e.isShiftDown())
                    TrafficPanel.this.scenario.clearSelection();
                
                Car car = TrafficPanel.this.getCarAtPosition(e.getPoint());
                
                if (car != null) {
                    TrafficPanel.this.scenario.addSelection(car);
                    e.consume();
                }
            }
        });
        
        // Use ctrl + mouse wheel to zoom
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    e.consume();
                    
                    float delta = 0.05f * (float) e.getPreciseWheelRotation();
                    setScale(MathUtils.clamp(scale + delta, 1.0f, 50f));
                } else {
                    getParent().dispatchEvent(e);
                }
            }
        });
    }
    
    /**
     * Get the location of the mouse in World coordinates. I.e. the centre of
     * this panel is world coordinate 0,0. Scaling is also taken into account.
     *
     * @return Vec2 with the mouse coordinates in world space.
     */
    public Vec2 getMouseWorldLocation() {
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        Point panelLoc = getLocationOnScreen();
        int mx = mouseLoc.x - panelLoc.x;
        int my = mouseLoc.y - panelLoc.y;
        return getPositionInWorld(new Point(mx, my));
    }
    
    public Car getCarAtPosition(Point position) {
        Vec2 worldPoint = getPositionInWorld(position);

        for (Car car : scenario.cars)
            if (car.bodyFixture.testPoint(worldPoint))
                return car;
        
        return null;
    }
    
    public Vec2 getPositionInWorld(Point position) {
        Point center = getCenter();
        float wx = (position.x - center.x) / scale;
        float wy = (position.y - center.y) / scale;
        return new Vec2(wx, wy);
    }
    
    /**
     * Get the location in pixels of the centre of the panel, typically the
     * half of the width and height of the panel.
     * @return centre of panel in pixels
     */
    private Point getCenter() {
        return new Point(getSize().width / 2, getSize().height / 2);
    }

    @Override
    public Dimension getPreferredSize() {
        Rectangle2D.Float worldBounds = scenario.getStreetGraph().getBounds();
        
        return new Dimension(
            (int) Math.ceil((worldBounds.getWidth() + 2 * offset)  * scale),
            (int) Math.ceil((worldBounds.getHeight() + 2 * offset) * scale));
    }
    
    /**
     * Get the world bounds from the street graph with offset spacing. Note that
     * the offsets may be negative since this returns a rectangle in world
     * coordinates which has 0,0 as its centre.
     * @return rectangle in world coordinates
     */
    public Rectangle2D.Float getWorldBounds() {
        Rectangle2D.Float worldBounds = scenario.getStreetGraph().getBounds();
        return new Rectangle2D.Float(
                worldBounds.x - offset,
                worldBounds.y - offset,
                worldBounds.width + 2 * offset,
                worldBounds.height + 2 * offset);   
    }
    
    /**
     * Change the scale of the drawing. If needed it will cause a repaint and
     * revalidation so the background gets redrawn.
     * @param newScale number of pixels per world meter.
     */
    public void setScale(float newScale) {
        if (newScale < 0.1 || newScale > 100)
            throw new IllegalArgumentException("Only scales between 0.1 and 100 are allowed to maintain stability");
        
        if (newScale != scale) {
            scale = newScale;
            revalidate();
            repaint();
        }
    }
    
    /**
     * Return the current scale. Scale can be seen as the number of pixels used
     * to draw one meter in the simulated world.
     * @return scale in number of pixels per meter
     */
    public float getScale() {
        return scale;
    }
    
    /**
     * Scale to fit the graph in the bounds of the container. It will include a
     * bit of offset.
     * @param container from which to obtain the dimensions.
     */
    public void scaleToFit(Container container) {
        Rectangle2D.Float worldBounds = getWorldBounds();
        Dimension sizeToFit = container.getSize();
        setScale(Math.min(
                (sizeToFit.width - 32) / worldBounds.width, 
                (sizeToFit.height - 32) / worldBounds.height));
    }

    @Override
    public void invalidate() {
        repaintEnvironment();
        super.invalidate();
    }
    

    /**
     * Our custom painting of awesome cars. Do not call directly, but call
     * repaint() instead.
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        // This paint method gets called indirectly every 1/60th of a second
        // by the mainLoop which issues a 'repaint()' request. (AWT then
        // decides on when to do the actual painting, and at that moment this
        // method is called.)
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // World position Vec2(0,0) is the center of the screen
        // Scale translates one world point to n pixels.
        Graphics2D gs = (Graphics2D) g2.create();
        Point center = getCenter();
        gs.translate(center.x, center.y);
        
        // Draw the street-graph
        if (scenario.streetGraph != null && environmentBufferImage != null) {
            g2.drawImage(environmentBufferImage,
                    (int) (center.x + environmentBufferBounds.x * scale),
                    (int) (center.y + environmentBufferBounds.y * scale),
                    (int) Math.ceil(environmentBufferBounds.width * scale),
                    (int) Math.ceil(environmentBufferBounds.height * scale), null);
        }
        
        gs.scale(scale, scale);
        
        // Scale the stroke and font back to 1.0 in screen space.
        gs.setStroke(new BasicStroke(1f / scale));
        gs.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D() / scale));
        
        // Draw the path we paint for debugging purposes
        CopyOnWriteArrayList<Vec2> path = (CopyOnWriteArrayList<Vec2>) scenario.commonKnowledge.get("path");
        if (path != null) {
            gs.setColor(Color.RED);
            drawPath(g2, path, -1);
        }

        // Lock the world for reading, so we don't delete a car from the list
        // while iterating over said list (resulting in an exception, or just
        // a pair of wheels with a missing body.)
        scenario.readLock.lock();
        try {
            // Then on top of those, we draw our cars.
            drawCars(gs, scenario.cars);
            
            drawCollisions(gs);
            
            // Draw fields of view of all sensors in the world, if enabled.
            if (drawFOV)
                drawFOVs(gs);
            
            // Draw an outline around the selected cars, if there are any cars selected
            if (!scenario.selectedCars.isEmpty())
                drawSelection(gs);
        } finally {
            scenario.readLock.unlock();
        }
        
        gs.dispose();
        
        // Draw scenario time in top left corner
        // Todo: do this outside this panel, because now the values are obscured
        // by the scroll pane.
        drawTime(g2, new Point(5, 5));
        drawScale(g2, new Point(5, getHeight() - 15));
        g2.dispose();
    }

    /**
     * Draw all cars and appropriate debug data. This first draws all wheels,
     * then the car bodies, the lights and and then the debug data.
     *
     * @param graphics
     * @param cars a list of cars
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
                        new Vec2(-car.getWidth() / 2 + 0.5f, -car.getLength() / 2),
                        car.body.getAngle(), 40 * MathUtils.DEG2RAD, 5,
                        car.body.getTransform());
                    drawLight(g2, headlightColor,
                        new Vec2(car.getWidth() / 2 - 0.5f, -car.getLength() / 2),
                        car.body.getAngle(), 40 * MathUtils.DEG2RAD, 5,
                        car.body.getTransform());
                    break;
                case BRAKE:
                    drawLight(g2, taillightColor,
                        new Vec2(-car.getWidth() / 2 + 0.5f, car.getLength() / 2),
                        car.body.getAngle() + MathUtils.PI, 120 * MathUtils.DEG2RAD, 1,
                        car.body.getTransform());
                    drawLight(g2, taillightColor,
                        new Vec2(car.getWidth() / 2 - 0.5f, car.getLength() / 2),
                        car.body.getAngle() + MathUtils.PI, 120 * MathUtils.DEG2RAD, 1,
                        car.body.getTransform());
                    break;
                case REVERSE:
                    drawLight(g2, reverselightColor,
                        new Vec2(-car.getWidth() / 2 + 0.5f, car.getLength() / 2),
                        car.body.getAngle() + MathUtils.PI, 120 * MathUtils.DEG2RAD, 5,
                        car.body.getTransform());
                    drawLight(g2, reverselightColor,
                        new Vec2(car.getWidth() / 2 - 0.5f, car.getLength() / 2),
                        car.body.getAngle() + MathUtils.PI, 120 * MathUtils.DEG2RAD, 5,
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
     *
     * @param graphics
     * @param shape JBox2D shape
     * @param transform applied to the shape to get the location and rotation
     */
    private void fillShape(Graphics2D g2, Shape shape, Transform transform) {
        java.awt.Shape shape2d = getShape(shape, transform);
        g2.fill(shape2d);
    }

    /**
     * Draw an absolute angle as an arrow of 1.0 (i.e. 1.0 * scale) world point
     * length
     *
     * @param g2
     * @param angle in radians
     * @param position of the beginning of the angle
     */
    private void drawAngle(Graphics2D g2, float angle, Vec2 position) {
        angle -= MathUtils.HALF_PI;
        Vec2 direction = new Vec2(MathUtils.cos(angle), MathUtils.sin(angle));
        drawVec(g2, direction, position);
    }

    /**
     * Draw a direction vector relative to position.
     *
     * @param g2
     * @param direction vector
     * @param position of the base of the vector
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
     *
     * @param g
     * @param lightColor colour of the light
     * @param position position in car space of the point between the light and the light box I.e. the light box is 'behind' this point, and the light cone 'in front of'
     * @param angle absolute rotation of the light in radians
     * @param angleWidth width of the light beam in radians
     * @param reach length of the light beam
     * @param transform transforms the position of the light in car space to world space
     */
    private void drawLight(Graphics2D g, Color lightColor, Vec2 position, float angle, float angleWidth, float reach, Transform transform) {
        Graphics2D g2 = (Graphics2D) g.create();
        Vec2 worldPosition = new Vec2();
        Transform.mulToOutUnsafe(transform, position, worldPosition);

        Point2D center = new Point2D.Float(
            worldPosition.x,
            worldPosition.y);
        
        g2.translate(center.getX(), center.getY());
        g2.rotate(angle);
        
        // First, draw the light box itself
        g2.setColor(lightColor);
        g2.fill(new Rectangle2D.Float(-0.2f, 0, 0.4f, 0.2f));
        
        // Secondly, draw the light beam
        
        angle -= MathUtils.HALF_PI; // light is up, not to the right
        
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {lightColor, new Color(0.0f, 0.0f, 0.0f, 0.0f)};
        RadialGradientPaint p = new RadialGradientPaint(new Point(0, 0), reach / 2f, dist, colors);
        g2.setPaint(p);
        g2.fill(new Arc2D.Float(-reach, -reach, reach * 2, reach * 2, (-angleWidth / 2 * MathUtils.RAD2DEG) + 90, angleWidth * MathUtils.RAD2DEG, Arc2D.PIE));

        g2.dispose();
    }

    private void drawPath(Graphics2D g2, List<Vec2> path, int current) {
        for (int i = 0; i < path.size(); ++i) {
            Vec2 point = path.get(i);
            Ellipse2D.Float circle = new Ellipse2D.Float(
                point.x - 0.2f,
                point.y - 0.2f,
                0.4f, 0.4f);
            
            if (i == current) {
                g2.fill(circle);
            } else {
                g2.draw(circle);
            }
        }
    }

    private void drawVertex(Graphics2D g, Vertex vertex) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        Vec2 point = vertex.getLocation();
        float radius = 1.0f;
        
        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Float(
            point.x - radius, point.y - radius,
            radius * 2, radius * 2
        ));
        
        drawVertexName(g2, vertex);
        
        g2.dispose();
    }

    private void drawEdge(Graphics2D g2, Edge edge) {
        Vec2 position = edge.getOrigin().getLocation();
        Vec2 direction = VecUtils.getDirection(position, edge.getDestination().getLocation());
        
        // move the position a bit away form 
        Vec2 normDirection = new Vec2(direction);
        normDirection.normalize();
        
        position = position.add(normDirection.mul(1.0f));
        direction = direction.sub(normDirection.mul(2.0f));
        
        drawVec(g2, direction, position);
    }

    private void drawFOVs(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2.setColor(Color.BLUE);

        for (Body body = scenario.getWorld().getBodyList(); body != null; body = body.getNext()) {
            for (Fixture fixture = body.getFixtureList(); fixture != null; fixture = fixture.getNext()) {
                if (fixture.isSensor() && fixture.getUserData() instanceof Driver) {
                    fillShape(g2, fixture.getShape(), body.getTransform());
                }
            }
        }

        g2.dispose();
    }

    private void drawRoad(Graphics2D g, StreetGraph graph, Vertex source, Vertex sink) {
        // Skip if the source and sink are the same.
        if (source.equals(sink))
            return;

        Graphics2D g2 = (Graphics2D) g.create();
        
        try {
            g2.setStroke(roadStroke);
            g2.setColor(Color.LIGHT_GRAY);
            
            List<Vec2> points = graph.generatePointPath(source, sink);
            drawLine(g2, points.iterator());
        } catch (NoPathException ex) {
            // Ignore, no route possible so no road required
        } finally {
            g2.dispose();
        }

    }
    
    private void drawLine(Graphics2D g2, Iterator<Vec2> pointIter) {
        Path2D.Float path = new Path2D.Float();
        
        // if it is an empty line, don't draw it at all
        if (!pointIter.hasNext())
            return;
        
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
     */
    private void drawEnvironment(Graphics2D g2, StreetGraph streetGraph) {
        // First draw the roads by finding all possible paths that can be
        // traveled with the cars
        for (Vertex source : streetGraph.getSources()) {
            for (Vertex sink : streetGraph.getSinks()) {
                drawRoad(g2, streetGraph, source, sink);
            }
        }

        // Then draw the graph as an overlay
        g2.setColor(Color.DARK_GRAY);
        for (Edge edge : streetGraph.getEdges()) {
            drawEdge(g2, edge);
        }
        
        // Finally mark the vertices, sources and sinks clearly
        for (Vertex vertex : streetGraph.getVertices()) {
            if (streetGraph.isSink(vertex))
                drawSink(g2, vertex);
            else if (streetGraph.isSource(vertex))
                drawSource(g2, vertex);
            else
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
        Transform.mulToOutUnsafe(transform, circle.getVertex(0), center);
        center.addLocal(-circle.getRadius(), -circle.getRadius());

        return new Ellipse2D.Float(
            center.x, center.y,
            2 * circle.getRadius(),
            2 * circle.getRadius());
    }

    private void drawSelection(Graphics2D g2) {
        drawCarsOutline(g2, scenario.selectedCars);
        drawDriverPaths(g2, scenario.selectedCars);
    }
    
    private void drawCarsOutline(Graphics2D g2, Collection<Car> cars) {
        Graphics2D strokePainter = (Graphics2D) g2.create();
        strokePainter.setColor(UIManager.getColor("Focus.color"));
        strokePainter.setStroke(new BasicStroke(2.0f / scale));

        for (Car car : cars) {
            Area area = new Area();

            for (Wheel wheel : car.wheels)
                area.add(new Area(getShape(wheel.body.getFixtureList().getShape(), wheel.body.getTransform())));

            area.add(new Area(getShape(car.bodyFixture.getShape(), car.body.getTransform())));

            strokePainter.draw(area);
        }

        strokePainter.dispose();
    }
    
    private void drawDriverPaths(Graphics2D g2, Collection<Car> cars) {
        Graphics2D pathPainter = (Graphics2D) g2.create();
        pathPainter.setColor(Color.red);
        for (Car car : cars) {
            drawPath(pathPainter, car.driver.getPath(), car.driver.getPathIndex());
        }
        pathPainter.dispose();
    }

    private synchronized void updateEnvironmentBuffer() {
        Rectangle2D.Float bufferBounds = getWorldBounds();
        
        float bufferScale = scale * 2f; // scale plus 2x for HighDPI;
        
        Image image = createImage(
                (int) Math.ceil(bufferBounds.width * bufferScale),
                (int) Math.ceil(bufferBounds.height * bufferScale));
        
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        
        g2.scale(bufferScale, bufferScale); // scale for the drawing
        g2.translate(-bufferBounds.x, -bufferBounds.y); // and re-center to world 0.0
        
        // Scale the stroke and font back to 1.0 in screen space.
        g2.setStroke(new BasicStroke(1f / bufferScale));
        g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize2D() / bufferScale));
        
        drawEnvironment(g2, scenario.streetGraph);
        
        g2.dispose();
        
        // Make the image and associated bounds available
        if (environmentBufferImage != null) {
            environmentBufferImage.flush();
        }
        
        environmentBufferBounds = bufferBounds;
        environmentBufferImage = image;
    }
    
    private void repaintEnvironment() {
        // Try to cancel a previous update
        if (environmentBufferUpdateTask != null && !environmentBufferUpdateTask.isDone()) {
            environmentBufferUpdateTask.cancel(false);
        }
        
        // and schedule a new one
        environmentBufferUpdateTask = backgroundThreadPool.schedule(new Runnable() {
            @Override
            public void run() {
                updateEnvironmentBuffer();
                repaint();
            }
        }, 250, TimeUnit.MILLISECONDS);
    }
    
    private void drawVertexName(Graphics2D g, Vertex vertex) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        String name = Integer.toString(vertex.getVertexListIndex());
        Font font = g2.getFont().deriveFont(1f);
        GlyphVector nameShape = font.createGlyphVector(g2.getFontRenderContext(), name);
        
        Rectangle2D bounds = nameShape.getVisualBounds();
        
        g2.translate(
            vertex.getLocation().x - bounds.getCenterX(),
            vertex.getLocation().y - bounds.getCenterY());
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(0.3f));
        g2.draw(nameShape.getOutline());
        
        g2.setColor(Color.BLACK);
        g2.fill(nameShape.getOutline());
        
        g2.dispose();
    }
    
    private void drawSource(Graphics2D g, Vertex vertex) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        Vec2 point = vertex.getLocation();
        float radius = 1.0f;
        float innerRadius = 0.7f;
        
        g2.setColor(Color.BLACK);
        g2.fill(new Ellipse2D.Float(
            point.x - radius, point.y - radius,
            radius * 2, radius * 2
        ));
        
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(0.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Float(
            point.x - innerRadius, point.y - innerRadius,
            innerRadius * 2 - 0.03f, innerRadius * 2 - 0.03f
        ));
        
        drawVertexName(g2, vertex);
        
        g2.dispose();
    }

    private void drawSink(Graphics2D g, Vertex vertex) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        Vec2 point = vertex.getLocation();
        float radius = 1.0f;
        
        Ellipse2D.Float circle = new Ellipse2D.Float(
            point.x - radius, point.y - radius,
            radius * 2, radius * 2
        );
        
        g2.setColor(Color.BLACK);
        g2.fill(circle);
        
        float d = radius * MathUtils.cos(MathUtils.QUARTER_PI);
        
        g2.setClip(circle);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Float(point.x - d, point.y - d, point.x + d, point.y + d)); // top left to bottom right
        g2.draw(new Line2D.Float(point.x + d, point.y - d, point.x - d, point.y + d)); // top right to bottom left
        
        g2.setClip(null);
        
        drawVertexName(g2, vertex);
        
        g2.dispose();
    }

    private void drawCollisions(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.ORANGE);
        
        synchronized (collisions) {
            Iterator<Collision> iter = collisions.iterator();

            while (iter.hasNext()) {
                Collision collision = iter.next();
                float progress = (scenario.getTime() - collision.time) / 1.0f;

                // If this collision has run its course, remove it from the list
                if (progress > 1) {
                    iter.remove();
                    continue;
                }

                float alpha = 1.0f - progress;
                float radius = 10.0f * progress;

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                drawPosition(g2, collision.position, radius);
            }
        }
        
        g2.dispose();
    }
    
    private void drawTime(Graphics2D g2, Point position) {
        float time = scenario.getTime();
        float clockRadius = 5;
        float fontHeight = 10;
        float timeOffset = 3;
        
        float secondsAngle = -MathUtils.TWOPI * ((time / 60.0f) % 1.0f) - MathUtils.HALF_PI;
        float minuteAngle = -MathUtils.TWOPI * ((time / 3600.0f) % 1.0f) - MathUtils.HALF_PI;
        
        Point2D.Float center = new Point2D.Float(
                position.x + clockRadius,
                position.y + clockRadius);
        
        g2.draw(new Ellipse2D.Float(
                position.x, position.y,
                2 * clockRadius, 2 * clockRadius));
        
        g2.draw(new Line2D.Float(
                center.x, center.y,
                center.x - MathUtils.cos(secondsAngle) * clockRadius,
                center.y + MathUtils.sin(secondsAngle) * clockRadius));
        
        g2.draw(new Line2D.Float(
                center.x, center.y,
                center.x - MathUtils.cos(minuteAngle) * 0.5f * clockRadius,
                center.y + MathUtils.sin(minuteAngle) * 0.5f * clockRadius));
        
        g2.drawString(TimeUtil.formatTime(time), position.x + 2 * clockRadius + timeOffset, position.y + fontHeight);
    }
    
    private void drawScale(Graphics2D g2, Point position) {
        float fontHeight = 10;
        g2.drawString(String.format("%.2fpx/m", scale), position.x, position.y + fontHeight);
    }
}
