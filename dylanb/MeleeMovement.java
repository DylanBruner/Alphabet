package dylanb;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.*;
import java.util.Hashtable;

public class MeleeMovement {
    //Components stuff
    Alphabet alphabet; //Parent robot
    AlphabetLogger logger = new AlphabetLogger("MeleeMovement");

    //Movement variables
    static Hashtable<String, meleeInternalEnemy> enemies = new Hashtable<String, meleeInternalEnemy>();
	public meleeInternalEnemy meleeTarget;
	static Point2D.Double nextDestination;
	static Point2D.Double lastPosition;
	static Point2D.Double myPos;
	static double myEnergy;

    public void init(Alphabet robot){
        alphabet = robot;
        logger.log("MeleeMovement initialized");

        nextDestination = lastPosition = myPos = alphabet.myLocation;
		meleeTarget = new meleeInternalEnemy();
    }
    
    public void execute() {
        myPos    = alphabet.myLocation;
        myEnergy = alphabet.getEnergy();

        if (alphabet.radar.target != null){
            meleeTarget = new meleeInternalEnemy();
            meleeTarget.live   = true;
            meleeTarget.energy = alphabet.radar.target.energy;
            meleeTarget.pos    = alphabet.radar.target.location;
            if (meleeTarget.live && alphabet.getTime() > 9){
                doMovement();
            }
        } else {
            logger.warn("No target found"); return;
        }
    }


    public void doMovement(){
		double distanceToTarget = myPos.distance(meleeTarget.pos);
        double distanceToNextDestination = myPos.distance(nextDestination);//<=== null pointer exception here

        //Find next location to move to
        if(distanceToNextDestination < 15) {
            
            double addLast = 1 - Math.rint(Math.pow(Math.random(), alphabet.getOthers()));
            
            Point2D.Double testPoint;
            int i=0;
            
            do {
                testPoint = calcPoint(myPos, Math.min(distanceToTarget*0.8, 100 + 200*Math.random()), 2*Math.PI*Math.random());
                if(MathUtils.fieldBox.contains(testPoint) && evaluate(testPoint, addLast) < evaluate(nextDestination, addLast)) {
                    nextDestination = testPoint;
                }
            } while(i++ < 200);
            
            lastPosition = myPos;
        } 
        else {            
            double angle = calcAngle(nextDestination, myPos) - alphabet.getHeadingRadians();
            double direction = 1;
            if(Math.cos(angle) < 0) {
                angle += Math.PI;
                direction = -1;
            }
            
            alphabet.setAhead(distanceToNextDestination * direction);
            alphabet.setTurnRightRadians(angle = Utils.normalRelativeAngle(angle));
            //Add wall smoothing
            alphabet.setMaxVelocity(Math.abs(angle) > 1 ? 0 : 8d);
        }
    }

    //RoboCode events
    public void onScannedRobot(ScannedRobotEvent e){
        if (true){return;}
        //We don't actually want to manage targeting here we just convert
        //To the correct interal data structure
        meleeInternalEnemy en = new meleeInternalEnemy();

        if (enemies.contains(e.getName())){
            en = enemies.get(e.getName());
        } else {
            en = new meleeInternalEnemy();
            enemies.put(e.getName(), en);
        }
        
        
        en.energy = alphabet.radar.target.energy;
        en.pos    = alphabet.radar.target.location;
        en.live   = true;

        meleeTarget = en;

        if (meleeTarget == null){
            logger.warn("For some reason meleeTarget is null, FML");
            return;
        }
        
        if(!meleeTarget.live || e.getDistance() < myPos.distance(meleeTarget.pos)) {
            meleeTarget = en;
		}
        logger.log("Target: " + meleeTarget);
        //Radar lock, this is managed by the radar class for us
		//if(getOthers()==1)	setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
    }

    public void onRobotDeath(RobotDeathEvent e){
        meleeInternalEnemy en = enemies.get(e.getName());
        if (en != null) en.live = false;
    }

    //Main Movement Formula
    public static double evaluate(Point2D.Double p, double addLast) {
		double eval = addLast*0.08/p.distanceSq(lastPosition);
		
        for (meleeInternalEnemy en : enemies.values()){
            if(en.live) {
                eval += Math.min(en.energy/myEnergy,2) * 
                        (1 + Math.abs(Math.cos(calcAngle(myPos, p) - calcAngle(en.pos, p)))) / p.distanceSq(en.pos);
            }
        }

		return eval;
	}

    //Math
	private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
		return new Point2D.Double(p.x + dist*Math.sin(ang), p.y + dist*Math.cos(ang));
	}
	
	private static double calcAngle(Point2D.Double p2,Point2D.Double p1){
		return Math.atan2(p2.x - p1.x, p2.y - p1.y);
	}

    //Enemy data class
    public class meleeInternalEnemy {
		public Point2D.Double pos;
		public double energy;
		public boolean live;
	}
}