package dylanbruner.special;

import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import robocode.ScannedRobotEvent;

/*
 * This is a special component, basically it's just used to program in a edge case for
 * specific robots. In this case, the first round we disable guns and wait till Gandalf shoots
 * this is so we can get it to disable bullet shielding so we can just fight him normally
*/

public class SpecialGandalf extends Component {
    AlphabetLogger logger = new AlphabetLogger("Special:Gandalf");

    //Config ========================================
    public final String[] GUN_NAMES = {"GuessFactorGun", "HeadOnGun", "LinearGun", 
                                       "MeleeGun", "PatternGunV2", "PatternMatchGun"};

    //Code ==========================================

    public static boolean gottenIntoFallbackMode = false;

    public boolean doingAction = false;

    public void onScannedRobot(ScannedRobotEvent e) {
        //TODO: Make sure this is the correct name
        if (alphabet.getOthers() == 1 && e.getName().toLowerCase().contains("super")){
            alphabet.selectedGun = alphabet.GUN_GUESS_FACTOR;// Because the robot is a surfer
            if (alphabet.getRoundNum() == 0 && (!gottenIntoFallbackMode && !doingAction)){
                logger.log("Found Gandalf! Exploiting fallback");
                doingAction = true;
                //Disable all guns
                setGunStates(false);
            }

            if (doingAction && e.getEnergy() < 100){
                doingAction = false;
                gottenIntoFallbackMode = true;
                logger.log("Detected Energy drop! Fallback mode enabled!");
                setGunStates(true);
            }
        }
    }

    //Helpers
    public void setGunStates(boolean state){
        for (String gunName : GUN_NAMES){alphabet.componentCore.setComponentState(gunName, state);}
    }
}