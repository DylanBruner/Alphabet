package dylanbruner;

import robocode.*;
import java.awt.geom.*;
import java.awt.event.MouseEvent;

/*
 * Overview: Moved to README.md 
*/

public class Alphabet extends AdvancedRobot {
	AlphabetLogger logger       = new AlphabetLogger("Main");
	ComponentCore componentCore = new ComponentCore(this);

	Radar radar;//This is the only component that is accessable without using the componentCore.getComponent() method
	            //I'm doing this because the radar is used in nearly every file LOTS of times and I dont want to change it over lol
				//TLDR: I'm lazy and I dont want to change it over

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
	public int selectedGun = GUN_PATTERN;// ^ For some reason starting with this gun gives the best results

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		//=======================================================[Components]=======================================================
		//Register components (calls init() on each component which gives them access to the robot)
		componentCore.registerComponent(new Radar()); //This *should* give the radar the first execution
		componentCore.registerComponent(new Painting());
		componentCore.registerComponent(new Themer());
		componentCore.registerComponent(new UhOhPreventer());
		componentCore.registerComponent(new VirtualGunManager());
		componentCore.registerComponent(new Statistics());
		componentCore.registerComponent(new PatternGunV2());
		componentCore.registerComponent(new LinearGun());
		componentCore.registerComponent(new HeadOnGun());
		componentCore.registerComponent(new GuessFactorGun());
		componentCore.registerComponent(new PatternMatchGun());
		componentCore.registerComponent(new MeleeRobot());
		componentCore.registerComponent(new SurfMovement());
		componentCore.registerComponent(new VirtualLeaderboard());
		componentCore.registerComponent(new ShadowGun());

		radar = (Radar) componentCore.getComponent("Radar");//Go to where i create the variable radar to see why I'm doing this

		//Shooting =======================================================

		componentCore.setEventConditional("PatternGunV2", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_PATTERN_V2 && alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("LinearGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_LINEAR && alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("HeadOnGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_HEAD_ON && alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("GuessFactorGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_GUESS_FACTOR && alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("PatternMatchGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_PATTERN && alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});

		//Movement =======================================================

		//Melee movement
		componentCore.setEventConditional("MeleeRobot", componentCore.ON_EXECUTE, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_MELEE;
		});
		componentCore.setEventConditional("MeleeRobot", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_MELEE;
		});

		//Surfing movement (lots of events)
		componentCore.setEventConditional("SurfMovement", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("SurfMovement", componentCore.ON_HIT_BY_BULLET, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("SurfMovement", componentCore.ON_BULLET_HIT, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});
		componentCore.setEventConditional("SurfMovement", componentCore.ON_BULLET_HIT_BULLET, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_SURFING;
		});

		//=======================================================[Robot]=======================================================

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		//Main
		while (true){
			myLocation = new Point2D.Double(getX(), getY());
			componentCore.execute();
			
			if (!forceDisableAutoMovement) {//This is really only used by OhUhPreventer
				if (getOthers() > 1 && movementMode != MOVEMENT_MELEE) {
					logger.log("Switching to melee movement");
					movementMode = MOVEMENT_MELEE;
				} else if (getOthers() <= 1 && movementMode != MOVEMENT_SURFING) {
					logger.log("Switching to surfing");
					movementMode = MOVEMENT_SURFING;
				}
			}
			
			execute();
		}
	}

	//Few helpers i need
	public double getFirePower(){
		if (radar.target == null || !radar.target.initialized){return 1;}
		return Math.min(400 / myLocation.distance(radar.target.location), 3);
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {componentCore.onScannedRobot(e);}
	public void onRobotDeath(RobotDeathEvent e) {componentCore.onRobotDeath(e);}
	public void onHitByBullet(HitByBulletEvent e) {componentCore.onHitByBullet(e);}
	public void onBulletHit(BulletHitEvent e) {componentCore.onBulletHit(e);}
	public void onBulletMissed(BulletMissedEvent e) {componentCore.onBulletMissed(e);}
	public void onBulletHitBullet(BulletHitBulletEvent e) {componentCore.onBulletHitBullet(e);}
	public void onHitRobot(HitRobotEvent e) {componentCore.onHitRobot(e);}
	public void onHitWall(HitWallEvent e) {componentCore.onHitWall(e);}
	public void onMouseMoved(MouseEvent e) {componentCore.onMouseMoved(e);}
	public void onDeath(DeathEvent e) {componentCore.onDeath(e);}
	public void onRoundEnded(RoundEndedEvent event) {componentCore.onRoundEnded(event);}
	public void onBattleEnded(BattleEndedEvent event) {componentCore.onBattleEnded(event);}
	public void onWin(WinEvent event) {componentCore.onWin(event);}
	@Override
	public void onPaint(java.awt.Graphics2D g) {componentCore.onPaint(g);}
}