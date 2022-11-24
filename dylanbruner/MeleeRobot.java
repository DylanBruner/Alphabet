package dylanbruner;

import robocode.*;
import robocode.util.Utils;
import java.util.Hashtable;
import java.awt.geom.Point2D;

/*
 * This is basically a whole new robot that is automatically controlled by the main robot 
 * it's only purpose is for melee combat and is disabled when there is only one enemy in
 * favor of wave surfing, a virtual gun system and other stuff
 * 
 * Only reason i did this was because I found my bot that worked GREAT in 1v1 was terrible in melee
 * And I didn't want to figure out how to do multi-bot wave surfing so I just made a whole new bot
 * 
 * 
 * It uses a minimum risk movement system and just head-on targeting because i haven't switched it
 * over to the virtual gun system yet (this will probably be done before the competition) [Did it!]
 * It's getting switched again! to a shadow gun
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
        
		//If the target is not alive, wait for the next scan
		//If the time is less than 9, wait so all the enemies can be scanned before we start moving
        if(target.live && alphabet.getTime() > 9) {
            moveAndShoot();
        }
    }
	
	public void moveAndShoot() {
		//The target is just used to keep distance
		double distanceToTarget = myPos.distance(target.pos);
		double distanceToNextDestination = myPos.distance(nextDestination);
		
		if(distanceToNextDestination < 15) {
			double addLast = 1 - Math.rint(Math.pow(Math.random(), alphabet.getOthers()));			
			Point2D.Double testPoint;
			
			//Wall smoothing
            for (int i=0; i < 200; i++){
                testPoint = MathUtils.calcPoint(myPos, Math.min(distanceToTarget*0.8, 100 + 200*Math.random()), 2*Math.PI*Math.random());
                if(MathUtils.fieldBox.contains(testPoint) && getDangerAt(testPoint, addLast) < getDangerAt(nextDestination, addLast)) {
                    nextDestination = testPoint;
                }
            }

			lastPosition = myPos;			
		} else {			
			double angle = MathUtils.calcAngle(nextDestination, myPos) - alphabet.getHeadingRadians();
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
	
	public static double getDangerAt(Point2D.Double p, double addLast) {
		double danger = addLast*0.08/p.distanceSq(lastPosition);
		
		//Loop through the enemies and add their danger to the danger variable
        for (internEnemy enemy : enemies.values()) {
            if(!enemy.live) {continue;}
			danger += Math.min(enemy.energy/myEnergy,2) * 
					(1 + Math.abs(Math.cos(MathUtils.calcAngle(myPos, p) - MathUtils.calcAngle(enemy.pos, p)))) / p.distanceSq(enemy.pos);
			//Basically we add danger based on the following
			//1. The closer the enemy is
			//2. The more energy the enemy
			//3. The more the enemy is facing us
        }
		return danger;
	}
	
    //RoboCode events ==========================================================
	public void onScannedRobot(ScannedRobotEvent e)
	{
		internEnemy enemy = (internEnemy)enemies.get(e.getName());
		
		if(enemy == null){
			enemy = new internEnemy();
			enemies.put(e.getName(), enemy);
		}
		
		enemy.energy = e.getEnergy();
		enemy.live = true;
		enemy.name = e.getName();
		enemy.pos = MathUtils.calcPoint(myPos, e.getDistance(), alphabet.getHeadingRadians() + e.getBearingRadians());
		
		// will be replaced soon, just kidding movement relies on it but when ever i feel like it i wanna
		// replace it with a better target selection system that uses the virtual leaderboard
		if(!target.live || e.getDistance() < myPos.distance(target.pos)) {
			target = enemy;
		}		
	}
	
	public void onRobotDeath(RobotDeathEvent e) {
		enemies.get(e.getName()).live = false;
	}
	
    // classes =================================================================

	//Used for tracking enemies because all radar control is handed off to this "robot"
	//When being used for melee combat
	public class internEnemy {
		public double energy;
		public String name;
		public Point2D.Double pos;
		public boolean live;
	}
}