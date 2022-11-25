package dylanbruner;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;

public class LinearGun extends Component {
    AlphabetLogger logger = new AlphabetLogger("LinearGun");
    
    private Point2D.Double estimateRobotLocation(ScannedRobotEvent robot, double bulletPower) {
        //Estimate the robot's location using basicish math
        double bulletVelocity = Rules.getBulletSpeed(bulletPower);
        double deltaTime = robot.getDistance() / bulletVelocity;
        double robotAngle = alphabet.getHeadingRadians() + robot.getBearingRadians();
        double robotX = alphabet.getX() + robot.getDistance() * Math.sin(robotAngle);
        double robotY = alphabet.getY() + robot.getDistance() * Math.cos(robotAngle);
        double robotHeading = robot.getHeadingRadians();
        double robotVelocity = robot.getVelocity();

        Point2D.Double loc = new Point2D.Double(robotX + robotVelocity * Math.sin(robotHeading) * deltaTime, robotY + robotVelocity * Math.cos(robotHeading) * deltaTime);
        //Limit the location to the field dimensions
        loc.x = Math.max(Math.min(loc.x, alphabet.getBattleFieldWidth()), 0);
        loc.y = Math.max(Math.min(loc.y, alphabet.getBattleFieldHeight()), 0);

        return loc;
    }

    public double doLinearGun(ScannedRobotEvent e, double bulletPower){
        //NOTE: RETURNS RELATIVE RADIANS
        Point2D.Double predictedEnemyLocation = estimateRobotLocation(e, bulletPower); //Guess location based on current movement
        return Utils.normalRelativeAngle(MathUtils.getAngle(predictedEnemyLocation, alphabet.myLocation) - alphabet.getGunHeadingRadians());
    }

    public void onScannedRobot(ScannedRobotEvent e){
        double bulletPower = alphabet.getFirePower();
        double gunRadians = doLinearGun(e, bulletPower);

        alphabet.setTurnGunRightRadians(gunRadians);
        alphabet.setFire(bulletPower);
    }
}
