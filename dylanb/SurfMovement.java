package dylanb;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.util.LinkedList;

public class SurfMovement {
    //Components stuff
    Alphabet alphabet; //Parent robot
    AlphabetLogger logger = new AlphabetLogger("SurfMovement");

    public void init(Alphabet robot){
        logger.log("SurfMovment initialized");
        alphabet = robot;

    }

    public void execute(){}

    public void onScannedRobot(ScannedRobotEvent e) {}

    public void onHitByBullet(HitByBulletEvent e) {}
    public void onBulletHit(BulletHitEvent e) {}
    public void onBulletHitBullet(BulletHitBulletEvent e) {}

    //Helpers 'n stuff
    private double checkDanger(int direction) {
        Point2D.Double predictedPosition = alphabet.myLocation;
        double predictedHeading = alphabet.getHeadingRadians();
        double predictedVelocity = alphabet.getVelocity();
        double maxTurning, moveAngle, moveDir, lastPredictedDistance;

        int counter = 3;

        do {
            moveDir = 1;

            //Don't hit walls
            if (Math.cos(moveAngle = wallSmoothing(predictedPosition, surfWave.absBearing(
                                                   predictedPosition) + (direction * MathUtils.A_LITTLE_LESS_THAN_HALF_PI), direction)
                                                                         - predictedHeading) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            predictedPosition = project(predictedPosition, 
                predictedHeading = Utils.normalRelativeAngle(predictedHeading +
                    MathUtils.limit(-(maxTurning = Rules.getTurnRateRadians(Math.abs(predictedVelocity))),
                        Utils.normalRelativeAngle(moveAngle), maxTurning)),
                    (predictedVelocity = MathUtils.limit(-8,
                    predictedVelocity + (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir),
                    8)));
            //Basically we find where we think bullets will be and if they are close to us we will want to move away


        } while (
                (lastPredictedDistance = surfWave.distanceToPoint(predictedPosition)) >=
                surfWave.distance + ((++counter) * surfWave.bulletSpeed));
                //loop until we are close enough to the next wave

        int index;

        //return the danger of the current predicted position, based on multiple factors
        double waveFactor = surfWave.waveGuessFactors[index = getFactorIndex(surfWave, predictedPosition)] + .01 / (Math.abs(index - GF_ZERO) + 1);
        // Calculate the distanceFactor.
        double distanceFactor = Math.pow(lastPredictedDistance, 4);
        // Return the combined factor.
        return waveFactor / distanceFactor;
    }

    public void logAndRemoveWave(Point2D.Double hitLocation) {
        Wave w = surfWave;
        int x = 0;
        System.out.println("Hit at " + hitLocation);
        do {
            try {
                if (Math.abs(w.distanceToPoint(hitLocation) - w.distance) < 100) {
                    logHit(w, hitLocation, 0.85);
                    enemyWaves.remove(w);
                    alphabet.removeCustomEvent(w);
                    return;
                }
                w = (Wave)enemyWaves.get(x++);
            } catch (Exception ex) { 
                logger.error(ex.getMessage());
            }
        } while (x <= enemyWaves.size());
    }

    public static void logHit(Wave w, Point2D.Double targetLocation, double rollingDepth) {
        for (int x = GF_ONE; x >= 0; x--) {
            w.waveGuessFactors[x] = ((w.waveGuessFactors[x] * rollingDepth)
                + ((1 + w.weight) / (Math.pow(x - getFactorIndex(w, targetLocation), 2) + 1)))
                / (rollingDepth + 1 + w.weight);
        }
    }

    private static int getFactorIndex(Wave w, Point2D.Double botLocation) {
        //Returns the index of the guess factor that would have been most likely to hit the target
        // limit the factor to between 0 and 1
        return (int)MathUtils.limit(0,
            // compute the angle factor
            ((((
            Utils.normalRelativeAngle(
            w.absBearing(botLocation) - w.directAngle)//Get the direct angle to the target
            * w.orientation) // get the orientation
            // divide the angle by the maximum angle that can be covered
            // in one turn.
            / Math.asin(8.0/w.bulletSpeed))
            // multiply by the maximum factor
            * (GF_ZERO)) + (GF_ZERO)),
            // limit to the maximum factor
            GF_ONE);
    }

    private static double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        //Used to avoid walls without actually havign to reverse direction
        while (!MathUtils.fieldBox.contains(project(botLocation, angle, WALL_STICK))) {
            angle += orientation*0.05;
        }
        return angle;
    }

    private static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
            sourceLocation.y + Math.cos(angle) * length);
    }

    //Classes 'n stuff
    static class Wave extends Condition {
        Point2D.Double sourceLocation;
        double[] waveGuessFactors;
        double bulletSpeed, directAngle, distance;
        int orientation, weight;
        Alphabet alphabet;

        public double absBearing(Point2D.Double target) {return Math.atan2(target.x - sourceLocation.x, target.y - sourceLocation.y);}
        public double distanceToPoint(Point2D.Double p) {return sourceLocation.distance(p);}

        public boolean test() {
            if (distanceToPoint(enemyWaves.contains(this) ? this.alphabet.myLocation : this.alphabet.radar.target.location) <= (distance+=bulletSpeed) + (2 * bulletSpeed)) {
                if (!enemyWaves.remove(this)) {
                    logHit(this, this.alphabet.radar.target.location, 600);
                }
                return true;
            }
            return false;
        }
    }
}