package dylanbruner.special;

import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import robocode.Rules;
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

    public void onScannedRobot(ScannedRobotEvent e) {
        //TODO: Make sure this is the correct name
        if (alphabet.getOthers() == 1 && e.getName().toLowerCase().contains(ROBOT_NAME.toLowerCase())){
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
                setGunStates(true);
                setMovementStates(true);
            } else if (doingAction) {
                //Calculate if we have enough time to get into position
                double timeNeeded = e.getDistance() / Rules.MAX_VELOCITY;
                double timeToTurn = Math.abs(alphabet.getHeading() + e.getBearing()) / Rules.MAX_TURN_RATE;
                double totalTime = timeNeeded + timeToTurn;
                double timeLeft = RAM_TIME - alphabet.getTime();
                if (totalTime > timeLeft){
                    logger.log("Not enough time to ram Gandalf!");
                } else {

                    //Turn the gun to the robot
                    double relTurn = alphabet.getHeading() + e.getBearing() - alphabet.getGunHeading();
                    alphabet.setTurnGunRight(relTurn);
    
                    //Face the robot
                    alphabet.setTurnRight(e.getBearing());
                    alphabet.setMaxVelocity(8);
    
                    if (alphabet.getTurnRemaining() == 0){
                        if (e.getDistance() > 100){
                            alphabet.setAhead(e.getDistance() + 100);
                        } else {
                            alphabet.setAhead(e.getDistance() + 100);
                            alphabet.setFire(3);
                        }
                    }
                }

            }
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