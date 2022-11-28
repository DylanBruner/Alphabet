package dylanbruner.gun;

import java.util.Hashtable;

import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import robocode.*;
import robocode.util.Utils;
/*
 * Modified from https://robowiki.net/wiki/Assertive
 * 
 * List of modoifications:
 *   - Increase history size
 *   - Gets the bullet power from Alphabet
 *   - Store history in a hashtable per robot, this gun was originally for 1v1
 *   - Convert to a component
*/

/*
 * New pattern gun, this thing is nuts
 * I'm not 100% sure on the math of how it works but I have a fairly
 * solid understanding of how the code works. so heres my best explanation of how it works
 * 
 * The location history is stored in a single string, each character represents a "snapshot" of the enemy
 * It's stored by doing: `(char)(int)(Math.sin(e.getHeadingRadians() - absoluteBearing) * e.getVelocity())`
 * this first does some math and then casts it to a int and finally to a char so it can be stored in the history string
*/

public class PatternGunV2 extends Component {
    AlphabetLogger logger = new AlphabetLogger("PatternGunV2");
    
    //Gun stuff
    static StringBuffer history_base = new StringBuffer("00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    static Hashtable<String, StringBuffer> patterns = new Hashtable<String, StringBuffer>();
    //This should modify it to work in melee
    static final int PATTERN_DEPTH = 30;

    public void onScannedRobot(ScannedRobotEvent e){
        double bulletPower = alphabet.getFirePower();
        double absBearing = doPatternGunV2(e, bulletPower);
        alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - alphabet.getGunHeadingRadians()));
        alphabet.setFire(bulletPower);
    }
    
    public double doPatternGunV2(ScannedRobotEvent e, double bulletPower){
        double bulletVelocity = 20 - bulletPower * 3;
        double distance = e.getDistance();
        double absoluteBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        if (!patterns.containsKey(e.getName())){
            patterns.put(e.getName(), new StringBuffer(history_base));
        }
        StringBuffer history = patterns.get(e.getName());

        int matchLength = PATTERN_DEPTH;
        //This basically converts the current velocity and heading into a character and stores it in the history
        history.insert(0, (char)(int)(Math.sin(e.getHeadingRadians() - absoluteBearing) * e.getVelocity()));
        /*
        1. We subtract the absolute bearing from the heading to determine the direction that the enemy is moving.
        2. We then multiply that by the velocity.
        3. We then take the sine of that value to determine the direction that the enemy is moving. (1 for forward, -1 for backwards)
        4. We then cast that value to a char and then insert it into the array.
        Not sure about this explanation it was auto generated while I was learning how this code works
        */
        patterns.put(e.getName(), history);
        
        int index;
        //This finds the index of the last occurance of the pattern in the history
        while((index = history.toString().indexOf(history.substring(0, matchLength--), 1)) < 0);
        
        //This is the part i screwed up in my original implementation
        //This plays the pattern for however long we need to aka (distance / bullet velocity)
        matchLength = index - (int)(distance / bulletVelocity);
        while (index >= Math.max(0, matchLength)){
            absoluteBearing += Math.asin((double)(byte)history.charAt(index--) / distance);
        }

        return absoluteBearing;
    }
}