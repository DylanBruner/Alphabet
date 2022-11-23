package dylanbruner;

import robocode.*;
import robocode.util.Utils;
import java.util.Hashtable;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

/*
 * This is basically a whole new robot that is automatically controlled by the main robot 
 * it's only purpose is for melee combat and is disabled when there is only one enemy in
 * favor of wave surfing, a virtual gun system and other stuff
 * 
 * This movement is HEAVILY inspired by HawkOnFire https://robowiki.net/wiki/HawkOnFire/Code
*/

public class MeleeRobot {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("MeleeRobot");

	static Hashtable<String, internEnemy> enemies = new Hashtable<String, internEnemy>();
	static internEnemy target;
	static Point2D.Double nextDestination;
	static Point2D.Double lastPosition;
	static Point2D.Double myPos;
	static double myEnergy;
	
	public void init(Alphabet alphabet) {
        this.alphabet = alphabet;		
		nextDestination = lastPosition = myPos = alphabet.myLocation;
		target = new internEnemy();
	}
    
	//Called every tick by the main robot
    public void execute() {
        myPos = alphabet.myLocation;
        myEnergy = alphabet.getEnergy();
        
        if(target.live && alphabet.getTime() > 9) {
            doMovementAndGun();
        }
    }
	
	public void doMovementAndGun() {
		double distanceToTarget = myPos.distance(target.pos);
		
		if(alphabet.getGunTurnRemaining() == 0 && myEnergy > 1) {
			alphabet.setFire( Math.min(Math.min(myEnergy/6d, 1300d/distanceToTarget), target.energy/3d) );
		}
		
		alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(calcAngle(target.pos, myPos) - alphabet.getGunHeadingRadians()));
		
		double distanceToNextDestination = myPos.distance(nextDestination);
		
		if(distanceToNextDestination < 15) {
			double addLast = 1 - Math.rint(Math.pow(Math.random(), alphabet.getOthers()));
			
			Rectangle2D.Double battleField = new Rectangle2D.Double(30, 30, alphabet.getBattleFieldWidth() - 60, alphabet.getBattleFieldHeight() - 60);
			Point2D.Double testPoint;
			
            for (int i=0; i < 200; i++){
                testPoint = calcPoint(myPos, Math.min(distanceToTarget*0.8, 100 + 200*Math.random()), 2*Math.PI*Math.random());
                if(battleField.contains(testPoint) && evaluate(testPoint, addLast) < evaluate(nextDestination, addLast)) {
                    nextDestination = testPoint;
                }
            }

			lastPosition = myPos;			
		} else {			
			double angle = calcAngle(nextDestination, myPos) - alphabet.getHeadingRadians();
			double direction = 1;
			
			if(Math.cos(angle) < 0) {
				angle += Math.PI;
				direction = -1;
			}
			
			alphabet.setAhead(distanceToNextDestination * direction);
			alphabet.setTurnRightRadians(angle = Utils.normalRelativeAngle(angle));
			alphabet.setMaxVelocity(Math.abs(angle) > 1 ? 0 : 8d);
			
		}
	}
	
	public static double evaluate(Point2D.Double p, double addLast) {
		double eval = addLast*0.08/p.distanceSq(lastPosition);
		
        for (internEnemy en : enemies.values()) {
            if(en.live) {
                eval += Math.min(en.energy/myEnergy,2) * 
                        (1 + Math.abs(Math.cos(calcAngle(myPos, p) - calcAngle(en.pos, p)))) / p.distanceSq(en.pos);
            }
        }
		return eval;
	}
	
    //RoboCode events ==========================================================
	public void onScannedRobot(ScannedRobotEvent e)
	{
		internEnemy en = (internEnemy)enemies.get(e.getName());
		
		if(en == null){
			en = new internEnemy();
			enemies.put(e.getName(), en);
		}
		
		en.energy = e.getEnergy();
		en.live = true;
		en.pos = calcPoint(myPos, e.getDistance(), alphabet.getHeadingRadians() + e.getBearingRadians());
		
		// will be replaced soon, just kidding movement relies on it
		if(!target.live || e.getDistance() < myPos.distance(target.pos)) {
			target = en;
		}
		
        //This doesn't really matter cause we switch to wave surfing
		//if(alphabet.getOthers()==1)	alphabet.setTurnRadarLeftRadians(alphabet.getRadarTurnRemainingRadians());
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		((internEnemy)enemies.get(e.getName())).live = false;
	}
	
    // math ====================================================================
	private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
		return new Point2D.Double(p.x + dist*Math.sin(ang), p.y + dist*Math.cos(ang));
	}
	
	private static double calcAngle(Point2D.Double p2,Point2D.Double p1){
		return Math.atan2(p2.x - p1.x, p2.y - p1.y);
	}
	
    // classes =================================================================
	public class internEnemy {
		public Point2D.Double pos;
		public double energy;
		public boolean live;
	}
}