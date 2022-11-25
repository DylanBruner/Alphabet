package dylanbruner;

import robocode.*;
import robocode.util.Utils;

public class PatternGunV2 {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("PatternGunV2");
    
    //Gun stuff
    static StringBuffer history = new StringBuffer("00000000000000000000000000000");
    static final double FIREPOWER = 2.5;
    static final double BULLETVEL = 12.5;
    static final int PATTERN_DEPTH = 30;

    public void init(Alphabet robot){alphabet = robot;}
    public void execute(){}

    public void onScannedRobot(ScannedRobotEvent e){
        double absBearing = doPatternGunV2(e);
        alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - alphabet.getGunHeadingRadians()));
        alphabet.setFire(FIREPOWER);
    }
    
    public double doPatternGunV2(ScannedRobotEvent e){
        double dist = e.getDistance();
        double absB = e.getBearingRadians() + alphabet.getHeadingRadians();
        int matchLength = PATTERN_DEPTH;
        history.insert(0, (char)(int)(Math.sin(e.getHeadingRadians() - absB) * e.getVelocity()));
        int index;
        while((index = history.toString().indexOf(history.substring(0, matchLength--), 1)) < 0) ;
        matchLength = index - (int)(dist / BULLETVEL);
        while (index >= Math.max(0, matchLength)){
            absB += Math.asin((double)(byte)history.charAt(index--) / dist);
        }
        return absB;
    }
}