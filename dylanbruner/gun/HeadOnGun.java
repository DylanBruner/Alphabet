package dylanbruner.gun;

import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import java.awt.geom.*;

import dylanbruner.data.Enemy;
import dylanbruner.data.Radar;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.MathUtils;

public class HeadOnGun extends Component {
    AlphabetLogger logger = new AlphabetLogger("HeadOnGun");

    public Point2D.Double doHeadOnGun(Enemy enemy, double bulletPower){
        double bulletSpeed = 20 - bulletPower * 3;
        double time = enemy.location.distance(alphabet.myLocation) / bulletSpeed;
        double futureX = enemy.location.x + enemy.velocity * Math.sin(enemy.heading) * time;
        double futureY = enemy.location.y + enemy.velocity * Math.cos(enemy.heading) * time;
        return new Point2D.Double(futureX, futureY);
    }

    public void onScannedRobot(ScannedRobotEvent e){
        Enemy enemy = ((Radar) alphabet.componentCore.getComponent("Radar")).enemies.get(e.getName());
        if (enemy == null || !enemy.initialized) return;

        double bulletPower = alphabet.getFirePower();

        Point2D.Double fireAt = doHeadOnGun(enemy, bulletPower);
        if (fireAt != null){
            alphabet.setFire(bulletPower);
            //Calculate the angle to the target
            double absFireRadians = MathUtils.absoluteBearing(alphabet.myLocation, fireAt);
            double angle = Utils.normalRelativeAngle(absFireRadians - alphabet.getGunHeadingRadians());
            alphabet.setTurnGunRightRadians(angle);
        }
    }
}