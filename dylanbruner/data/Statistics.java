package dylanbruner.data;

import java.util.ArrayList;

import dylanbruner.Alphabet;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import robocode.*;

public class Statistics extends Component {
    AlphabetLogger logger = new AlphabetLogger("Statistics");

    public static int counter_shotsHit           = 0;
    public static int counter_shotsMissed        = 0;
    public static int counter_turnsSkipped       = 0;
    public static int counter_uncaughtExceptions = 0;
    public static int counter_roundsWon          = 0;
    public static int counter_roundNumber        = 0;

    public static ArrayList<Double> distances = new ArrayList<Double>();

    public void execute(){
        if (alphabet.getTime() % 10 == 0){
            //Get the distance to the enemy
            distances.add(((Radar) alphabet.componentCore.getComponent("Radar")).target.distance);
        }
    }

    public void onBulletHit(BulletHitEvent e) {counter_shotsHit++;}
    public void onBulletMissed(BulletMissedEvent e) {counter_shotsMissed++;}
    public void onSkippedTurn(SkippedTurnEvent e) {counter_turnsSkipped++;}

    public void onBattleEnded(BattleEndedEvent e){
        logger.log("Shots Hit/M: " + counter_shotsHit+"/"+counter_shotsMissed+" ("+((double)counter_shotsHit/(double)(counter_shotsHit+counter_shotsMissed))*100+"%)");
        logger.log("Turns Skipped: " + counter_turnsSkipped);
        logger.log("Uncaught Exceptions: " + counter_uncaughtExceptions);
        logger.log("Rounds Won: " + counter_roundsWon+"/"+counter_roundNumber);
        //Highest distance, min distance, average distance
        double highest = 0;
        double lowest = 1000;
        double total = 0;
        for (double d : distances){
            if (d > highest) highest = d;
            if (d < lowest) lowest = d;
            total += d;
        }
        logger.log("Highest Distance: " + highest);
        logger.log("Lowest Distance: " + lowest);
        logger.log("Average Distance: " + (total/distances.size()));
    }

    public void onRoundEnded(RoundEndedEvent e){counter_roundNumber++;}
    public void onWin(WinEvent e){counter_roundsWon++;}
}
