package dylanbruner.gun;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;
import java.util.ArrayList;

import dylanbruner.data.EnemySnapshot;
import dylanbruner.data.Enemy;
import dylanbruner.data.Radar;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.MathUtils;

// import java.awt.geom.*;

/*
 * This gun needs to be reworked to properly predict the amount of snapshots we should skip 
*/

public class PatternMatchGun extends Component {
    AlphabetLogger logger = new AlphabetLogger("PatternMatchGun");

    //Stuff
    Point2D.Double guessLocation = new Point2D.Double(0, 0);

    public double getAverageMovement(Enemy enemy){
        double average = 0;
        for (EnemySnapshot snapshot : enemy.snapshots){
            average += snapshot.distance;
        }
        return average / enemy.snapshots.size();
    }

    private Point2D.Double estimateRobotLocation(ScannedRobotEvent robot, double bulletPower)
    {
        double bulletVelocity = Rules.getBulletSpeed(bulletPower);
        double deltaTime = robot.getDistance() / bulletVelocity;
        double robotAngle = alphabet.getHeadingRadians() + robot.getBearingRadians();
        double robotX = alphabet.myLocation.getX() + robot.getDistance() * Math.sin(robotAngle);
        double robotY = alphabet.myLocation.getY() + robot.getDistance() * Math.cos(robotAngle);
        double robotHeading = robot.getHeadingRadians();
        double robotVelocity = robot.getVelocity();

        Point2D.Double loc = new Point2D.Double(robotX + robotVelocity * Math.sin(robotHeading) * deltaTime, robotY + robotVelocity * Math.cos(robotHeading) * deltaTime);
        //Limit the location to the field dimensions
        loc.x = Math.max(Math.min(loc.x, alphabet.getBattleFieldWidth()), 0);
        loc.y = Math.max(Math.min(loc.y, alphabet.getBattleFieldHeight()), 0);

        return loc;
    }

    public double getPatternDiffrence(ArrayList<EnemySnapshot> pattern1, ArrayList<EnemySnapshot> pattern2){
        //Calculate the diffrence between two patterns using velocity and heading
        double diffrence = 0;
        for(int i = 0; i < pattern1.size(); i++){
            diffrence += Math.abs(pattern1.get(i).velocity - pattern2.get(i).velocity);
            diffrence += Math.abs(pattern1.get(i).heading - pattern2.get(i).heading);
        }
        return diffrence;
    }

    public Point2D.Double predictLocationFromPattern(EnemySnapshot[] pattern, Point2D.Double predictedEnemyLocation){
        for (EnemySnapshot snapshot : pattern){
            double snapshotHeading          = snapshot.heading;

            //Adjust predicted enemy location based on the snapshot's velocity and heading
            predictedEnemyLocation = MathUtils.project(predictedEnemyLocation, snapshotHeading, snapshot.velocity);
        }
        return predictedEnemyLocation;
    }

    public Point2D.Double doPatternGun(ScannedRobotEvent e, double bulletPower){
        if (((Radar) alphabet.componentCore.getComponent("Radar")).target == null || 
           !((Radar) alphabet.componentCore.getComponent("Radar")).target.initialized){
            return estimateRobotLocation(e, bulletPower); //If pattern matching failed, use basic estimation math
        }

        Enemy enemy = ((Radar) alphabet.componentCore.getComponent("Radar")).target;
        int snapshotsToUse = (int)(Math.abs(getAverageMovement(enemy) * MathUtils.bulletVelocity(bulletPower)));
        ArrayList<EnemySnapshot> snapshots = ((Radar) alphabet.componentCore.getComponent("Radar")).target.snapshots;

        //Get the latest n snapshots to be used as the pattern
        ArrayList<EnemySnapshot> pattern = new ArrayList<EnemySnapshot>();
        for (int i = snapshots.size() - 1; i >= 0; i--) {
            if (pattern.size() < snapshotsToUse) {pattern.add(snapshots.get(i));
            } else {break;}
        }
        
        ArrayList<EnemySnapshot> closestPattern = new ArrayList<EnemySnapshot>();

        //Loop through snapshots in chunks of n
        for (int i = 0; i < snapshots.size() - snapshotsToUse; i++) {
            //Get the current n snapshots
            ArrayList<EnemySnapshot> currentPattern = new ArrayList<EnemySnapshot>();
            for (int j = i; j < i + snapshotsToUse; j++) {
                currentPattern.add(snapshots.get(j));
            }

            if (closestPattern.size() == 0) {closestPattern = currentPattern;
            } else {
                //Get the difference between the current pattern and the pattern we are trying to match
                double difference = getPatternDiffrence(currentPattern, pattern);

                //Get the difference between the closest pattern and the pattern we are trying to match
                double closestDifference = getPatternDiffrence(closestPattern, pattern);

                //If the current pattern is closer to the pattern we are trying to match, set it as the closest pattern
                if (difference < closestDifference) {
                    closestPattern = currentPattern;
                }
            }
        }

        double difference = getPatternDiffrence(closestPattern, pattern);
        if (difference > 18_000) {
            //System.out.println("[DEBUG] Pattern matching failed, difference: " + difference);
            return estimateRobotLocation(e, bulletPower); //If pattern matching failed, use basic estimation math
        }

        //Get the enemy's current location
        Point2D.Double enemyLocation          = MathUtils.project(alphabet.myLocation, alphabet.getHeadingRadians() + e.getBearingRadians(), e.getDistance());
        Point2D.Double predictedEnemyLocation = new Point2D.Double(enemyLocation.x, enemyLocation.y); //Make a copy of the enemy's location

        predictedEnemyLocation = predictLocationFromPattern(closestPattern.toArray(new EnemySnapshot[closestPattern.size()]), predictedEnemyLocation);

        //Limit the predicted enemy location to the field dimensions using Math.min and Math.max
        predictedEnemyLocation.x = Math.max(Math.min(predictedEnemyLocation.x, alphabet.getBattleFieldWidth()), 0);
        predictedEnemyLocation.y = Math.max(Math.min(predictedEnemyLocation.y, alphabet.getBattleFieldHeight()), 0);
    

        return predictedEnemyLocation;
    }

    public void onScannedRobot(ScannedRobotEvent e){
        if (alphabet.isTeammate(e.getName())) return;

        double bulletPower = alphabet.getFirePower();
        Point2D.Double location = doPatternGun(e, bulletPower);
        //Get the angle to the predicted location
        double angle = MathUtils.absoluteBearing(alphabet.myLocation, location);
        //Turn the gun to the angle
        alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(angle - alphabet.getGunHeadingRadians()));
        //Fire the bullet
        alphabet.setFire(bulletPower);
    }
}