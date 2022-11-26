package dylanbruner;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;

/*
 * Not used currently. At the moment we use MeleeRobot.java & SurfMovement.java 
 * 
*/

public class AntiGravity {
    //Components stuff
    Alphabet alphabet; //Parent robot
    AlphabetLogger logger = new AlphabetLogger("AntiGravity");

    static Point2D.Double[] enemyPoints = new Point2D.Double[66];
    int count;
    boolean hitRobot = false;
    Point2D.Double hitLocation = new Point2D.Double(0, 0);

    //Code

    public void init(Alphabet robot){
        alphabet = robot;
    }
    
    public void execute() {}

    //RoboCode events
    public void onScannedRobot(ScannedRobotEvent e){
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        //I know i should just loop through alphabet.radar.enemies later down but im lazy
        int index = 0;
        //TODO: Change radar access
        for (Enemy enemy : alphabet.radar.enemies.values()) {
            enemyPoints[index] = enemy.location;
            index++;
        }

        double xForce = 0;
        double yForce = 0;

        for (int i = 0; (i < alphabet.getOthers() && enemyPoints[i] != null); i++){
            absBearing = Utils.normalAbsoluteAngle(Math.atan2(enemyPoints[i].x-alphabet.myLocation.getX(),enemyPoints[i].y-alphabet.myLocation.getY()));
            double distance = enemyPoints[i].distance(alphabet.myLocation);

            xForce -= Math.sin(absBearing) / (distance * distance * 1.25);
            yForce -= Math.cos(absBearing) / (distance * distance * 1.25);
        }

        //Add wall avoidance
        //Add more force the closer we are to the wall
        xForce += 5000 / Math.pow(alphabet.myLocation.getX(), 2);
        xForce -= 5000 / Math.pow(alphabet.getBattleFieldWidth() - alphabet.myLocation.getX(), 2);
        yForce += 5000 / Math.pow(alphabet.myLocation.getY(), 2);
        yForce -= 5000 / Math.pow(alphabet.getBattleFieldHeight() - alphabet.myLocation.getY(), 2);

        //Add a little force to the center
        xForce += 100 / Math.pow(alphabet.myLocation.getX() - alphabet.getBattleFieldWidth()/2, 2);
        yForce += 100 / Math.pow(alphabet.myLocation.getY() - alphabet.getBattleFieldHeight()/2, 2);

        if (hitRobot) {
            hitRobot = false;
            //Apply a force away from the hit location
            xForce += 10000 / Math.pow(hitLocation.x - alphabet.myLocation.getX(), 2);
            yForce += 10000 / Math.pow(hitLocation.y - alphabet.myLocation.getY(), 2);
        }


        double angle = Math.atan2(xForce, yForce);

        if (xForce == 0 && yForce == 0){
            angle = alphabet.getHeadingRadians();
            //Add a random factor to the angle 0-10 degrees in radians
            angle += Math.random() * 0.174533;
            angle = wallSmoothing(angle);
            alphabet.setTurnRightRadians(Utils.normalRelativeAngle(angle - alphabet.getHeadingRadians()));
            alphabet.setAhead(100);
        } else if (Math.abs(angle-alphabet.getHeadingRadians())<Math.PI/2){
            angle = Utils.normalRelativeAngle(angle-Math.PI-alphabet.getHeadingRadians());
            angle = wallSmoothing(angle);
            alphabet.setAhead(Double.POSITIVE_INFINITY);
        } else {
            angle = Utils.normalRelativeAngle(angle-Math.PI-alphabet.getHeadingRadians());
            angle = wallSmoothing(angle);
            alphabet.setTurnRightRadians(angle);
            alphabet.setAhead(Double.NEGATIVE_INFINITY);
        }
    }

    public void onHitRobot(HitRobotEvent e){
        // logger.warn("Hit robot");
        hitRobot = true;
        //Calculate the rough location of the robot we cant get the exact because there is no e.getDistance()
        hitLocation = new Point2D.Double(alphabet.myLocation.getX() + Math.sin(e.getBearingRadians()) * 100, alphabet.myLocation.getY() + Math.cos(e.getBearingRadians()) * 100);
    }

    //Helpers
    public double wallSmoothing(double relativeAngle){
        double angle = alphabet.getHeadingRadians() + relativeAngle;
        double wallStick = 360;
        while (!MathUtils.fieldBox.contains(alphabet.myLocation.x + Math.sin(angle) * wallStick, alphabet.myLocation.y + Math.cos(angle) * wallStick)){
            angle += (relativeAngle < 0 ? -1 : 1) * 0.05;
        }
        return angle;
    }

    public void onPaint(java.awt.Graphics2D g){
        //Visualize the points
        for (int i = 0; (i < alphabet.getOthers() && enemyPoints[i] != null); i++){
            g.setColor(java.awt.Color.red);
            g.drawOval((int)enemyPoints[i].x-10, (int)enemyPoints[i].y-10, 20, 20);
        }
    }
}