package dylanbruner;

import java.util.ArrayList;
import robocode.*;

/*
 * A virtual leaderboard that is used to determine the best bot so we can target it first
 * for melee. This is a very simple implementation and is acurate *enough*.
 * 
 * How it works (it's very advanced):
 *   - When the robot dies log how long it survived
 *   - that's it ....
*/

public class VirtualLeaderboard extends Component {
    AlphabetLogger logger = new AlphabetLogger("VirtualLeaderboard");

    //                      Name,   Score(s)
    public static ArrayList<LeaderboardEntry> leaderboard = new ArrayList<LeaderboardEntry>();
    public static ArrayList<Long> myScores = new ArrayList<Long>();
    public ArrayList<String> loggedRobotsThisRound = new ArrayList<String>();

    public LeaderboardEntry[] getEntriesSorted(){
        //Sort the leaderboard using getAverageScore()
        LeaderboardEntry[] entries = leaderboard.toArray(new LeaderboardEntry[leaderboard.size()]);
        //Add my entry to the array
        LeaderboardEntry myEntry = new LeaderboardEntry(alphabet.getName(), 0l);
        //Convert myScores to an array long[]
        long[] myScoresArray = new long[myScores.size()];
        for(int i = 0; i < myScores.size(); i++){myScoresArray[i] = myScores.get(i);}

        myEntry.scores = myScoresArray;
        entries = (LeaderboardEntry[]) MathUtils.resizeArray(entries, entries.length + 1);
        //Add my entry to the end of the array
        entries[entries.length - 1] = myEntry;

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
        myScores.add(alphabet.getTime());

        //Im not too sure about if this benefits the trackings or not, it could be modified to add data based on amount of energy left
        // for (Enemy enemy : alphabet.radar.enemies.values()){
        //     if (!loggedRobotsThisRound.contains(enemy.name)){
        //         loggedRobotsThisRound.add(enemy.name);
        //         //This isn't a great way to do this but it's kinda the only way to do it
        //         leaderboard.add(new LeaderboardEntry(enemy.name, alphabet.getTime() + (long) Math.random()*200));
        //     }
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

    public void onBattleEnded(BattleEndedEvent e){
        //Display the leaderboard
        logger.log("My placement: " + getMyPlacement());
        LeaderboardEntry[] entries = getEntriesSorted();

        BattleResults results = e.getResults();

        for (int i = 0; i < entries.length; i++){
            LeaderboardEntry entry = entries[i];
            if (alphabet.getName() == entry.name && results.getRank() == i + 1){
                logger.log(i + ". " + entry.name + " with score " + entry.getAverageScore()+" (Correct)");
            } else {logger.log(i + ". " + entry.name + " with score " + entry.getAverageScore());}
        }
    }

    public int getMyPlacement(){
        int totalScore = 0;
        for (long score : myScores){
            totalScore += score;
        }
        if (totalScore <= 0) return -1;
        double myAverageScore = totalScore / myScores.size();

        LeaderboardEntry[] entries = getEntriesSorted();
        //Find my placement
        int myPlacement = 0;
        //reverse loop
        for (int i = entries.length - 1; i >= 0; i--){
            LeaderboardEntry entry = entries[i];
            if (entry.getAverageScore() > myAverageScore){
                myPlacement++;
            }
        }

        if (myPlacement == 0) return 1;
        return myPlacement;
    }

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