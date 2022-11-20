package dylanb;

import java.awt.geom.*;

import robocode.ScannedRobotEvent;

public class Enemy {
    String name;
    double bearing, distance, energy, heading, velocity, lastVelocity, lastDistance, absBearing, bearingRadians, absBearingRadians;
    Point2D.Double location, lastLocation;
    ScannedRobotEvent lastScan;

    //Gun accuracy trackers
    public int tracker_linearGun      = 0;
    public int tracker_guessFactorGun = 0; 

    boolean alive = true;
    boolean initialized = false;

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