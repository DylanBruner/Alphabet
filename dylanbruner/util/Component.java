package dylanbruner.util;

import robocode.*;
import java.awt.event.MouseEvent;

import dylanbruner.Alphabet;

public class Component {
    public Alphabet alphabet;
    public AlphabetLogger logger;
    public String componentTag;

    public void init(Alphabet alphabet) {
        this.alphabet = alphabet; //The component shouldn't need to this manually
    }

    // Can be overridden by the component to internally setup conditionals
    public void setupConditionals(ComponentCore componentCore){}

    //All events that should be overridden by the component
    public void execute() { }
    public void onScannedRobot(ScannedRobotEvent e) { }
    public void onHitByBullet(HitByBulletEvent e) { }
    public void onBulletHit(BulletHitEvent e) { }
    public void onBulletMissed(BulletMissedEvent e) { }
    public void onBulletHitBullet(BulletHitBulletEvent e) { }
    public void onHitRobot(HitRobotEvent e) { }
    public void onRobotDeath(RobotDeathEvent e) { }
    public void onHitWall(HitWallEvent e) { }
    public void onPaint(java.awt.Graphics2D g) { }
    public void onMouseMoved(MouseEvent e) { }
    public void onDeath(DeathEvent e) { }
    public void onWin(WinEvent e) { }
    public void onRoundEnded(RoundEndedEvent e) { }
    public void onBattleEnded(BattleEndedEvent e) { }
    public void onSkippedTurn(SkippedTurnEvent e) { }
    public void onStatus(StatusEvent e) { }
    public void onCustomEvent(CustomEvent e) { }
}
