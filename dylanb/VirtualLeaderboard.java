package dylanb;

import java.util.ArrayList;
import robocode.*;

/*
 * A virtual leaderboard that is used to determine the best bot so we can target it first
 * for melee. This is a very simple implementation and is acurate enough.
 * 
 * How it works (it's very advanced):
 *   - When the robot dies log how long it survived
 *   - that's it ....
*/

public class VirtualLeaderboard {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("VirtualLeaderboard");

    //                      Name,   Score(s)
    public static ArrayList<LeaderboardEntry> leaderboard = new ArrayList<LeaderboardEntry>();
    public ArrayList<String> loggedRobotsThisRound = new ArrayList<String>();

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
        logger.log("VirtualLeaderboard initialized");
        logger.log("Already have " + leaderboard.size() + " entries");
    }
    
    public void execute(){ }

    public LeaderboardEntry[] getEntriesSorted(){
        //Sort the leaderboard using getAverageScore()
        LeaderboardEntry[] entries = leaderboard.toArray(new LeaderboardEntry[leaderboard.size()]);
        for (int i = 0; i < entries.length; i++){
            for (int j = 0; j < entries.length; j++){
                if (entries[i].getAverageScore() > entries[j].getAverageScore()){
                    LeaderboardEntry temp = entries[i];
                    entries[i] = entries[j];
                    entries[j] = temp;
                }
            }
        }
        return entries;
    }

    public void iLeaveRoundEvent(){
        //Display the leaderboard
        // LeaderboardEntry[] entries = getEntriesSorted();
        // for (int i = 0; i < entries.length; i++){
        //     LeaderboardEntry entry = entries[i];
        //     logger.log("Entry " + i + ": " + entry.name + " with score " + entry.getAverageScore());
        // }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        boolean foundRobot = false;
        for (LeaderboardEntry entry : leaderboard){
            if (entry.name.equals(e.getName())){
                entry.addScore(alphabet.getTime());
                foundRobot = true;
                break;
            }
        }
        if (!foundRobot){
            leaderboard.add(new LeaderboardEntry(e.getName(), alphabet.getTime()));
        }
        loggedRobotsThisRound.add(e.getName());
    }

    public void onBattleEnded(BattleEndedEvent e){}

    public void onRoundEnded(RoundEndedEvent e) {iLeaveRoundEvent();}
    public void onDeath(DeathEvent e){iLeaveRoundEvent();}

    public void addScore(String name, long amount){
        boolean foundRobot = false;
        for (LeaderboardEntry entry : leaderboard){
            if (entry.name.equals(name)){
                entry.addScore(amount);
                foundRobot = true;
                break;
            }
        }
        if (!foundRobot){
            leaderboard.add(new LeaderboardEntry(name, amount));
        }
    }

    public class LeaderboardEntry {
        public String name;
        public long[] scores;

        LeaderboardEntry(String name, long score){
            this.name = name;
            this.scores = new long[] {score};
        }

        public void addScore(long score){
            long[] newScores = new long[scores.length + 1];
            for (int i = 0; i < scores.length; i++){
                newScores[i] = scores[i];
            }
            newScores[scores.length] = score;
            scores = newScores;
        }

        public long getAverageScore(){
            long total = 0;
            for (int i = 0; i < scores.length; i++){
                total += scores[i];
            }
            return total / scores.length;
        }
    }
}