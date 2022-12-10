package dylanbruner.testing;

import dylanbruner.Alphabet;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;

public class TestLoader extends Component {
    AlphabetLogger logger = new AlphabetLogger("TestLoader");
    public Component child;
    
    public void load(Component bot){
        child = bot;
    }

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    //Pass through all events
    public void onScannedRobot(robocode.ScannedRobotEvent e){ child.onScannedRobot(e); }
    public void onHitRobot(robocode.HitRobotEvent e){ child.onHitRobot(e); }
    public void onHitByBullet(robocode.HitByBulletEvent e){ child.onHitByBullet(e); }
    public void onBulletHit(robocode.BulletHitEvent e){ child.onBulletHit(e); }
    public void onBulletMissed(robocode.BulletMissedEvent e){ child.onBulletMissed(e); }
    public void onBulletHitBullet(robocode.BulletHitBulletEvent e){ child.onBulletHitBullet(e); }
    public void onRobotDeath(robocode.RobotDeathEvent e){ child.onRobotDeath(e); }
    public void onWin(robocode.WinEvent e){ child.onWin(e); }
    public void onDeath(robocode.DeathEvent e){ child.onDeath(e); }
    public void onHitWall(robocode.HitWallEvent e){ child.onHitWall(e); }
    public void onSkippedTurn(robocode.SkippedTurnEvent e){ child.onSkippedTurn(e); }
    public void onStatus(robocode.StatusEvent e){ child.onStatus(e); }
    public void onBattleEnded(robocode.BattleEndedEvent e){ child.onBattleEnded(e); }
    public void onRoundEnded(robocode.RoundEndedEvent e){ child.onRoundEnded(e); }
    public void onCustomEvent(robocode.CustomEvent e){ child.onCustomEvent(e); }
}