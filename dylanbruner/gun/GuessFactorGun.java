package dylanbruner.gun;

import java.awt.geom.Point2D;

import dylanbruner.Alphabet;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.MathUtils;
import robocode.BulletHitEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class GuessFactorGun extends Component {
    AlphabetLogger logger = new AlphabetLogger("GuessFactorGun");

    static int shots = 0;
    static int hits = 0;

    private static double lateralDirection;
    private static double lastEnemyVelocity;
    
    public void init(Alphabet alphabet) {
        super.init(alphabet);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double enemyAbsoluteBearing = alphabet.getHeadingRadians() + e.getBearingRadians();
        double enemyDistance = e.getDistance();
        double enemyVelocity = e.getVelocity();
        if (enemyVelocity != 0) {
            lateralDirection = MathUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
        }
        GFWave wave = new GFWave(alphabet);
        wave.gunLocation = new Point2D.Double(alphabet.getX(), alphabet.getY());
        GFWave.targetLocation = MathUtils.project(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
        wave.lateralDirection = lateralDirection;
        wave.bulletPower = alphabet.getFirePower();
        wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
        lastEnemyVelocity = enemyVelocity;
        wave.bearing = enemyAbsoluteBearing;
        alphabet.setTurnGunRightRadians(Utils
                .normalRelativeAngle(
                        enemyAbsoluteBearing - alphabet.getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
        if (alphabet.getEnergy() >= wave.bulletPower && alphabet.getGunHeat() == 0) {
            alphabet.setFire(wave.bulletPower);
            alphabet.addCustomEvent(wave);
            shots++;
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        hits++;
    }

    public void onRoundEnded(RoundEndedEvent e) {
        logger.log("Shots: " + shots + " Hits: " + hits + " Accuracy: " + (double) hits / shots);
    }
}
