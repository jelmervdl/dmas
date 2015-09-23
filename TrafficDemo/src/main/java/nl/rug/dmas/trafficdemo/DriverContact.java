/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

/**
 *
 * @author jelmer
 */
public class DriverContact {
    Driver driver;
    Fixture fixture;

    public DriverContact(Contact contact) {
        if (contact.getFixtureA().getUserData() instanceof Driver) {
            driver = (Driver) contact.getFixtureA().getUserData();
            fixture = contact.getFixtureB();
        }
        else if (contact.getFixtureB().getUserData() instanceof Driver) {
            driver = (Driver) contact.getFixtureB().getUserData();
            fixture = contact.getFixtureA();
        }
    }
}
