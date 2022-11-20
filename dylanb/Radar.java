package dylanb;

import java.util.Hashtable;
//import java.awt.geom.*;

import robocode.*;
import robocode.util.Utils;

public class Radar {
    //Component stuff
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Radar");

    //Radar stuff
    public Enemy target;
    public Hashtable<String, Enemy> enemies = new Hashtable<String, Enemy>();
    public boolean targetLocked = false;

    public void init(Alphabet robot){
        alphabet = robot;
        logger.log("Radar initialized");

        target = new Enemy();
    }

    public void execute(){
        //alphabet.setTurnRadarRight(Double.POSITIVE_INFINITY);
        //alphabet.setTurnRadarRight(Double.POSITIVE_INFINITY);
        if (!targetLocked){
            alphabet.setTurnRadarRight(Double.POSITIVE_INFINITY);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        targetLocked = true;
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        //Lock the radar on the enemy
        if (alphabet.movementMode != alphabet.MOVEMENT_MELEE){
            alphabet.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - alphabet.getRadarHeadingRadians()) * 2);
        }

        //Update the enemy in our database
        if (enemies.containsKey(e.getName())){
            target = enemies.get(e.getName());
            target.update(e, alphabet);
        } else {
            target = new Enemy();
            target.populateData(e, alphabet.myLocation, alphabet);
            enemies.put(e.getName(), target);
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (enemies.containsKey(e.getName())){
            enemies.get(e.getName()).alive = false;
        }

        if (target.name.equals(e.getName())){
            targetLocked = false;
        }
    }
}
