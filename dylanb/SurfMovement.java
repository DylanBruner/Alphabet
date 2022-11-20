package dylanb;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.util.ArrayList;

public class SurfMovement {
    //Components stuff
    Alphabet alphabet; //Parent robot
    AlphabetLogger logger = new AlphabetLogger("SurfMovement");

    //Surfing stuff
    public static int BINS = 47;
    public static double _surfStats[] = new double[BINS];
    public Point2D.Double _myLocation;     // our bot's location
    public Point2D.Double _enemyLocation;  // enemy bot's location

    public ArrayList<EnemyWave> _enemyWaves;
    public ArrayList<Integer> _surfDirections;
    public ArrayList<Double> _surfAbsBearings;

    public static double _oppEnergy = 100.0;

    public void init(Alphabet robot){
        logger.log("SurfMovment initialized");
        alphabet = robot;

        _enemyWaves      = new ArrayList<EnemyWave>();
        _surfDirections  = new ArrayList<Integer>();
        _surfAbsBearings = new ArrayList<Double>();
    }

    public void execute(){}

    public void onScannedRobot(ScannedRobotEvent e) {
        _myLocation = alphabet.myLocation;

        double lateralVelocity = alphabet.getVelocity()*Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        //Radar is managed by radar class
        //alphabet.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
        //    - alphabet.getRadarHeadingRadians()) * 2);

        _surfDirections.add(0, (int) ((lateralVelocity >= 0) ? 1 : -1));
        _surfAbsBearings.add(0, (double) (absBearing + Math.PI));


        double bulletPower = _oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09
            && _surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime = alphabet.getTime() - 1;
            ew.bulletVelocity = MathUtils.bulletVelocity(bulletPower);
            ew.distanceTraveled = MathUtils.bulletVelocity(bulletPower);
            ew.direction = ((Integer)_surfDirections.get(2)).intValue();
            ew.directAngle = ((Double)_surfAbsBearings.get(2)).doubleValue();
            ew.fireLocation = (Point2D.Double)_enemyLocation.clone(); // last tick

            _enemyWaves.add(ew);
        }

        _oppEnergy = e.getEnergy();

        // update after EnemyWave detection, because that needs the previous
        // enemy location as the source of the wave
        _enemyLocation = MathUtils.project(_myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();
    }

    public void onHitByBullet(HitByBulletEvent e) {}
    public void onBulletHit(BulletHitEvent e) {}
    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave)_enemyWaves.get(x);

                if (Math.abs(ew.distanceTraveled -
                    _myLocation.distance(ew.fireLocation)) < 50
                    && Math.abs(MathUtils.bulletVelocity(e.getBullet().getPower()) 
                        - ew.bulletVelocity) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);

                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }
    }

    //Helpers 'n stuff
    public double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave,
            predictPosition(surfWave, direction));

        return _surfStats[index];
    }

    public void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) { return; }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = MathUtils.absoluteBearing(surfWave.fireLocation, _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = MathUtils.wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1);
        } else {
            goAngle = MathUtils.wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1);
        }

        setBackAsFront(alphabet, goAngle);
    }

    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double)_myLocation.clone();
        double predictedVelocity = alphabet.getVelocity();
        double predictedHeading = alphabet.getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        do {
            moveAngle =
                MathUtils.wallSmoothing(predictedPosition, MathUtils.absoluteBearing(surfWave.fireLocation,
                predictedPosition) + (direction * (Math.PI/2)), direction)
                - predictedHeading;
            moveDir = 1;

            if(Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this, you can't turn more then this in one tick
            maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading
                + MathUtils.limit(-maxTurning, moveAngle, maxTurning));

            predictedVelocity +=
                (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
            predictedVelocity = MathUtils.limit(-8, predictedVelocity, 8);

            // calculate the new predicted position
            predictedPosition = MathUtils.project(predictedPosition, predictedHeading,
                predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.fireLocation) <
                surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)
                + surfWave.bulletVelocity) {
                intercepted = true;
            }
        } while(!intercepted && counter < 500);

        return predictedPosition;
    }

    public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);

            ew.distanceTraveled = (alphabet.getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled >
                _myLocation.distance(ew.fireLocation) + 50) {
                _enemyWaves.remove(x);
                x--;
            }
        }
    }

    public EnemyWave getClosestSurfableWave() {
        double closestDistance = 50000;
        EnemyWave surfWave = null;

        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
            double distance = _myLocation.distance(ew.fireLocation)
                - ew.distanceTraveled;

            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        double offsetAngle = (MathUtils.absoluteBearing(ew.fireLocation, targetLocation)
            - ew.directAngle);
        double factor = Utils.normalRelativeAngle(offsetAngle)
            / MathUtils.maxEscapeAngle(ew.bulletVelocity) * ew.direction;

        return (int)MathUtils.limit(0,
            (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
            BINS - 1);
    }

    //update our stat array to reflect the danger in that area.
    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
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