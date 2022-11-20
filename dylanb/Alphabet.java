package dylanb;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;

public class Alphabet extends AdvancedRobot
{
	SurfMovement surferMove       = new SurfMovement();
	GuessFactorGun guessFactorGun = new GuessFactorGun();
	Radar radar                   = new Radar();
	AlphabetLogger logger         = new AlphabetLogger("Main");

	//Other public variables
	public Point2D.Double myLocation;

	public void run() {
		//Setup components
		surferMove.init(this);
		guessFactorGun.init(this);
		radar.init(this);

		//Setup robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setColors(Color.black, Color.blue, Color.red);
		
		//Main
		while (true){
			myLocation = new Point2D.Double(getX(), getY());

			surferMove.execute();
			guessFactorGun.execute();
			radar.execute();

			execute();
		}
	}

	//Events 'n stuff
	public void onScannedRobot(ScannedRobotEvent e) {
		radar.onScannedRobot(e);
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
