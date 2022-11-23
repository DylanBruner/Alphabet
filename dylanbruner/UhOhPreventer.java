package dylanbruner;

/*
 * This does exactly what the name says. It prevents the robot from
 * doing a uh-oh. Like for some reason just sitting still and not
 * moving. (This really only affects wave surfing) 
 * 
*/

import java.awt.geom.*;

public class UhOhPreventer {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("OhUhPreventer");

    long lastMoveTime = 0;
    double lastEngery = 0;
    Point2D.Double lastLocation = new Point2D.Double(0, 0);

    public void init(Alphabet robot){
        alphabet = robot;
    }

    public void execute() {
        if (alphabet.myLocation.distance(lastLocation) == 0 && alphabet.getTime() - lastMoveTime > Config.uhOhTriggerTime && lastEngery != alphabet.getEnergy()) {
            lastMoveTime = alphabet.getTime();
            if (alphabet.movementMode == alphabet.MOVEMENT_SURFING) {
                alphabet.movementMode = alphabet.MOVEMENT_MELEE;
                alphabet.forceDisableAutoMovement = true;
                logger.warn("I'm not moving! Switching to melee movement");
                alphabet.themer.flashing = true;//Show that we are in uh-oh mode
            }
        } else if (alphabet.myLocation.distance(lastLocation) != 0) {
            lastMoveTime = alphabet.getTime();
            lastLocation = alphabet.myLocation;
            lastEngery   = alphabet.getEnergy();
        }
    }
}
