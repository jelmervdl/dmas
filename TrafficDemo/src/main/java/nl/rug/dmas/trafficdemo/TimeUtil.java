/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.rug.dmas.trafficdemo;

/**
 *
 * @author jelmer
 */
class TimeUtil {

    static String formatTime(float time) {
        int hours = (int) (time / 3600);
        time -= hours * 3600;
        
        int minutes = (int) (time / 60);
        time -= minutes * 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02.2f", hours, minutes, time);
        } else {
            return String.format("%02d:%02.2f", minutes, time);
        }
    }
}
