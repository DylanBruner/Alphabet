package dylanbruner.special;

import dylanbruner.Alphabet;
import dylanbruner.testing.TestBot;
import dylanbruner.testing.TestLoader;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.ComponentCore;
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
    public final int RAM_TIME = 170; //How fast we need to be able to get in position to ram when deciding if we should ram
    public final String ROBOT_NAME = "Gandalf";

    //Code ==========================================

    public static boolean gottenIntoFallbackMode = false;
    public boolean doingAction = false;

    TestLoader loader;

    public void execute(){
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        //TODO: Make sure this is the correct name
        if (alphabet.getOthers() == 1 && e.getName().toLowerCase().contains(ROBOT_NAME.toLowerCase())){
            Alphabet.DEV_DISABLE_MOST = true;
            alphabet.selectedGun = alphabet.GUN_GUESS_FACTOR;// Because the robot is a surfer
            if (alphabet.getRoundNum() == 0 && (!gottenIntoFallbackMode && !doingAction)){
                logger.log("Found Gandalf! Exploiting fallback");
                doingAction = true;
                //Disable all guns
                alphabet.setAhead(0);
                alphabet.setTurnRight(0);
                setGunStates(false);
                setMovementStates(false);
            } 

            if (doingAction && e.getEnergy() < 100){
                doingAction = false;
                gottenIntoFallbackMode = true;
                logger.log("Detected Energy drop! Fallback mode enabled!");

                alphabet.componentCore.unloadAll();

                loader = new TestLoader();
                loader.load(new TestBot());
                alphabet.componentCore.registerComponent(loader);
                ((TestBot) loader.child).init(alphabet);
            }
        }

        if (gottenIntoFallbackMode && !alphabet.componentCore.componentLookup.containsKey("TestLoader")){
            alphabet.componentCore.unloadAll();

            loader = new TestLoader();
            loader.load(new TestBot());
            alphabet.componentCore.registerComponent(loader);
            ((TestBot) loader.child).init(alphabet);
        }
    }

    //Helpers
    public void setGunStates(boolean state){
        for (String gunName : GUN_NAMES){alphabet.componentCore.setComponentState(gunName, state);}
    }
    public void setMovementStates(boolean state){
        alphabet.componentCore.setComponentState("SurfMovement", state);
        alphabet.componentCore.setComponentState("MeleeRobot", state);
    }
}