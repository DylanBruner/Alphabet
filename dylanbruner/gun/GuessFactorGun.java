package dylanbruner.gun;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;

import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.MathUtils;

public class GuessFactorGun extends Component {
    // Component stuff
    AlphabetLogger logger = new AlphabetLogger("GuessFactorGun");

    public static final double BULLET_POWER = 0.5;
    public static double lateralDirection;
    public static double lastEnemyVelocity;

    public void init(){
        lateralDirection  = 1;
        lastEnemyVelocity = 0;
    }

    public void onScannedRobot(ScannedRobotEvent e){
        if (alphabet.isTeammate(e.getName())) return;

        double enemyAbsoluteBearing = alphabet.getHeadingRadians() + e.getBearingRadians();
        double enemyDistance        = e.getDistance();
        double enemyVelocity        = e.getVelocity();
        if (enemyVelocity != 0){
            lateralDirection = MathUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
        }

        GFTWave wave = new GFTWave(alphabet);
        wave.gunLocation = alphabet.myLocation;
        GFTWave.targetLocation = project(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
        wave.lateralDirection = lateralDirection;
        wave.bulletPower = BULLET_POWER;
        wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
        lastEnemyVelocity = enemyVelocity;
        wave.bearing = enemyAbsoluteBearing;

        alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - alphabet.getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
        alphabet.setFire(wave.bulletPower);

        if (alphabet.getEnergy() >= BULLET_POWER){
            alphabet.addCustomEvent(wave);
        }
    }

	static Point2D project(Point2D sourceLocation, double angle, double length) {
		return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
				sourceLocation.getY() + Math.cos(angle) * length);
	}
}