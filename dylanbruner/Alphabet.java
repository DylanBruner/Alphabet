package dylanbruner;

import robocode.*;
import java.awt.geom.*;
import java.awt.event.MouseEvent;

import dylanbruner.data.Radar;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.ComponentCore;


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
*/

public class Alphabet extends AdvancedRobot {
	AlphabetLogger logger              = new AlphabetLogger("Main");
	public ComponentCore componentCore = new ComponentCore(this);

	//Code ================================================================================================================
	//Auto movement mode
	public boolean forceDisableAutoMovement = false;
	public boolean useMirorMovement = false; //When i figure out how scoring works I'll probably make this a thing
											 //For example if i've won 2/3 games and i dont need to win the third i'll turn this on

	//Auto gun
	public final int GUN_GUESS_FACTOR = 0;
	public final int GUN_LINEAR       = 1;
	public final int GUN_PATTERN	  = 2;
	public final int GUN_HEAD_ON	  = 3;
	public final int GUN_PATTERN_V2   = 4;
	public int selectedGun = -1;

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		myLocation = new Point2D.Double(getX(), getY());

		//=======================================================[Components]=======================================================
		componentCore.registerComponents(new Component[] {
			new Radar()
		});

		//=======================================================[Robot]=======================================================

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		//Main
		while (true){
			myLocation = new Point2D.Double(getX(), getY());
			
			componentCore.execute();			
			execute();
		}
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