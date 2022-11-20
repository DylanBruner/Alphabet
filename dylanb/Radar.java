package dylanb;

import java.util.Hashtable;
import java.awt.geom.*;

import robocode.*;
import robocode.util.Utils;

public class Radar {
    //Component stuff
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Radar");

    //Radar stuff
    public Enemy target;
    public Hashtable<String, Enemy> enemies = new Hashtable<String, Enemy>();

    public void init(Alphabet robot){
        alphabet = robot;
        logger.log("Radar initialized");
    }

    public void execute(){
        //alphabet.setTurnRadarRight(Double.POSITIVE_INFINITY);
        alphabet.turnRadarRightRadians(1);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        //Lock the radar on the enemy
        alphabet.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - alphabet.getRadarHeadingRadians()) * 2);

        //Update the enemy in our database
        if (enemies.containsKey(e.getName())){
            target = enemies.get(e.getName());
            target.update(e);
        } else {
            target = new Enemy(e, alphabet.myLocation);
            enemies.put(e.getName(), target);
        }
    }
}
