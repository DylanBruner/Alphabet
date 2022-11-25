package dylanbruner;

import robocode.*;
import java.awt.geom.*;
import java.util.function.Function;
import java.awt.event.MouseEvent;

/*
 *==============================[OVERVIEW]==============================]
 * Shooting: (Auto switches)
 *   - Virtual Guns
 *   - GuessFactor, Linear, pattern matching and head on
 * Movement: (Auto switches)
 *   - Melee: Minimum Risk Movement
 *   - 1v1: Wave Surfing
 * Targeting:
 *   - Virtual Leaderboard (already implemented) will be used to determine the best bot to target
 *   - In 1v1 we just target the enemy (duh)
 * Data Collecting:
 *   - Virtual Gun Data         (automatic gun switching)
 *   - Virtual leaderboard data (for melee targetting)
 *   - When enemies are scanned we store a 'snapshot' of their data, max set in Config.java
 *     this will be used for pattern match targeting and whatever 
 *     else could benefit from it. We could analyze the data and
 * 	   calculate it into the leaderboard data to make it more accurate. (Energy loss vs Gain or Loss vs time)
 * 
 * 
 * I wont be switching over to the virtual leaderboard just yet as doesn't play well with the current melee movement
 * Also implementing Keep Distance into the wave surfing would improve performance immensely
 * 
 * The guns need to be moved over to targeting the target set by the radar not just the last scanned robot
*/

public class Alphabet extends AdvancedRobot {
	//Attacking
	GuessFactorGun guessFactorGun    = new GuessFactorGun();
	PatternMatchGun patternMatchGun  = new PatternMatchGun();
	HeadOnGun headOnGun              = new HeadOnGun();

	//Movement
	SurfMovement surferMove = new SurfMovement();
	MeleeRobot meleeMove    = new MeleeRobot(); //Melee movement basically takes over the whole bot until it's done
	//AntiGravity antiGravMov = new AntiGravity();

	//Data collection
	Radar radar                                   = new Radar();
	public static VirtualLeaderboard vLeaderboard = new VirtualLeaderboard();

	//Debug
	AlphabetLogger logger = new AlphabetLogger("Main");

	//New
	ComponentCore componentCore = new ComponentCore(this);
	//Code ================================================================================================================
	//Auto movement mode
	public final int MOVEMENT_SURFING = 0;
	public final int MOVEMENT_MELEE   = 1;
	public int movementMode = -1;
	public boolean forceDisableAutoMovement = false;

	//Auto gun
	public final int GUN_GUESS_FACTOR = 0;
	public final int GUN_LINEAR       = 1; //24.91, 26.84
	public final int GUN_PATTERN	  = 2; //21.59, 21.56
	public final int GUN_HEAD_ON	  = 3; //30.42, 25.61
	public final int GUN_PATTERN_V2   = 4;
	public int selectedGun = GUN_PATTERN_V2;// ^ For some reason starting with this gun gives the best results

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		//=======================================================[Components]=======================================================
		componentCore.registerComponent(new Painting());
		componentCore.registerComponent(new Themer());
		componentCore.registerComponent(new UhOhPreventer());
		componentCore.registerComponent(new VirtualGunManager());
		componentCore.registerComponent(new Statistics());
		componentCore.registerComponent(new PatternGunV2());
		componentCore.registerComponent(new LinearGun());

		//Setup event conditionals
		
		//Only shoot the PatternGunV2 if it's selected
		componentCore.setEventConditional("PatternGunV2", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_PATTERN_V2;
		});

		//Only shoot the LinearGun if it's selected
		componentCore.setEventConditional("LinearGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_LINEAR;
		});

		//=======================================================[Robot]=======================================================

		myLocation = new Point2D.Double(getX(), getY());

		//Initlize the components
		surferMove.init(this);
		// antiGravMov.init(this);
		guessFactorGun.init(this);
		headOnGun.init(this);
		radar.init(this);
		meleeMove.init(this);
		vLeaderboard.init(this);
		patternMatchGun.init(this);

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		//Main
		while (true){
			componentCore.execute();
			radar.execute();
			
			myLocation = new Point2D.Double(getX(), getY());
			if (!forceDisableAutoMovement) {//This is really only used by OhUhPreventer
				if (getOthers() > 1 && movementMode != MOVEMENT_MELEE) {
					logger.log("Switching to melee movement");
					movementMode = MOVEMENT_MELEE;
					// radar.disableRadarManagement();// radar magagement will need to be controlled by radar again
				} else if (getOthers() <= 1 && movementMode != MOVEMENT_SURFING) {
					logger.log("Switching to surfing");
					movementMode = MOVEMENT_SURFING;
					// radar.enableRadarManagement();// radar magagement will need to be controlled by radar again
				}
			}
			
			//Auto movement
			if (movementMode == MOVEMENT_MELEE) meleeMove.execute();
			
			//Auto gun
			if (movementMode == MOVEMENT_MELEE){}//Guns are handled in MeleeRobot.java during Melee
			else if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.execute();

			
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
		componentCore.onScannedRobot(e);

		radar.onScannedRobot(e);

		if (movementMode == MOVEMENT_SURFING) surferMove.onScannedRobot(e);
		else if (movementMode == MOVEMENT_MELEE) meleeMove.onScannedRobot(e);//meleeMove.onScannedRobot(e);
		
		//Multi-gun
		//if (movementMode == MOVEMENT_MELEE) {}//Guns are handled in MeleeRobot.java during Melee
		if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onScannedRobot(e);
		else if (selectedGun == GUN_PATTERN) patternMatchGun.onScannedRobot(e);
		else if (selectedGun == GUN_HEAD_ON) headOnGun.onScannedRobot(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		componentCore.onHitByBullet(e);
		if (movementMode == MOVEMENT_SURFING) surferMove.onHitByBullet(e);
	}

	public void onBulletHit(BulletHitEvent e) {
		componentCore.onBulletHit(e);
		if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHit(e);

		//Multi-gun
		if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onBulletHit(e);
		//else if (gunMode == GUN_LINEAR) linearGun.onBulletHit(e);
	}

	public void onBulletMissed(BulletMissedEvent e) {
		componentCore.onBulletMissed(e);
		//Multi-gun
		if (selectedGun == GUN_GUESS_FACTOR) guessFactorGun.onBulletMissed(e);
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		componentCore.onBulletHitBullet(e);
		if (movementMode == MOVEMENT_SURFING) surferMove.onBulletHitBullet(e);
	}

	public void onHitRobot(HitRobotEvent e) {
		componentCore.onHitRobot(e);
		// if (movementMode == MOVEMENT_MELEE) antiGravMov.onHitRobot(e);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		componentCore.onRobotDeath(e);
		radar.onRobotDeath(e);
		vLeaderboard.onRobotDeath(e);
	}

	public void onHitWall(HitWallEvent e) {
		componentCore.onHitWall(e);
	}

	@Override
	public void onPaint(java.awt.Graphics2D g) {
		componentCore.onPaint(g);
	}

	public void onMouseMoved(MouseEvent e) {
		componentCore.onMouseMoved(e);
	}

	public void onDeath(DeathEvent e) {
		componentCore.onDeath(e);
		vLeaderboard.onDeath(e);
	}

	public void onRoundEnded(RoundEndedEvent event) {
		componentCore.onRoundEnded(event);
		vLeaderboard.onRoundEnded(event);
	}

	public void onBattleEnded(BattleEndedEvent event) {
		componentCore.onBattleEnded(event);
		vLeaderboard.onBattleEnded(event);
	}
}