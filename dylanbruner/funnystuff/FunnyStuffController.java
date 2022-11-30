package dylanbruner.funnystuff;

import dylanbruner.data.Config;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.data.Radar;

public class FunnyStuffController extends Component {
    AlphabetLogger logger = new AlphabetLogger("FunnyStuffController");

    public boolean disable_guns = false;

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
            && shouldDodge() && alphabet.getRoundNum() < Config.ABORT_AFTER_ROUNDS && alphabet.getTime() < Config.ABORT_AFTER_TIME){
            disable_guns = true;
        } else {
            disable_guns = false;
        }
    }
}
