package dylanb;

import robocode.*;
import robocode.util.Utils;

import java.util.ArrayList;

//List of points that effect the robot "n" negativly and changes based on distance
//The robot will always be moving
//Find best location with the least negative charge

public class AntiGravity {
    //Components stuff
    Alphabet alphabet; //Parent robot
    AlphabetLogger logger = new AlphabetLogger("MeleeMovement");

    //IDS n' sh
    public static final int OTHER       = -1;
    public static final int ENEMY_POINT = 0;
    public static final int WALL_POINT  = 1;

    public static ArrayList<Integer> points = new ArrayList<Integer>();

    //Code

    public void init(Alphabet robot){
        alphabet = robot;
        logger.log("MeleeMovement initialized");
    }
    
    public void execute() {}

    //RoboCode events
    public void onScannedRobot(ScannedRobotEvent e){
        double xForce = 0; double yForce = 0;


        for (Enemy enemy : alphabet.radar.enemies.values()){
            if (enemy.alive){
                //Calculate the force of the enemy
                double distanceToEnemy = getDistanceToEnemy(enemy);
                if (!alphabet.radar.enemies.get(e.getName()).initialized) continue;
                double absBearing = alphabet.radar.enemies.get(e.getName()).absBearing;

                xForce -= Math.sin(absBearing) / (distanceToEnemy * distanceToEnemy) * Config.FORCE_ENEMY;
                yForce -= Math.cos(absBearing) / (distanceToEnemy * distanceToEnemy) * Config.FORCE_ENEMY;
            }
        }

        //Subtract more force if we are close to a wall
        //double distanceToWall = getDistanceToWall();
        //xForce -= Math.sin(alphabet.getHeadingRadians()) / (distanceToWall * distanceToWall);
        //yForce -= Math.cos(alphabet.getHeadingRadians()) / (distanceToWall * distanceToWall);


        //logger.log("X: " + xForce + " Y: " + yForce);

        double angle = Math.atan2(xForce, yForce);
        if (xForce <= 0.001 && yForce <= 0.001){
            if (Math.random() < 0.5){
                alphabet.setAhead(Double.POSITIVE_INFINITY);
            } else {
                alphabet.setAhead(Double.NEGATIVE_INFINITY);
            }

        } else if(Math.abs(angle-alphabet.getHeadingRadians())<Math.PI/2){
            angle = MathUtils.wallSmoothing(alphabet.myLocation, angle, 1);
            alphabet.setTurnRightRadians(Utils.normalRelativeAngle(angle-alphabet.getHeadingRadians()));
            alphabet.setAhead(Double.POSITIVE_INFINITY);
        } else {
            angle = MathUtils.wallSmoothing(alphabet.myLocation, angle, -1);
            alphabet.setTurnRightRadians(Utils.normalRelativeAngle(angle+Math.PI-alphabet.getHeadingRadians()));
            alphabet.setAhead(Double.NEGATIVE_INFINITY);
        }
    }

    public void onHitRobot(HitRobotEvent e){
        //If we hit a robot, we want to move away from it
        double angle = Math.atan2(alphabet.myLocation.x - e.getBearing(), alphabet.myLocation.y - e.getBearing());
        if (Math.abs(angle-alphabet.getHeadingRadians())<Math.PI/2){
            angle = MathUtils.wallSmoothing(alphabet.myLocation, angle, 1);
            alphabet.setTurnRightRadians(Utils.normalRelativeAngle(angle-alphabet.getHeadingRadians()));
            alphabet.setAhead(Double.POSITIVE_INFINITY);
        } else {
            angle = MathUtils.wallSmoothing(alphabet.myLocation, angle, -1);
            alphabet.setTurnRightRadians(Utils.normalRelativeAngle(angle+Math.PI-alphabet.getHeadingRadians()));
            alphabet.setAhead(Double.NEGATIVE_INFINITY);
        }
    }

    //Helpers

    public double getDistanceToEnemy(Enemy enemy){
        double xDistance = alphabet.myLocation.getX() - enemy.location.getX();
        double yDistance = alphabet.myLocation.getY() - enemy.location.getY();
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }

    public double getDistanceToWall(){
        double xDistance = alphabet.myLocation.getX() - alphabet.getBattleFieldWidth();
        double yDistance = alphabet.myLocation.getY() - alphabet.getBattleFieldHeight();
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }

    public void onPaint(java.awt.Graphics2D g){
        //Visualize the points

    }
}