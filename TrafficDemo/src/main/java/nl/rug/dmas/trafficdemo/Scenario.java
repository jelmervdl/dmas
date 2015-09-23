/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

/**
 * A scenario, which is simulated in a world and updates the state of the
 * cars and drivers from time step to time step.
 * @author jelmer
 */
public class Scenario {
    World world;
    
    // A list of all cars in the simulation.
    ArrayList<Car> cars = new ArrayList<>();
    
    // A map of locations known to all agents (such as the mouse ;) )
    Map<String, Object> commonKnowledge = new HashMap<>();
    
    final private ArrayList<Car> carsToRemove = new ArrayList<>();
    final private ArrayList<Car> carsToAdd = new ArrayList<>();
    
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    final Lock readLock = lock.readLock();
    final Lock writeLock = lock.writeLock();
    
    /**
     * A scenario takes an instance of a JBox2D world and sets the contact
     * listener. This listener updates the fixturesInSight list of the drivers
     * throughout the simulation.
     */
    public Scenario() {
        // Create a world without gravity (2d world seen from top, eh!)
        // The world is our physics simulation.
        world = new World(new Vec2(0, 0));

        // Keep a contact listener that monitors whether cars are in sight of
        // drivers.
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact fixtureContact) {
                DriverContact contact = new DriverContact(fixtureContact);
                
                if (contact.driver != null && contact.driver.car != contact.fixture.getUserData())
                    contact.driver.fixturesInSight.add(contact.fixture);
            }

            @Override
            public void endContact(Contact fixtureContact) {
                DriverContact contact = new DriverContact(fixtureContact);
                
                if (contact.driver != null && contact.driver.car != contact.fixture.getUserData())
                    contact.driver.fixturesInSight.remove(contact.fixture);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                //
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
               //
            }
            
        });
    }
    
    /**
     * Add a car to the simulation. If a car is added during a time step, the
     * addition is queued and the car is added once the time step has completed.
     * @param car to add
     */
    public void add(Car car) {
        if (writeLock.tryLock())
            try {
                addCarUnsafe(car);
            } finally {
                writeLock.unlock();
        } else {
            carsToAdd.add(car);
        }
    }
    
    private void addCarUnsafe(Car car) {
        car.initialize(world);
        cars.add(car);
    }
    
    /**
     * Remove a car from the simulation. If a car is removed during a time step,
     * it is only queued for removal until it will be removed once the time step
     * has completed.
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
        cars.remove(car);
    }
    
    /**
     * Steps the simulation of dt seconds. This locks the simulation.
     * @param dt delta time in seconds
     */
    public void step(float dt) {
        // For running the simulation we only need a read lock
        readLock.lock();
        try {
            for (Car car : cars) {
                car.driver.step();
                car.update(dt);
            }

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
                for (Car car : carsToRemove)
                    removeCarUnsafe(car);

                carsToRemove.clear();
            }

            // And process additions that were also queued
            if (!carsToAdd.isEmpty()) {
                for (Car car : carsToAdd)
                    addCarUnsafe(car);

                carsToAdd.clear();
            }
        }
        finally {
            writeLock.unlock();
        }       
    }
}
