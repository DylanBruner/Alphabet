package dylanbruner;

import robocode.*;

public class Statistics extends Component {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Statistics");

    public static int counter_shotsHit           = 0;
    public static int counter_shotsMissed        = 0;
    public static int counter_turnsSkipped       = 0;
    public static int counter_uncaughtExceptions = 0;

    public void onBulletHit(BulletHitEvent e) {counter_shotsHit++;}
    public void onBulletMissed(BulletMissedEvent e) {counter_shotsMissed++;}
    public void onSkippedTurn(SkippedTurnEvent e) {counter_turnsSkipped++;}

    public void onBattleEnded(BattleEndedEvent e){
        logger.log("Shots Hit/M: " + counter_shotsHit+"/"+counter_shotsMissed+" ("+((double)counter_shotsHit/(double)(counter_shotsHit+counter_shotsMissed))*100+"%)");
        logger.log("Turns Skipped: " + counter_turnsSkipped);
        logger.log("Uncaught Exceptions: " + counter_uncaughtExceptions);
    }
}
