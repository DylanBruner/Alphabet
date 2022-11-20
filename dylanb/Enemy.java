package dylanb;

import java.awt.geom.*;

import robocode.ScannedRobotEvent;

public class Enemy {
    String name;
    double bearing, distance, energy, heading, velocity;
    Point2D.Double location;

    public Enemy(ScannedRobotEvent e, Point2D.Double myLocation){
        this.name     = e.getName();
        this.bearing  = e.getBearing();
        this.distance = e.getDistance();
        this.energy   = e.getEnergy();
        this.heading  = e.getHeading();
        this.velocity = e.getVelocity();
        this.location = new Point2D.Double(myLocation.x + Math.sin(Math.toRadians(e.getBearing())) * e.getDistance(), myLocation.y + Math.cos(Math.toRadians(e.getBearing())) * e.getDistance()); 
    }

    public void update(ScannedRobotEvent e){
        bearing  = e.getBearing();
        distance = e.getDistance();
        energy   = e.getEnergy();
        heading  = e.getHeading();
        velocity = e.getVelocity();
        location = new Point2D.Double(location.x + Math.sin(bearing) * distance, location.y + Math.cos(bearing) * distance);
    }
}
