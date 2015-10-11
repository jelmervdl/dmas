/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import nl.rug.dmas.trafficdemo.actors.AutonomousDriver;
import nl.rug.dmas.trafficdemo.actors.Driver;
import nl.rug.dmas.trafficdemo.actors.HumanDriver;
import nl.rug.dmas.trafficdemo.actors.StreetGraphSink;
import nl.rug.dmas.trafficdemo.actors.StreetGraphSource;
import nl.rug.dmas.trafficdemo.streetgraph.NoPathException;
import nl.rug.dmas.trafficdemo.streetgraph.PointPath;
import nl.rug.dmas.trafficdemo.streetgraph.StreetGraph;
import nl.rug.dmas.trafficdemo.streetgraph.Vertex;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * A scenario, which is simulated in a world and updates the state of the cars
 * and drivers from time step to time step.
 *
 * @author jelmer
 */
public class Scenario {

    World world;

    // A list of all cars in the simulation.
    final ArrayList<Car> cars = new ArrayList<>();

    // A list of actors, agents or objects that can act, such as drivers
    // and spawn points.
    final Map<Actor, Long> actors = new HashMap<>();

    // A map of locations known to all agents (such as the mouse ;) )
    Map<String, Object> commonKnowledge = new HashMap<>();

    final private ArrayList<Car> carsToRemove = new ArrayList<>();
    final private ArrayList<Car> carsToAdd = new ArrayList<>();

    final Set<Car> selectedCars = new HashSet<>();

    final StreetGraph streetGraph;

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    final Lock readLock = lock.readLock();
    final Lock writeLock = lock.writeLock();

    private Thread mainLoop;
    
    final Set<ScenarioListener> listeners = new HashSet<>();
    
    private float time = 0f;
    
    final private Random oracle = new Random();
    
    final Parameter carWidth = Parameter.fromString("1.47-2.55");
    final Parameter carLength = Parameter.fromString("2.540-6.0"); // Parameter.fromString("2.540-12.0");
    
    /**
     * A scenario takes an instance of a JBox2D world and sets the contact
     * listener. This listener updates the fixturesInSight list of the drivers
     * throughout the simulation.
     *
     * @param graph Graph of the streets of the world
     */
    public Scenario(StreetGraph graph) {
        streetGraph = graph;

        // Create a world without gravity (2d world seen from top, eh!)
        // The world is our physics simulation.
        world = new World(new Vec2(0, 0));

        // Add actors for the spawn points and sinks of the street graph
        for (Vertex source : streetGraph.getSources())
            actors.put(new StreetGraphSource(this, source, 1.0f), 0l);
        
        for (Vertex sink : streetGraph.getSinks())
            actors.put(new StreetGraphSink(this, sink), 0l);
        
        // Keep a contact listener that monitors whether cars are in sight of
        // drivers.
        world.setContactListener(new ObserverContactListener());
    }

    public World getWorld() {
        return world;
    }

    public StreetGraph getStreetGraph() {
        return streetGraph;
    }

    public Map<String, Object> getCommonKnowledge() {
        return commonKnowledge;
    }
    
    public float getTime() {
        return time;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Listener methods">
    
    public void addListener(ScenarioListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ScenarioListener listener) {
        listeners.remove(listener);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Selection methods">
    
    public Collection<Car> getSelection() {
        return Collections.unmodifiableSet(selectedCars);
    }
    
    public void setSelection(Collection<Car> cars) {
        selectedCars.clear();
        selectedCars.addAll(cars);
        
        for (ScenarioListener listener : listeners)
            listener.selectionChanged();
    }
    
    public void addSelection(Car car) {
        selectedCars.add(car);
        
        for (ScenarioListener listener : listeners)
            listener.selectionChanged();
    }
    
    public void clearSelection() {
        selectedCars.clear();
        
        for (ScenarioListener listener : listeners)
            listener.selectionChanged();
    }
    
    // </editor-fold>
    
    public PointPath createPath(Vertex origin) throws NoPathException {
        List<Vertex> destinations = streetGraph.getSinks();
        Collections.shuffle(destinations);

        for (Vertex destination : destinations) {
            try {
                return streetGraph.generatePointPath(origin, destination);
            } catch (NoPathException e) {
                // Try the next one
            }
        }

        throw new NoPathException();
    }
    
    /**
     * Create a clueless driver. Mostly used when you add a car through the
     * menu, this factory method will give you a driver that drives along the
     * drawn path.
     * @return 
     */
    public Driver createDriver() {
        return new AutonomousDriver(this, (List<Vec2>) commonKnowledge.get("path"));
    }
    
    /**
     * Create a purposeful driver. This driver will try to drive along the path
     * you pass to this factory method.
     * Todo: here we should create drivers randomly according to the ratio human
     * vs autonomous we want to test.
     * @param path
     * @return a driver!
     */
    public Driver createDriver(PointPath path) {
        return oracle.nextBoolean()
            ? new HumanDriver(this, path)
            : new AutonomousDriver(this, path);
    }
    
    public Car createCar(Driver driver, Vec2 position, float angle) {
        return new Car(driver, carWidth.getValue(oracle), carLength.getValue(oracle), position, angle);
    }
    
    private float getRandomFloatBetween(float lower, float upper) {
        return lower + oracle.nextFloat() * (upper - lower);
    }
    
    /**
     * Add a car to the simulation. If a car is added during a time step, the
     * addition is queued and the car is added once the time step has completed.
     *
     * @param car to add
     */
    public void add(Car car) {
        if (writeLock.tryLock()) {
            try {
                addCarUnsafe(car);
            } finally {
                writeLock.unlock();
            }
        } else {
            carsToAdd.add(car);
        }
    }

    private void addCarUnsafe(Car car) {
        car.initialize(world);
        cars.add(car);
        actors.put(car.driver, 0l);
        
        for (ScenarioListener listener : listeners)
            listener.carAdded(car);
    }

    /**
     * Remove a car from the simulation. If a car is removed during a time step,
     * it is only queued for removal until it will be removed once the time step
     * has completed.
     *
     * @param car to remove
     */
    public void remove(Car car) {
        if (writeLock.tryLock()) {
            try {
                removeCarUnsafe(car);
            } finally {
                writeLock.unlock();
            }
        } else {
            carsToRemove.add(car);
        }
    }

    private void removeCarUnsafe(Car car) {
        car.destroy(world);
        actors.remove(car.driver);
        cars.remove(car);
        selectedCars.remove(car);
        
        for (ScenarioListener listener : listeners)
            listener.carRemoved(car);
    }

    /**
     * Start the main loop of the simulation. The simulation is executed in its
     * own thread.
     */
    public void start() {
        if (mainLoop != null)
            return;
        
        mainLoop = new Thread(new MainLoop(60));
        mainLoop.start();
    }

    /**
     * Stop the main loop of the simulation. Effectively interrupts the thread,
     * nothing more.
     * @throws java.lang.InterruptedException
     */
    public void stop() throws InterruptedException {
        if (mainLoop == null)
            return;
        
        mainLoop.interrupt();
        mainLoop.join();
        mainLoop = null;
    }

    /**
     *
     * @param time
     * @throws java.lang.InterruptedException
     */
    public void jumpTo(float time) throws InterruptedException {
        stop();
        
        mainLoop = new Thread(new JumpLoop(60, time));
        mainLoop.start();
        mainLoop.join();
        mainLoop = null;
    }
    
    /**
    * Steps the simulation of dt seconds. This locks the simulation.
    * @param dt delta time in seconds
    */
    private void step(float dt) {
        // For running the simulation we only need a read lock
        readLock.lock();
        try {
            for (Map.Entry<Actor,Long> entry : actors.entrySet()) {
                // Only run the actor if their last act was long enough ago
                // Todo: getActPeriod should return a time as float in seconds.
                if (entry.getValue() + entry.getKey().getActPeriod() < (long) (time * 1000)) {
                    entry.getKey().act();
                    entry.setValue((long) time * 1000);
                }
            }

            for (Car car : cars)
                car.update(dt);

            world.step(dt, 3, 8);
        } finally {
            readLock.unlock();
        }

        // For altering the cars that need to be added or removed we want a
        // complete lock.
        writeLock.lock();
        try {
            // Process removals that were queued
            if (!carsToRemove.isEmpty()) {
                for (Car car : carsToRemove) {
                    removeCarUnsafe(car);
                }

                carsToRemove.clear();
            }

            // And process additions that were also queued
            if (!carsToAdd.isEmpty()) {
                for (Car car : carsToAdd) {
                    addCarUnsafe(car);
                }

                carsToAdd.clear();
            }
        } finally {
            writeLock.unlock();
        }

        for (ScenarioListener listener : listeners)
            listener.scenarioStepped();
    }
    
    private class ObserverContactListener implements ContactListener {

        @Override
        public void beginContact(Contact fixtureContact) {
            ObserverContact contact = new ObserverContact(fixtureContact);

            if (contact.observer != null) {
                contact.observer.addFixtureInSight(contact.fixture);
            }
        }

        @Override
        public void endContact(Contact fixtureContact) {
            ObserverContact contact = new ObserverContact(fixtureContact);

            if (contact.observer != null) {
                contact.observer.removeFixtureInSight(contact.fixture);
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            //
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            //
        }
    }

    class MainLoop implements Runnable {

        final private int hz;
        
        public MainLoop(int hz) {
            this.hz = hz;
        }

        @Override
        public void run() {
            // Let everyone know that we've started.
            for (ScenarioListener listener : listeners) {
                listener.scenarioStarted();
            }

            try {
                float stepTime = 1.0f / (float) hz;

                while (!Thread.interrupted()) {
                    long startTimeMS = System.currentTimeMillis();

                    step(stepTime);
                    time += stepTime;
                    
                    long finishTimeMS = System.currentTimeMillis();
                    long sleepTimeMS = (long) (stepTime * 1000) - (finishTimeMS - startTimeMS);
                    
                    if (sleepTimeMS > 0) {
                        Thread.sleep(sleepTimeMS);
                    }
                }
            } catch (InterruptedException e) {
                // Just stop the mainloop
            }
            
            // Let everyone know that we've stopped.
            for (ScenarioListener listener : listeners) {
                listener.scenarioStopped();
            }
        }
    }
    
    class JumpLoop implements Runnable {

        final private int hz;
        
        final private float targetTime;
        
        public JumpLoop(int hz, float targetTime) {
            this.hz = hz;
            this.targetTime = targetTime;
        }

        @Override
        public void run() {
            float stepTime = 1.0f / (float) hz;
            float startTime = time;
            
            for (ScenarioListener listener : listeners) {
                listener.scenarioStarted();
            }
            
            while (!Thread.interrupted() && time < targetTime) {
                step(stepTime);
                time += stepTime;
            }
            
            for (ScenarioListener listener : listeners) {
                listener.scenarioStopped();
            }
        }
    }
}
