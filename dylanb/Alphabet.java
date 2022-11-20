package dylanb;

import robocode.*;
//import java.awt.Color;

public class Alphabet extends AdvancedRobot
{
	SurfMovement surferMove       = new SurfMovement();
	GuessFactorGun guessFactorGun = new GuessFactorGun();
	AlphabetLogger logger         = new AlphabetLogger("Main");

	public void run() {
		//Setup components
		surferMove.init(this);
		guessFactorGun.init(this);

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true){
			surferMove.execute();
			guessFactorGun.execute();
			
			execute(); //Not sure if this is needed yet
		}
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {
		surferMove.onScannedRobot(e);
		guessFactorGun.onScannedRobot(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		surferMove.onHitByBullet(e);
	}

	public void onBulletHit(BulletHitEvent e) {
		surferMove.onBulletHit(e);
		guessFactorGun.onBulletHit(e);
	}

	public void onBulletMissed(BulletMissedEvent e) {
		guessFactorGun.onBulletMissed(e);
	}
}
