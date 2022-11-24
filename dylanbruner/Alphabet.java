package dylanbruner;

import robocode.*;
import java.awt.geom.*;

import java.awt.event.MouseEvent;

/*
 *==============================[OVERVIEW]==============================]
 * Shooting: (Auto switches)
 *   - Virtual Guns
 *   - GuessFactor Targeting, Linear Targeting and pattern matching
 * Movement: (Auto switches)
 *   - Melee: Minimum Risk Movement
 *   - 1v1: Wave Surfing
 * Targeting:
 *   - Virtual Leaderboard (already implemented) will be used to determine the best bot to target
 *   - In 1v1 we just target the enemy (duh)
 * Data Collecting:
 *   - Virtual Gun Data         (automatic gun switching)
 *   - Virtual leaderboard data (for melee targetting)
 *   - When enemies are scanned we store a 'snapshot' of their data
 *     this will be used for pattern match targeting and whatever 
 *     else could benefit from it. We could analyze the data and
 * 	   calculate it into the leaderboard data to make it more accurate. (Energy loss vs Gain or Loss vs time)
 * 
 * 
 * I wont be switching over to the virtual leaderboard just yet as doesn't play well with the current melee movement
 * Also implementing Keep Distance into the wave surfing would improve performance immensely
*/

//We could store data about the enemy on the disk to preserve it and maybe already having targeting data for like melee would be good

public class Alphabet extends AdvancedRobot
{
	//Attacking
	VirtualGunManager vGunManager   = new VirtualGunManager();
	GuessFactorGun guessFactorGun   = new GuessFactorGun();
	LinearGun linearGun             = new LinearGun();
	PatternMatchGun patternMatchGun = new PatternMatchGun(); //WIP
	//PatternMatchGun patternMatchGun = new PatternMatchGun();

	//Movement
	SurfMovement surferMove = new SurfMovement();
	MeleeRobot meleeMove    = new MeleeRobot(); //Melee movement basically takes over the whole bot until it's done
	//AntiGravity antiGravMov = new AntiGravity();

	//Data collection
	Radar radar                                   = new Radar();
	public static VirtualLeaderboard vLeaderboard = new VirtualLeaderboard();

	//Other
	UhOhPreventer ohUhPreventer = new UhOhPreventer();

	//Debug
	Painting debugOverlay = new Painting();
	AlphabetLogger logger = new AlphabetLogger("Main");

	//Fun
	Themer themer = new Themer();

	//Code ================================================================================================================
	//Auto movement mode
	public final int MOVEMENT_SURFING = 0;
	public final int MOVEMENT_MELEE   = 1;
	public int movementMode = -1;
	public boolean forceDisableAutoMovement = false;

	//Auto gun
	public final int GUN_GUESS_FACTOR = 0;
	public final int GUN_LINEAR       = 1;
	public final int GUN_PATTERN	  = 2;
	public int selectedGun = GUN_PATTERN;

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		//Initlize the components
		vGunManager.init(this);
		surferMove.init(this);
		// antiGravMov.init(this);
		guessFactorGun.init(this);
		linearGun.init(this);
		radar.init(this);
		meleeMove.init(this);
		debugOverlay.init(this);
		vLeaderboard.init(this);
		themer.init(this);
		patternMatchGun.init(this);
		ohUhPreventer.init(this);

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		//Main
		while (true){
			radar.execute();
			
			myLocation = new Point2D.Double(getX(), getY());
			if (!forceDisableAutoMovement) {//This is really only used by OhUhPreventer
				if (getOthers() > 1 && movementMode != MOVEMENT_MELEE) {
					logger.log("Switching to melee movement");
					movementMode = MOVEMENT_MELEE;
					radar.disableRadarManagement();// radar magagement will need to be controlled by radar again
				} else if (getOthers() <= 1 && movementMode != MOVEMENT_SURFING) {
					logger.log("Switching to surfing");
					movementMode = MOVEMENT_SURFING;
					radar.enableRadarManagement();// radar magagement will need to be controlled by radar again
				}
			}
			
			//Auto movement
			if (movementMode == MOVEMENT_MELEE) meleeMove.execute();
			
			//Auto gun
			if (movementMode == MOVEMENT_MELEE){}//Guns are handled in MeleeRobot.java during Melee
			else if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.execute();
			else if (selectedGun == GUN_LINEAR) linearGun.execute();
			
			vGunManager.execute();

			ohUhPreventer.execute();
			themer.execute();//Theme the robot, change colors and stuff
			execute();
		}
	}

	//Few helpers i need
	public double getFirePower(){
		if (radar.target == null || !radar.target.initialized){return 1;}
		return Math.min(400 / myLocation.distance(radar.target.location), 3);
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {
		radar.onScannedRobot(e);

		if (movementMode == MOVEMENT_SURFING) surferMove.onScannedRobot(e);
		else if (movementMode == MOVEMENT_MELEE) meleeMove.onScannedRobot(e);//meleeMove.onScannedRobot(e);
		
		//Multi-gun
		//if (movementMode == MOVEMENT_MELEE) {}//Guns are handled in MeleeRobot.java during Melee
		if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onScannedRobot(e);
		else if (selectedGun == GUN_LINEAR) linearGun.onScannedRobot(e);
		else if (selectedGun == GUN_PATTERN) patternMatchGun.onScannedRobot(e);
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
		// if (movementMode == MOVEMENT_MELEE) antiGravMov.onHitRobot(e);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		radar.onRobotDeath(e);
		vLeaderboard.onRobotDeath(e);
		vGunManager.onRobotDeath(e);
	}

	public void onHitWall(HitWallEvent e) {
		//logger.warn("Hit wall");
	}

	@Override
	public void onPaint(java.awt.Graphics2D g) {
		debugOverlay.onPaint(g);
		// antiGravMov.onPaint(g);
	}

	public void onMouseMoved(MouseEvent e) {
		debugOverlay.onMouseMoved(e);
	}

	public void onDeath(DeathEvent e) {
		vLeaderboard.onDeath(e);
	}

	public void onRoundEnded(RoundEndedEvent event) {
		vLeaderboard.onRoundEnded(event);
	}

	public void onBattleEnded(BattleEndedEvent event) {
		vLeaderboard.onBattleEnded(event);
	}
}