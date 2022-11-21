package dylanb;

import java.awt.geom.*;
import java.util.ArrayList;

import robocode.ScannedRobotEvent;

public class Enemy {
    String name;
    double bearing, distance, energy, heading, velocity, lastVelocity, lastDistance, absBearing, bearingRadians, absBearingRadians;
    Point2D.Double location, lastLocation;
    ScannedRobotEvent lastScan;

    //Data gathering
    public ArrayList<EnemySnapshot> snapshots = new ArrayList<EnemySnapshot>();

    //Gun accuracy trackers
    public int tracker_linearGun      = 0;
    public int tracker_guessFactorGun = 0; 

    boolean alive = true;
    boolean initialized = false;

    public boolean isIdle(){
        //check the last 20 snapshots (or less if we don't have that many)
        int snapshotsWeCanCheck = snapshots.size() < 20 ? snapshots.size() : 20;
        for (int i = 0; i < snapshotsWeCanCheck; i++){
            if (snapshots.get(i).velocity != 0){
                return false;
            }
        }
        return true;
    }

    public int getPatternDiffrence(ArrayList<EnemySnapshot> p1, ArrayList<EnemySnapshot> p2){
        int difference = 0;
        for (int i = 0; i < p1.size(); i++) {
            difference += p1.get(i).getDifference(p2.get(i));
        }
        return difference;
    }

    public Point2D.Double predictLocationFromPattern(EnemySnapshot[] pattern, Point2D.Double predictedEnemyLocation){
        for (EnemySnapshot snapshot : pattern){
            double snapshotHeading          = snapshot.heading;

            //Adjust predicted enemy location based on the snapshot's velocity and heading
            predictedEnemyLocation = MathUtils.project(predictedEnemyLocation, snapshotHeading, snapshot.velocity);
        }
        return predictedEnemyLocation;
    }

    public double getAverageMovement(){
        double totalMovement = 0;
        for (int i = 0; i < snapshots.size() - 1; i++) {
            //If distance is 0 skip it
            if (snapshots.get(i).distance == 0) {continue;}
            totalMovement += snapshots.get(i).location.distance(snapshots.get(i + 1).location);
        }
        return totalMovement / (snapshots.size() - 1);
    }

    public Point2D.Double predictLocation(ScannedRobotEvent robot, double bulletPower, Alphabet alphabet){
        //int snapshotsToUse = 14; //Amount of snapshots to use for pattern matching and prediction
        int snapshotsToUse = (int)(Math.abs(getAverageMovement() * MathUtils.bulletVelocity(bulletPower)));
        //System.out.println("Using " + snapshotsToUse + " snapshots for pattern matching and prediction");

        //Get the latest n snapshots to be used as the pattern
        ArrayList<EnemySnapshot> pattern = new ArrayList<EnemySnapshot>();
        for (int i = snapshots.size() - 1; i >= 0; i--) {
            if (pattern.size() < snapshotsToUse) {pattern.add(snapshots.get(i));
            } else {break;}}
        
        ArrayList<EnemySnapshot> closestPattern = new ArrayList<EnemySnapshot>();

        //Search through your history of enemy movements and find the series of 7 snapshots that most closely matches
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
                int difference = getPatternDiffrence(currentPattern, pattern);

                //Get the difference between the closest pattern and the pattern we are trying to match
                int closestDifference = getPatternDiffrence(closestPattern, pattern);

                //If the current pattern is closer to the pattern we are trying to match, set it as the closest pattern
                if (difference < closestDifference) {
                    closestPattern = currentPattern;
                }
            }
        }

        double difference = getPatternDiffrence(closestPattern, pattern);
        if (difference > 18_000) {
            //System.out.println("[DEBUG] Pattern matching failed, difference: " + difference);
            double angle = alphabet.linearGun.doLinearGun(robot, bulletPower);
            return MathUtils.project(location, angle, 18_000);
        }

        //Get the enemy's current location
        Point2D.Double enemyLocation          = MathUtils.project(alphabet.myLocation, alphabet.getHeadingRadians() + robot.getBearingRadians(), robot.getDistance());
        Point2D.Double predictedEnemyLocation = new Point2D.Double(enemyLocation.x, enemyLocation.y); //Make a copy of the enemy's location

        predictedEnemyLocation = predictLocationFromPattern(closestPattern.toArray(new EnemySnapshot[closestPattern.size()]), predictedEnemyLocation);

        //Adjust the predicted enemy location based on how long it will take for the bullet to reach it
        //double bulletTravelTime = enemyLocation.distance(getLocation()) / bulletVelocity(bulletPower);
        //predictedEnemyLocation = project(predictedEnemyLocation, robot.getHeadingRadians(), robot.getVelocity() * bulletTravelTime);

        //Limit the predicted enemy location to the field dimensions using Math.min and Math.max
        predictedEnemyLocation.x = Math.max(Math.min(predictedEnemyLocation.x, alphabet.getBattleFieldWidth()), 0);
        predictedEnemyLocation.y = Math.max(Math.min(predictedEnemyLocation.y, alphabet.getBattleFieldHeight()), 0);
    

        return predictedEnemyLocation;
    }

    public void populateData(ScannedRobotEvent e, Point2D.Double myLocation, Alphabet alphabet){
        this.name     = e.getName();
        this.bearing  = e.getBearing();
        this.bearingRadians = e.getBearingRadians();
        this.lastScan = e;
        this.absBearingRadians = bearingRadians + alphabet.getHeadingRadians();
        this.absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();
        this.distance = e.getDistance();
        this.lastDistance = this.distance;
        this.energy   = e.getEnergy();
        this.heading  = e.getHeading();
        this.velocity = e.getVelocity();
        this.lastVelocity = this.velocity;
        this.location = new Point2D.Double(myLocation.x + Math.sin(Math.toRadians(e.getBearing())) * e.getDistance(), myLocation.y + Math.cos(Math.toRadians(e.getBearing())) * e.getDistance()); 
        this.lastLocation = this.location;
        this.initialized = true;
    }

    public void update(ScannedRobotEvent e, Alphabet alphabet){
        bearing  = e.getBearing();
        bearingRadians = e.getBearingRadians();
        absBearingRadians = bearingRadians + alphabet.getHeadingRadians();
        absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();
        lastDistance = distance;
        distance = e.getDistance();
        energy   = e.getEnergy();
        heading  = e.getHeading();
        lastVelocity = velocity;
        lastScan = e;   
        velocity = e.getVelocity();
        lastLocation = new Point2D.Double(location.x, location.y);

        location = new Point2D.Double(alphabet.myLocation.x + Math.sin(Math.toRadians(alphabet.getHeading() + e.getBearing())) * e.getDistance(), 
									  alphabet.myLocation.y + Math.cos(Math.toRadians(alphabet.getHeading() + e.getBearing())) * e.getDistance());
    }
}