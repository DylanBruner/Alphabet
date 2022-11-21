package dylanbruner;

import robocode.ScannedRobotEvent;
import java.awt.geom.*;

/*
 * Pattern Matching gun, code will be fully stolen from dylanb.xyz 
 * 
*/

public class PatternMatchGun {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("PatternMatchGun");

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    public double doPatternGun(ScannedRobotEvent e, double bulletPower){
        Enemy enemy = alphabet.radar.enemies.get(e.getName());
        if (enemy == null) {
            logger.warn("Enemy is null"); return 0;
        }

        Point2D.Double location = enemy.predictLocation(e, bulletPower, alphabet);
        //Return relative angle
        return MathUtils.getAngle(location, alphabet.myLocation) - alphabet.getGunHeadingRadians();
    }

    public void onScannedRobot(ScannedRobotEvent e){
        double bulletPower = alphabet.getFirePower();
        double gunRadians = doPatternGun(e, bulletPower);

        alphabet.setTurnGunRightRadians(gunRadians);
        alphabet.setFire(bulletPower);
    }
}
