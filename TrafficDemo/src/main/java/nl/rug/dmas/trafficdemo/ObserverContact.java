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
public class ObserverContact {
    Observer observer;
    Fixture fixture;

    public ObserverContact(Contact contact) {
        if (contact.getFixtureA().getUserData() instanceof Observer) {
            observer = (Observer) contact.getFixtureA().getUserData();
            fixture = contact.getFixtureB();
        }
        else if (contact.getFixtureB().getUserData() instanceof Observer) {
            observer = (Observer) contact.getFixtureB().getUserData();
            fixture = contact.getFixtureA();
        }
    }
}
