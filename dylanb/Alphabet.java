package dylanb;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
//We could store data about the enemy on the disk to preserve it and maybe already having targeting data for like melee would be good

public class Alphabet extends AdvancedRobot
{
	GuessFactorGun guessFactorGun = new GuessFactorGun();
	LinearGun linearGun           = new LinearGun();
	SurfMovement surferMove       = new SurfMovement();
	MeleeMovement meleeMove       = new MeleeMovement();
	Radar radar                   = new Radar();
	AlphabetLogger logger         = new AlphabetLogger("Main");

	//Auto movement mode
	public static final int MOVEMENT_SURFING = 0;
	public static final int MOVEMENT_MELEE   = 1;
	public static int movementMode = -1;

	//Auto gun
	public static final int GUN_GUESS_FACTOR = 0;
	public static final int GUN_LINEAR       = 1;
	public static int gunMode = GUN_LINEAR;

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		//Setup components
		surferMove.init(this);
		//guessFactorGun.init(this);
		linearGun.init(this);
		radar.init(this);
		meleeMove.init(this);

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setColors(Color.black, Color.blue, Color.red);

		//Main
		while (true){
			myLocation = new Point2D.Double(getX(), getY());
			if (getOthers() > 1 && movementMode != MOVEMENT_MELEE) {
				logger.log("Switching to melee movement");
				movementMode = MOVEMENT_MELEE;
			} else if (getOthers() <= 1 && movementMode != MOVEMENT_SURFING) {
				logger.log("Switching to surfing");
				movementMode = MOVEMENT_SURFING;
			}

			//Auto movement
			if (movementMode == MOVEMENT_MELEE) meleeMove.execute();
			
			//Auto gun
			if (gunMode == GUN_GUESS_FACTOR) guessFactorGun.execute();
			else if (gunMode == GUN_LINEAR) linearGun.execute();

			radar.execute();
			execute();
		}
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {
		radar.onScannedRobot(e);
		if (movementMode == MOVEMENT_SURFING) surferMove.onScannedRobot(e);
		
		//Multi-gun
		if (gunMode == GUN_GUESS_FACTOR) guessFactorGun.onScannedRobot(e);
		else if (gunMode == GUN_LINEAR) linearGun.onScannedRobot(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (movementMode == MOVEMENT_SURFING) surferMove.onHitByBullet(e);
	}

	public void onBulletHit(BulletHitEvent e) {
		if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHit(e);

		//Multi-gun
		if (gunMode == GUN_GUESS_FACTOR) guessFactorGun.onBulletHit(e);
		//else if (gunMode == GUN_LINEAR) linearGun.onBulletHit(e);
	}

	public void onBulletMissed(BulletMissedEvent e) {
		//Multi-gun
		if (gunMode == GUN_GUESS_FACTOR) guessFactorGun.onBulletMissed(e);
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHitBullet(e);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		radar.onRobotDeath(e);
		if (movementMode == MOVEMENT_MELEE) meleeMove.onRobotDeath(e);
	}
}
