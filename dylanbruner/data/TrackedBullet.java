package dylanbruner.data;

import java.awt.geom.*;

import dylanbruner.Alphabet;

public class TrackedBullet {
    Point2D.Double fireLocation;
    double absFireRadians, power;
    long fireTime;
    int parentGun;

    public TrackedBullet copy(){
        TrackedBullet copy = new TrackedBullet();
        copy.fireLocation = fireLocation;
        copy.absFireRadians = absFireRadians;
        copy.fireTime = fireTime;
        copy.parentGun = parentGun;
        return copy;
    }

    public Point2D.Double getLocation(Alphabet alphabet){
        //Calculate the bullet location
        double bulletSpeed = 20 - 3 * power;
        double bulletTravelDistance = (alphabet.getTime() - fireTime) * bulletSpeed;
        return new Point2D.Double(fireLocation.x + Math.sin(absFireRadians) * bulletTravelDistance, 
                                  fireLocation.y + Math.cos(absFireRadians) * bulletTravelDistance);
    }

    // TrackedBullet (Point2D.Double fireLocation, double absFireRadians, int parentGun){
    //     this.fireLocation   = fireLocation;
    //     this.absFireRadians = absFireRadians;
    //     this.fireTime       = alphabet.getTime();
    //     this.parentGun      = parentGun;
    // }
}