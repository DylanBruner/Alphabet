package dylanbruner.gun;

import java.util.ArrayDeque;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.MathUtils;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class BulletShieldingGun extends Component {
    AlphabetLogger logger = new AlphabetLogger("BulletShieldingGun");

    ArrayDeque<Double> pastAngles = new ArrayDeque<Double>();

    static final double BULLET_START_POWER = 0.15;

    Point2D.Double lastPosition = null;
    double lastEnemyEnergy  = 100;
    double lastMoveDistance = 0;
    double aimAngle = 0;
    double firePower = BULLET_START_POWER;
    long fireOnTick = 0;

    static final int[] TIME_TO_MOVE  = new int[] {1,1,2,2,3,3,4,4,4};
	static final int[] MAX_MOVE_DIST = new int[] {1,1,3,5,8};

    public void onScannedRobot(ScannedRobotEvent e) {
        double angleToEnemy = alphabet.getHeadingRadians() + e.getBearingRadians();
		
		Point2D.Double enemyPosition = MathUtils.project(alphabet.myLocation,angleToEnemy,e.getDistance());
		
		pastAngles.addFirst(angleToEnemy);
		if(pastAngles.size() > 3)
			pastAngles.removeLast();
	
		long time = alphabet.getTime();
		
		/* Radar */
		alphabet.setTurnRadarRightRadians(1.9*Utils.normalRelativeAngle(angleToEnemy - alphabet.getRadarHeadingRadians()));
		
		double enemyEnergy = e.getEnergy();
		double deltaEnergy = lastEnemyEnergy - enemyEnergy;
		lastEnemyEnergy = enemyEnergy;
		
		if(deltaEnergy > 0 && deltaEnergy <= 3) {
			double bulletAngle = pastAngles.getLast() + Math.PI;
			double bulletSpeed = Rules.getBulletSpeed(deltaEnergy);
			
			/* how much time till the next time they can fire? */
			int minTimeTillNextFire = (int)(Rules.getGunHeat(deltaEnergy)/alphabet.getGunCoolingRate());
			
			/* we have to move there and back, so divide the time we have in half */
			int maxTestDistance = getMaxMoveDistanceForTime(minTimeTillNextFire/2);
			
			double bestDeviation = Double.POSITIVE_INFINITY;
			
			double myBulletSpeed = Rules.getBulletSpeed(BULLET_START_POWER);
			
			for(int move = -maxTestDistance; move <= maxTestDistance; ++move) {
				/* shielding with a move of zero is dangerous and we shouldn't attempt it */
				if(move == 0)
					continue;
				
				/* calculate where we will be when we finish moving */
				Point2D.Double myFirePosition = getMoveEnd(move);
				
				/* determine how long it will take to get there */
				long timeToMove = TIME_TO_MOVE[Math.abs(move)];
				
				/* the bullet position at the time time we start */
				Point2D.Double bulletStart = MathUtils.project(lastPosition,bulletAngle,bulletSpeed*(timeToMove+1));
				
				/* calculate our target position */
				Point2D.Double target = intercept(myFirePosition,myBulletSpeed,bulletStart,bulletAngle,bulletSpeed);
				
				double eGoalTime = Math.ceil(bulletStart.distance(target) / bulletSpeed);
				
				/* Update our target */
				target = MathUtils.project(bulletStart, bulletAngle, (eGoalTime-0.5)*bulletSpeed);
				double myGunAim = angleFromTo(myFirePosition,target);
				
				/* determine if they intersect */
				Line2D eLine = new Line2D.Double(
						MathUtils.project(bulletStart,bulletAngle,bulletSpeed*(eGoalTime-1)),
						MathUtils.project(bulletStart,bulletAngle,bulletSpeed*eGoalTime)
						);
				
				double myTargetTime = myFirePosition.distance(target) / myBulletSpeed;
				double myGoalTime = Math.ceil(myTargetTime);
				
				Line2D myLine = new Line2D.Double(
						MathUtils.project(myFirePosition,myGunAim,myBulletSpeed*(myGoalTime-1)),
						MathUtils.project(myFirePosition,myGunAim,myBulletSpeed*myGoalTime)
						);
				
				if(myLine.intersectsLine(eLine)) {
					double dB = myTargetTime - (myGoalTime - 0.5);
					double deviation = dB*dB;
					
					if(deviation < bestDeviation) {
						bestDeviation = deviation;
						lastMoveDistance = move;
						
						fireOnTick = time + timeToMove;
						
						/* tweak our power so that ours is more centered as well */
						double goalSpeed = myFirePosition.distance(target) / (myGoalTime-0.5);
						firePower = Math.max(0.1,Math.min((20-goalSpeed)/3,0.2));
						
						aimAngle = myGunAim;
						alphabet.setTurnRightRadians(0);
						alphabet.setAhead(lastMoveDistance);
					}
				}
			} /* for(int move ... */
		} /* if(deltaEnergy ... */
		
		if(enemyEnergy <= 0) {
			alphabet.setFire(0.1);
		}
		
		if(time == fireOnTick) {
			alphabet.setFire(firePower);
		}
		
		if(time == fireOnTick + 1) {
			alphabet.setAhead(-lastMoveDistance);
		}
		
		if(time > fireOnTick && alphabet.getDistanceRemaining() == 0) {
			aimAngle = angleToEnemy;
			alphabet.setTurnRightRadians(Utils.normalRelativeAngle(angleToEnemy + Math.PI/2.0 - Math.PI/4.0 - alphabet.getHeadingRadians()));
		}
		
		alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(aimAngle - alphabet.getGunHeadingRadians()));
		
		lastPosition = enemyPosition;
    }

    public void onBulletHit(BulletHitEvent e) {
        lastEnemyEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
    }

    public void onHitByBullet(HitByBulletEvent e) {
        lastEnemyEnergy += Rules.getBulletHitBonus(e.getPower());
    }

	public int getMaxMoveDistanceForTime(int time) {
		if(time >= MAX_MOVE_DIST.length)
			return MAX_MOVE_DIST[MAX_MOVE_DIST.length-1];
		return MAX_MOVE_DIST[time];
	}
	
	public Point2D.Double getMoveEnd(double distance) {
		return MathUtils.project(alphabet.myLocation,alphabet.getHeadingRadians(),distance);
	}
	
	public Point2D.Double intercept(Point2D.Double pos, double vel, Point2D tPos, double tHeading, double tVel) {
		double tVelX = Math.sin(tHeading)*tVel;
		double tVelY = Math.cos(tHeading)*tVel;
		double relX = tPos.getX() - pos.getX();
		double relY = tPos.getY() - pos.getY();
		double b = relX * tVelX + relY * tVelY;
		double a = vel * vel - tVel * tVel;
		b = (b + Math.sqrt(b * b + a * (relX * relX + relY * relY))) / a;
		return new Point2D.Double(tVelX*b+tPos.getX(),tVelY*b+tPos.getY());
	}

	public double angleFromTo(Point2D a, Point2D b) {
		return Math.atan2(b.getX() - a.getX(), b.getY() - a.getY());
	}
}
