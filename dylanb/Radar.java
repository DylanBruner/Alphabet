package dylanb;

import robocode.*;
import robocode.util.Utils;

public class Radar {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Radar");

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
    }
}
