package dylanb;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;

import java.awt.event.MouseEvent;

//We could store data about the enemy on the disk to preserve it and maybe already having targeting data for like melee would be good

public class Alphabet extends AdvancedRobot
{
	VirtualGunManager vGunManager  = new VirtualGunManager();
	GuessFactorGun guessFactorGun  = new GuessFactorGun();
	LinearGun linearGun            = new LinearGun();
	SurfMovement surferMove        = new SurfMovement();
	MeleeRobot meleeMove           = new MeleeRobot(); //Melee movement basically takes over the whole bot until it's done
	Radar radar                    = new Radar();
	Painting debugOverlay          = new Painting();
	AlphabetLogger logger          = new AlphabetLogger("Main");

	//Auto movement mode
	public final int MOVEMENT_SURFING = 0;
	public final int MOVEMENT_MELEE   = 1;
	public int movementMode = -1;

	//Auto gun
	public final int GUN_GUESS_FACTOR = 0;
	public final int GUN_LINEAR       = 1;
	public int selectedGun = GUN_LINEAR;

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		//Initlize the components
		vGunManager.init(this);
		surferMove.init(this);
		guessFactorGun.init(this);
		linearGun.init(this);
		radar.init(this);
		meleeMove.init(this);
		debugOverlay.init(this);

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setColors(Color.black, Color.blue, Color.red);

		//Main
		while (true){
			radar.execute();

			myLocation = new Point2D.Double(getX(), getY());
			if (getOthers() > 1 && movementMode != MOVEMENT_MELEE) {
				logger.log("Switching to melee movement");
				movementMode = MOVEMENT_MELEE;
				radar.disableRadarManagement();// Disable radar management it's done ein MeleeRobot.java
			} else if (getOthers() <= 1 && movementMode != MOVEMENT_SURFING) {
				logger.log("Switching to surfing");
				movementMode = MOVEMENT_SURFING;
				radar.enableRadarManagement(); //Switch back on the radar
			}

			//Auto movement
			if (movementMode == MOVEMENT_MELEE) meleeMove.execute();
			
			//Auto gun
			if (movementMode == MOVEMENT_MELEE){}//Guns are handled in MeleeRobot.java during Melee
			else if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.execute();
			else if (selectedGun == GUN_LINEAR) linearGun.execute();

			vGunManager.execute();
			execute();
		}
	}

	//Few helpers i need
	public double getFirePower(){
		return Math.min(400 / myLocation.distance(radar.target.location), 3);
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {
		radar.onScannedRobot(e);
		//update target location
		//radar.target.location = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading() + e.getBearing())) * e.getDistance(), 
		//										   getY() + Math.cos(Math.toRadians(getHeading() + e.getBearing())) * e.getDistance());
		
		if (movementMode == MOVEMENT_SURFING) surferMove.onScannedRobot(e);
		else if (movementMode == MOVEMENT_MELEE) meleeMove.onScannedRobot(e);
		
		//Multi-gun
		if (movementMode == MOVEMENT_MELEE) {}//Guns are handled in MeleeRobot.java during Melee
		else if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onScannedRobot(e);
		else if (selectedGun == GUN_LINEAR) linearGun.onScannedRobot(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		if (movementMode == MOVEMENT_SURFING) surferMove.onHitByBullet(e);
	}

	public void onBulletHit(BulletHitEvent e) {
		if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHit(e);

		//Multi-gun
		if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onBulletHit(e);
		//else if (gunMode == GUN_LINEAR) linearGun.onBulletHit(e);
	}

	public void onBulletMissed(BulletMissedEvent e) {
		//Multi-gun
		if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onBulletMissed(e);
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHitBullet(e);
	}

	public void onHitRobot(HitRobotEvent e) {
	}

	public void onRobotDeath(RobotDeathEvent e) {
		radar.onRobotDeath(e);
	}

	@Override
	public void onPaint(java.awt.Graphics2D g) {
		debugOverlay.onPaint(g);
	}

	public void onMouseMoved(MouseEvent e) {
		debugOverlay.onMouseMoved(e);
	}
}