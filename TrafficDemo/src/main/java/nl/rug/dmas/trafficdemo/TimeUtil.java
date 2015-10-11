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
        
        int seconds = (int) time;
        time -= seconds;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, (int) (time * 100));
        } else {
            return String.format("%02d:%02d.%02d", minutes, seconds, (int) (time * 100));
        }
    }
}
