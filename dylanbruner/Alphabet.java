package dylanbruner;

import robocode.*;
import java.awt.geom.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.awt.event.MouseEvent;

import dylanbruner.data.*;
import dylanbruner.gun.*;
import dylanbruner.move.*;
import dylanbruner.special.SpecialGandalf;
import dylanbruner.util.*;
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
	AlphabetLogger logger = new AlphabetLogger("Main");
	public ComponentCore componentCore = new ComponentCore(this);

	// Code
	// ================================================================================================================
	// Auto movement mode
	public final int MOVEMENT_SURFING = 0;
	public final int MOVEMENT_MELEE = 1;
	public int movementMode = -1;
	public boolean forceDisableAutoMovement = false;
	public boolean useMirorMovement = false; // When i figure out how scoring works I'll probably make this a thing
												// For example if i've won 2/3 games and i dont need to win the third
												// i'll turn this on

	// Auto gun
	public final int GUN_GUESS_FACTOR = 0;
	public final int GUN_LINEAR = 1;
	public final int GUN_PATTERN = 2;
	public final int GUN_HEAD_ON = 3;
	public final int GUN_PATTERN_V2 = 4;
	public int selectedGun = GUN_GUESS_FACTOR;

	// Other public variables
	public Point2D.Double myLocation;

	public static boolean DEV_DISABLE_MOST = false;


	static final String RED = "\u001B[31m";
    static final String GREEN = "\u001B[32m";
    static final String RESET = "\u001B[0m";
	public static void main(String[] args) { // for development
		try {
			Process proc = new ProcessBuilder("python", "compile.py").start();
			new Thread(() -> new BufferedReader(new InputStreamReader(proc.getInputStream()))
					.lines().forEach(System.out::println)).start();
			new Thread(() -> new BufferedReader(new InputStreamReader(proc.getErrorStream()))
					.lines().forEach(System.err::println)).start();
			int exitCode = proc.waitFor();
			System.out.println(
					exitCode == 0 ? GREEN + "Compilation successful" + RESET
							: RED + "Error compiling Python code. Exit code: " + exitCode + RESET);
		} catch (IOException | InterruptedException e) {
			System.err.println("Error running compilation process: " + e.getMessage());
		}
	}

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		componentCore.registerComponents(new Component[] {
				new Radar(), new Painting(), new Themer(),
				new UhOhPreventer(), new VirtualGunManager(), new Statistics(),
				new PatternGunV2(), new LinearGun(), new HeadOnGun(),
				new GuessFactorGun(), new PatternMatchGun(), new MeleeRobot(),
				new SurfMovement(), new VirtualLeaderboard(), new MirrorMovement(),
				new FunnyStuffController(), new SpecialGandalf()
		});

		// =======================================================[Robot]=======================================================

		// Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// Main
		while (true) {
			componentCore.execute();

			if (!DEV_DISABLE_MOST) {
				myLocation = new Point2D.Double(getX(), getY());

				if (!forceDisableAutoMovement) {// This is really only used by OhUhPreventer
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

	public double getFirePower() {
		Enemy target = ((Radar) componentCore.getComponent("Radar")).target;
		if (target == null) return 1;

        double bulletPower = (target.location.distance(myLocation) < 100) ? 2.95 : 1.95;
        bulletPower = Math.min(bulletPower, target.energy / 4); // Only use amount of energy required
        if (getEnergy() < 20) {
            bulletPower = Math.min(bulletPower, 1);
        }

		if (target.isShielding()) {
			bulletPower = 0.1;
		}

        return Math.min(Math.max(bulletPower, .1), 3);
    }

	// Events 'n stuff
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
	public void onPaint(java.awt.Graphics2D g) {componentCore.onPaint(g);}
	public void onCustomEvent(CustomEvent event) {componentCore.onCustomEvent(event);}
}