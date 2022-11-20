package dylanb;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;

public class Alphabet extends AdvancedRobot
{
	SurfMovement surferMove       = new SurfMovement();
	MeleeMovement meleeMove       = new MeleeMovement();
	GuessFactorGun guessFactorGun = new GuessFactorGun();
	Radar radar                   = new Radar();
	AlphabetLogger logger         = new AlphabetLogger("Main");

	public static final int MOVEMENT_SURFING = 0;
	public static final int MOVEMENT_MELEE   = 1;
	int movementMode = 1;

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		//Setup components
		//surferMove.init(this);
		guessFactorGun.init(this);
		radar.init(this);
		meleeMove.init(this);

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setColors(Color.black, Color.blue, Color.red);

		//Main
		while (true){
			myLocation = new Point2D.Double(getX(), getY());

			if (getOthers() > 1) {movementMode = MOVEMENT_MELEE;} 
			else {movementMode = MOVEMENT_SURFING;}

			//if (movementMode == MOVEMENT_SURFING) surferMove.execute();
			if (movementMode == MOVEMENT_MELEE) meleeMove.execute();
			
			guessFactorGun.execute();//Doesn't do anything at the moment
			radar.execute();
			execute();
		}
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {
		if (movementMode == MOVEMENT_MELEE) meleeMove.onScannedRobot(e);
		radar.onScannedRobot(e);
		//if (movementMode == MOVEMENT_SURFING) surferMove.onScannedRobot(e);
		guessFactorGun.onScannedRobot(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		//if (movementMode == MOVEMENT_SURFING) surferMove.onHitByBullet(e);
	}

	public void onBulletHit(BulletHitEvent e) {
		//if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHit(e);
		guessFactorGun.onBulletHit(e);
	}

	public void onBulletMissed(BulletMissedEvent e) {
		guessFactorGun.onBulletMissed(e);
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		//if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHitBullet(e);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		radar.onRobotDeath(e);
		if (movementMode == MOVEMENT_MELEE) meleeMove.onRobotDeath(e);
	}

	public void onCustomEvent(CustomEvent e) {
		//if (movementMode == MOVEMENT_SURFING) surferMove.onCustomEvent(e);
	}
}
