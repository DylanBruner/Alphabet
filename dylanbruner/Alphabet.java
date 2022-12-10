package dylanbruner;

import robocode.*;
import java.awt.geom.*;
import java.util.function.Function;
import java.awt.event.MouseEvent;

import dylanbruner.data.Radar;
import dylanbruner.data.Statistics;
import dylanbruner.data.VirtualGunManager;
import dylanbruner.data.VirtualLeaderboard;
import dylanbruner.gun.GuessFactorGun;
import dylanbruner.gun.HeadOnGun;
import dylanbruner.gun.LinearGun;
import dylanbruner.gun.PatternGunV2;
import dylanbruner.gun.PatternMatchGun;
import dylanbruner.move.MeleeRobot;
import dylanbruner.move.MirrorMovement;
import dylanbruner.move.SurfMovement;
import dylanbruner.move.UhOhPreventer;
import dylanbruner.special.SpecialGandalf;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.ComponentCore;
import dylanbruner.util.Painting;
import dylanbruner.util.Themer;
import dylanbruner.funnystuff.FunnyStuffController;


/*
 * Overview: Moved to README.md
 * 
 * His robot falls back into normal shooting if we don't do anything for like 250 ticks during the first round
 * NOTE: We only need to do this the first round
 * 
 * Changes i need/want to do:
 *   - SEGMENT WAVE SURVING, this is the number one thing i need to do
 *     - Things to segment upon:
 *        - Distance
 *       
 *   - I could possibly trick his bot into falling back into normal surfing+shooting and then we switch to bullet shielding
 *   - I could also try to write a anti-wave surfing gun which should do well
 *   - Mirror Movement????
 * 
 * test
*/

public class Alphabet extends AdvancedRobot {
	AlphabetLogger logger              = new AlphabetLogger("Main");
	public ComponentCore componentCore = new ComponentCore(this);

	//Code ================================================================================================================
	//Auto movement mode
	public final int MOVEMENT_SURFING = 0;
	public final int MOVEMENT_MELEE   = 1;
	public int movementMode = -1;
	public boolean forceDisableAutoMovement = false;
	public boolean useMirorMovement = false; //When i figure out how scoring works I'll probably make this a thing
											 //For example if i've won 2/3 games and i dont need to win the third i'll turn this on

	//Auto gun
	public final int GUN_GUESS_FACTOR = 0;
	public final int GUN_LINEAR       = 1;
	public final int GUN_PATTERN	  = 2;
	public final int GUN_HEAD_ON	  = 3;
	public final int GUN_PATTERN_V2   = 4;
	public int selectedGun = GUN_PATTERN_V2;

	//Other public variables
	public Point2D.Double myLocation;

	public static boolean DEV_DISABLE_MOST = false;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		//=======================================================[Components]=======================================================
		componentCore.registerComponents(new Component[] {
			new Radar(), new Painting(), new Themer(),
			new UhOhPreventer(), new VirtualGunManager(), new Statistics(),
			new PatternGunV2(), new LinearGun(), new HeadOnGun(),
			new GuessFactorGun(), new PatternMatchGun(), new MeleeRobot(),
			new SurfMovement(), new VirtualLeaderboard(), new MirrorMovement(),
			new FunnyStuffController(), new SpecialGandalf()
		});

		//Shooting =======================================================

		componentCore.setEventConditional("PatternGunV2", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_PATTERN_V2 
			       && alphabet.movementMode == alphabet.MOVEMENT_SURFING 
				   && ((FunnyStuffController) componentCore.getComponent("FunnyStuffController")).disable_guns == false;
		});
		componentCore.setEventConditional("LinearGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_LINEAR 
			       && alphabet.movementMode == alphabet.MOVEMENT_SURFING
				   && ((FunnyStuffController) componentCore.getComponent("FunnyStuffController")).disable_guns == false;
		});
		componentCore.setEventConditional("HeadOnGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_HEAD_ON 
			       && alphabet.movementMode == alphabet.MOVEMENT_SURFING
				   && ((FunnyStuffController) componentCore.getComponent("FunnyStuffController")).disable_guns == false;
		});
		componentCore.setEventConditional("GuessFactorGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_GUESS_FACTOR 
			       && alphabet.movementMode == alphabet.MOVEMENT_SURFING
				   && ((FunnyStuffController) componentCore.getComponent("FunnyStuffController")).disable_guns == false;
		});
		componentCore.setEventConditional("PatternMatchGun", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.selectedGun == alphabet.GUN_PATTERN 
			       && alphabet.movementMode == alphabet.MOVEMENT_SURFING
				   && ((FunnyStuffController) componentCore.getComponent("FunnyStuffController")).disable_guns == false;
		});

		//Movement =======================================================

		//Melee movement
		componentCore.setEventConditional("MeleeRobot", componentCore.ON_EXECUTE, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_MELEE && !alphabet.useMirorMovement;
		});
		componentCore.setEventConditional("MeleeRobot", componentCore.ON_SCANNED_ROBOT, (Alphabet alphabet) -> {
			return alphabet.movementMode == alphabet.MOVEMENT_MELEE && !alphabet.useMirorMovement;
		});

		Function<Alphabet, Boolean> surfingMovementConditional = (Alphabet alphabet) -> {return alphabet.movementMode == alphabet.MOVEMENT_SURFING && !alphabet.useMirorMovement;};
		componentCore.setEventConditional("SurfMovement", componentCore.ON_SCANNED_ROBOT, surfingMovementConditional);
		componentCore.setEventConditional("SurfMovement", componentCore.ON_HIT_BY_BULLET, surfingMovementConditional);
		componentCore.setEventConditional("SurfMovement", componentCore.ON_BULLET_HIT, surfingMovementConditional);
		componentCore.setEventConditional("SurfMovement", componentCore.ON_BULLET_HIT_BULLET, surfingMovementConditional);

		//Mirror movement, this is mostly just a joke lol
		componentCore.setEventConditional("MirrorMovement", componentCore.ON_EXECUTE, (Alphabet alphabet) -> {
			return alphabet.useMirorMovement;
		});

		//=======================================================[Robot]=======================================================

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		//Main
		while (true){
			componentCore.execute();

			if (!DEV_DISABLE_MOST) {
				myLocation = new Point2D.Double(getX(), getY());
				
				if (!forceDisableAutoMovement) {//This is really only used by OhUhPreventer
					if (getOthers() > 1 && movementMode != MOVEMENT_MELEE) {
						logger.log("Switching to melee movement");
						movementMode = MOVEMENT_MELEE;
					} else if (getOthers() <= 1 && movementMode != MOVEMENT_SURFING) {
						logger.log("Switching to surfing");
						movementMode = MOVEMENT_SURFING;
						((Radar) componentCore.getComponent("Radar")).clearRadarLock();
					}
				}
				
			}
			execute();
		}
	}

	//Few helpers i need
	public double getFirePower(){
		if (((Radar) componentCore.getComponent("Radar")).target == null || !((Radar) componentCore.getComponent("Radar")).target.initialized){return 1;}
		return Math.min(400 / myLocation.distance(((Radar) componentCore.getComponent("Radar")).target.location), 3);
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
	public void onCustomEvent(CustomEvent event) {componentCore.onCustomEvent(event);}
}