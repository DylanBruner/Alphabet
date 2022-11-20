package dylanb;

import java.awt.geom.*;
import robocode.*;

public class EnemySnapshot {
    double bearing, distance, energy, heading, velocity;
    Point2D.Double location;

    EnemySnapshot (ScannedRobotEvent e, Point2D.Double myLocation){
        bearing  = e.getBearing();
        distance = e.getDistance();
        energy   = e.getEnergy();
        heading  = e.getHeading();
        velocity = e.getVelocity();
        location = new Point2D.Double(myLocation.x + Math.sin(Math.toRadians(e.getBearing())) * e.getDistance(), myLocation.y + Math.cos(Math.toRadians(e.getBearing())) * e.getDistance()); 
    }
}
