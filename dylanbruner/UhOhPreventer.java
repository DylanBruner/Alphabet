package dylanbruner;

/*
 * This does exactly what the name says. It prevents the robot from
 * doing a uh-oh. Like for some reason just sitting still and not
 * moving. (This really only affects wave surfing) 
 * 
 * Allthough i think i fixed the problem that caused wave surfing to fail, 
 * i'm still going to keep this in just in case.
*/

import java.awt.geom.*;

public class UhOhPreventer extends Component {
    AlphabetLogger logger = new AlphabetLogger("OhUhPreventer");

    long lastMoveTime = 0;
    double lastEngery = 0;
    Point2D.Double lastLocation = new Point2D.Double(0, 0);

    public void execute() {
        if (alphabet.myLocation.distance(lastLocation) == 0 && alphabet.getTime() - lastMoveTime > Config.uhOhTriggerTime && lastEngery != alphabet.getEnergy() && alphabet.getOthers() >= 1) {
            lastMoveTime = alphabet.getTime();
            if (alphabet.movementMode == alphabet.MOVEMENT_SURFING) {
                alphabet.movementMode = alphabet.MOVEMENT_MELEE;
                alphabet.forceDisableAutoMovement = true;
                logger.warn("I'm not moving! Switching to melee movement");
                ((Themer) alphabet.componentCore.getComponent("Themer")).flashing = true;
            }
        } else if (alphabet.myLocation.distance(lastLocation) != 0) {
            lastMoveTime = alphabet.getTime();
            lastLocation = alphabet.myLocation;
            lastEngery   = alphabet.getEnergy();
        }
    }
}
