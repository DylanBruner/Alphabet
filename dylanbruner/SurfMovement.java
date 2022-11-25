package dylanbruner;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.util.ArrayList;

public class SurfMovement extends Component {
    AlphabetLogger logger = new AlphabetLogger("SurfMovement");

    //This stores how often the gun shoots at us from a certain angle
    public static int BINS = 47;
    public static double surfStats[] = new double[BINS];

    public Point2D.Double myLocation;     
    public Point2D.Double enemyLocation;

    public ArrayList<EnemyWave> enemyWaves = new ArrayList<EnemyWave>();
    public ArrayList<Integer> surfDirections = new ArrayList<Integer>();
    public ArrayList<Double> surfAbsBearings = new ArrayList<Double>();

    public static double oppEnergy = 100.0;

    public void onScannedRobot(ScannedRobotEvent e) {
        myLocation = alphabet.myLocation;

        double lateralVelocity = alphabet.getVelocity()*Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        surfDirections.add(0, (int) ((lateralVelocity >= 0) ? 1 : -1));
        surfAbsBearings.add(0, (double) (absBearing + Math.PI));


        //Energy drop means the robot has fired (most likely)
        double bulletPower = oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09
            && surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime         = alphabet.getTime() - 1;
            ew.bulletVelocity   = MathUtils.bulletVelocity(bulletPower);
            ew.distanceTraveled = MathUtils.bulletVelocity(bulletPower);
            ew.direction        = surfDirections.get(2);
            ew.directAngle      = surfAbsBearings.get(2);
            ew.fireLocation     = (Point2D.Double) enemyLocation.clone(); // last tick

            enemyWaves.add(ew);
        }

        oppEnergy = e.getEnergy();

        // update after EnemyWave detection, because that needs the previous
        // enemy location as the source of the wave
        enemyLocation = MathUtils.project(myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing(e);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (!enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            for (int x = 0; x < enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave) enemyWaves.get(x);

                if (Math.abs(ew.distanceTraveled -
                    myLocation.distance(ew.fireLocation)) < 50
                    && Math.abs(MathUtils.bulletVelocity(e.getBullet().getPower()) 
                        - ew.bulletVelocity) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);

                // We can remove this wave now, of course since it's "passed" us.
                enemyWaves.remove(enemyWaves.lastIndexOf(hitWave));
            }
        }
    }
    public void onBulletHit(BulletHitEvent e) {}
    public void onBulletHitBullet(BulletHitBulletEvent e) {}

    //Helpers 'n stuff
    public double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave, predictPosition(surfWave, direction));
        return surfStats[index];
    }

    public void doSurfing(ScannedRobotEvent e) {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) {return;}

        double dangerLeft  = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = MathUtils.absoluteBearing(surfWave.fireLocation, myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = MathUtils.wallSmoothing(myLocation, goAngle - (Math.PI/2), -1);
        } else {
            goAngle = MathUtils.wallSmoothing(myLocation, goAngle + (Math.PI/2), 1);
        }

        setBackAsFront(alphabet, goAngle);
    }

    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double)myLocation.clone();
        double predictedVelocity = alphabet.getVelocity();
        double predictedHeading = alphabet.getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        while(!intercepted && counter < 500) {
            moveAngle =
                MathUtils.wallSmoothing(predictedPosition, MathUtils.absoluteBearing(surfWave.fireLocation,
                predictedPosition) + (direction * (Math.PI/2)), direction)
                - predictedHeading;
            moveDir = 1;

            //Max turning
            if(Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this, you can't turn more then this in one tick
            maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading + MathUtils.limit(-maxTurning, moveAngle, maxTurning));

            predictedVelocity += (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
            predictedVelocity = MathUtils.limit(-8, predictedVelocity, 8);

            // calculate the new predicted position
            predictedPosition = MathUtils.project(predictedPosition, predictedHeading, predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.fireLocation) < surfWave.distanceTraveled + (counter * surfWave.bulletVelocity) + surfWave.bulletVelocity) {
                intercepted = true;
            }
        }

        return predictedPosition;
    }

    public void updateWaves() {
        for (int x = 0; x < enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)enemyWaves.get(x);

            ew.distanceTraveled = (alphabet.getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled >
                myLocation.distance(ew.fireLocation) + 50) {
                enemyWaves.remove(x);
                x--;
            }
        }
    }

    public EnemyWave getClosestSurfableWave() {
        double closestDistance = 50000;
        EnemyWave surfWave = null;

        for (int x = 0; x < enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)enemyWaves.get(x);
            double distance = myLocation.distance(ew.fireLocation)
                - ew.distanceTraveled;

            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        //Angle used to fire at us
        double offsetAngle = (MathUtils.absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
        
        double factor = Utils.normalRelativeAngle(offsetAngle) / MathUtils.maxEscapeAngle(ew.bulletVelocity) * ew.direction;

        return (int) MathUtils.limit(0, (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2), BINS - 1);
    }

    //update our stat array to reflect the danger in that area.
    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
        //Invert movement
        double angle =
            Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
        if (Math.abs(angle) > (Math.PI/2)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }
            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1*angle);
           } else {
                robot.setTurnRightRadians(angle);
           }
            robot.setAhead(100);
        }
    }

    //Classes 'n stuff
    class EnemyWave {
        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity, directAngle, distanceTraveled;
        int direction;

        public EnemyWave() { }
    }
}