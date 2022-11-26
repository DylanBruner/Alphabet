package dylanbruner;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.util.ArrayList;

/*                           (I have a fairly good understanding of it now)
 * I dont really know how this gun works, i mean i have a rough idea but thats
 * ok because i never gets used anyway 
*/

public class GuessFactorGun extends Component {
    //Component stuff
    AlphabetLogger logger = new AlphabetLogger("GuessFactorGun");

    //Gun stuff
    public static final int NUM_BINS = 31;
    ArrayList<WaveBullet> waves = new ArrayList<WaveBullet>();
    static int[][] stats          = new int[8][NUM_BINS];
    int direction               = 1;

    //Events 'n stuff
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();
        long time = alphabet.getTime();

        //TODO: Change radar access
        Enemy target = alphabet.radar.target; //For readability

        for (int i=0; i < waves.size(); i++){
            WaveBullet currentWave = waves.get(i);
            //If the wave has passed the enemy remove it
            if (currentWave.checkHit(target.location.getX(), target.location.getY(), time)){
                waves.remove(currentWave);
                i--;
            }
        }

        double power = alphabet.getFirePower();
        if (target.velocity != 0){
            if (Math.sin(e.getHeadingRadians() - absBearing) * target.velocity < 0){
                direction = -1;
            } else {
                direction = 1;
            }
        }

        int[] currentStats = stats[(int)(e.getDistance() / 200)];
        WaveBullet newWave = new WaveBullet(alphabet.myLocation, absBearing, power, direction, time, currentStats);

        //Fire the bullet
        int bestIndex = 15; //Start with the middle index
        for (int i=0; i < stats.length; i++){
            if (currentStats[i] < currentStats[bestIndex]){
                bestIndex = i;
            }
        }

        double guessFactor = (double)(bestIndex - (NUM_BINS - 1) / 2) / ((NUM_BINS - 1) / 2);
        double angleOffset = direction * guessFactor * newWave.maxEscapeAngle();
        double gunAdjust = Utils.normalRelativeAngle(absBearing - alphabet.getGunHeadingRadians() + angleOffset);

        alphabet.setTurnGunRightRadians(gunAdjust);

        //This should probably be moved into where it actually shoots
        //But the GuessFactorGun hardly gets selected (I think) and Linear
        //Targeting is used most of the time
        if (alphabet.getGunHeat() == 0 && gunAdjust < Math.atan2(9, e.getDistance()) && alphabet.setFireBullet(power) != null){
            waves.add(newWave);
        }
    }
    public double doGuessFactorGun(double absBearing, double power){
        //NOTE: RETURNS RELATIVE RADIANS

        long time = alphabet.getTime();
        //Return the angle we should turn the gun
        //TODO: Change radar access
        int[] currentStats = stats[(int)(alphabet.radar.target.distance / 200)];
        WaveBullet newWave = new WaveBullet(alphabet.myLocation, absBearing, power, direction, time, currentStats);

        //Fire the bullet
        int bestIndex = 15; //Start with the middle index
        for (int i=0; i < stats.length; i++){
            if (currentStats[i] < currentStats[bestIndex]){
                bestIndex = i;
            }
        }

        double guessFactor = (double)(bestIndex - (NUM_BINS - 1) / 2) / ((NUM_BINS - 1) / 2);
        double angleOffset = direction * guessFactor * newWave.maxEscapeAngle();
        double gunAdjust = Utils.normalRelativeAngle(absBearing - alphabet.getGunHeadingRadians() + angleOffset);

        return gunAdjust;
    }

    //Helper functions

    //Helper classes
    public class WaveBullet {
        private Point2D.Double startLocation;
        private long fireTime;
        private double startBearing, power;
        private int direction;
        private int[] returnSegment;

        public WaveBullet(Point2D.Double startLocation, double bearing, double power,
                          int direction, long time, int[] segment){
            this.startLocation = startLocation;
            this.startBearing  = bearing;
            this.power         = power;
            this.direction     = direction;
            this.fireTime      = time;
            this.returnSegment = segment;
        }

        public double getBulletSpeed(){
            return 20 - 3 * power;
        }

        public double maxEscapeAngle(){
            return Math.asin(8 / getBulletSpeed());
        }

        public boolean checkHit(double enemyX, double enemyY, long currentTime){
            Point2D.Double enemyLocation = new Point2D.Double(enemyX, enemyY);

            if (enemyLocation.distance(startLocation) <= (currentTime - fireTime) * getBulletSpeed()){
                double desiredDirection = Math.atan2(enemyX - startLocation.getX(), enemyY - startLocation.getY());
                double angleOffset      = Utils.normalRelativeAngle(desiredDirection - startBearing);
                double guessFactor      = Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direction;
                int index               = (int) Math.round((returnSegment.length - 1) / 2 * (guessFactor + 1));
                returnSegment[index]++;
                return true;
            }
            return false;
        }
    }
}