package dylanbruner.funnystuff;

import dylanbruner.data.Config;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import robocode.DeathEvent;
import dylanbruner.data.Radar;

public class FunnyStuffController extends Component {
    AlphabetLogger logger = new AlphabetLogger("FunnyStuffController");

    public boolean disable_guns = false;
    public static boolean has_died = false;

    public boolean shouldDodge(){
        for (String name : Config.DODGE_ME){
            if ((((Radar) alphabet.componentCore.getComponent("Radar")).target.name.toLowerCase()).startsWith(name.toLowerCase())){
                return true;
            }
        }

        return false;
    }

    public void execute(){
        if (alphabet.getOthers() == 1 && alphabet.getEnergy() > Config.ABORT_DODGE_HEALTH
            && ((Radar) alphabet.componentCore.getComponent("Radar")).target.energy > Config.ABORT_DODGE_ENEMY_HEALTH
            && shouldDodge() && alphabet.getRoundNum() < Config.ABORT_AFTER_ROUNDS && alphabet.getTime() < Config.ABORT_AFTER_TIME
            && !has_died){
            disable_guns = true;
            // alphabet.useMirorMovement = true;
        } else {
            disable_guns = false;
            alphabet.useMirorMovement = false;
        }
    }

    public void onDeath(DeathEvent e){
        has_died = true;
    }
}
