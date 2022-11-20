package dylanb;

import robocode.*;
//import java.awt.Color;

public class Alphabet extends Robot
{
	//public void main(){}
	public void run() {
		SurfMovement surf = new SurfMovement();
		while(true) {
			System.out.println("WOAH!");
			surf.test();
			ahead(100);
			turnGunRight(360);
			back(100);
			turnGunRight(360);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		fire(1);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		back(10);
	}
	
	public void onHitWall(HitWallEvent e) {
		back(20);
	}	
}
