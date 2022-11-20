package dylanb;

import robocode.*;

public class GuessFactorGun {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("GuessFactorGun");

    public void init(Alphabet robot){
        alphabet = robot;
        logger.log("GuessFactorGun initialized");
    }

    public void execute(){}

    public void onScannedRobot(ScannedRobotEvent e) {}
    public void onBulletHit(BulletHitEvent e) {}
    public void onBulletMissed(BulletMissedEvent e) {}
}