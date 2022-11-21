package dylanbruner;

import robocode.*;
// import java.awt.geom.*;

/*
 * Going to remake this from scratch
 * 
*/

public class PatternMatchGun {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("PatternMatchGun");

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    public double doPatternGun(ScannedRobotEvent e, double bulletPower){
        return 0;
    }

    public void onScannedRobot(ScannedRobotEvent e){}
}