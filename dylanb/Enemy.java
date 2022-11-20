package dylanb;

import java.awt.geom.*;

import robocode.ScannedRobotEvent;

public class Enemy {
    String name;
    double bearing, distance, energy, heading, velocity, lastVelocity, lastDistance, absBearing;
    Point2D.Double location, lastLocation;

    public Enemy(ScannedRobotEvent e, Point2D.Double myLocation, Alphabet alphabet){
        this.name     = e.getName();
        this.bearing  = e.getBearing();
        this.absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();
        this.distance = e.getDistance();
        this.lastDistance = this.distance;
        this.energy   = e.getEnergy();
        this.heading  = e.getHeading();
        this.velocity = e.getVelocity();
        this.lastVelocity = this.velocity;
        this.location = new Point2D.Double(myLocation.x + Math.sin(Math.toRadians(e.getBearing())) * e.getDistance(), myLocation.y + Math.cos(Math.toRadians(e.getBearing())) * e.getDistance()); 
        this.lastLocation = this.location;
    }

    public void update(ScannedRobotEvent e, Alphabet alphabet){
        bearing  = e.getBearing();
        absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();
        lastDistance = distance;
        distance = e.getDistance();
        energy   = e.getEnergy();
        heading  = e.getHeading();
        lastVelocity = velocity;
        velocity = e.getVelocity();
        lastLocation = location;
        location = new Point2D.Double(location.x + Math.sin(bearing) * distance, location.y + Math.cos(bearing) * distance);
    }
}
