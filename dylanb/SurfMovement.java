package dylanb;

import robocode.*;

public class SurfMovement {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("SurfMovement");

    public void init(Alphabet robot){
        logger.log("SurfMovment initialized");
        alphabet = robot;
    }

    public void execute(){}

    public void onScannedRobot(ScannedRobotEvent e) {}
    public void onHitByBullet(HitByBulletEvent e) {}
    public void onBulletHit(BulletHitEvent e) {}
}