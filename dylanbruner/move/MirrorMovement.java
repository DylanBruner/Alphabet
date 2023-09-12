package dylanbruner.move;

import dylanbruner.Alphabet;
import dylanbruner.data.Enemy;
import dylanbruner.data.Radar;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.ComponentCore;
import robocode.util.Utils;

/*
 * Not sure if im even going to implement this it's kinda just a reminder
 * of something I might wanna do
 * 
 * https://robowiki.net/wiki/Mirror_Movement
*/

public class MirrorMovement extends Component {
    AlphabetLogger logger = new AlphabetLogger("MirrorMovement");

    private static final int MIRROR_RELATIVE = 0; // Just copy the enemy's movement
    private static final int MIRROR_PERPENDICULAR = 1; // Should circle around the enemy
    private static final int COORDINATE_MOVEMENT_VERTICAL = 2; // Mirror location on the y axis
    private static final int COORDINATE_MOVEMENT_HORIZONTAL = 3; // Mirror location on the x axis
    private static final int COORDINATE_MOVEMENT_CENTER = 4; // Mirror location on the center of the field
    private int mirrorMode = MIRROR_RELATIVE;

    public void setupConditionals(ComponentCore componentCore) {
        componentCore.setEventConditional("MirrorMovement", componentCore.ON_EXECUTE, (Alphabet alphabet) -> {
            return alphabet.useMirorMovement;
        });
    }

    public void execute() {
        Enemy target = ((Radar) alphabet.componentCore.getComponent("Radar")).target;
        if (target == null || !target.initialized)
            return;

        if (mirrorMode == MIRROR_RELATIVE) {
            alphabet.setTurnRight(Utils.normalRelativeAngleDegrees(target.heading - alphabet.getHeading()));
            alphabet.setMaxVelocity(Math.abs(target.velocity));
            alphabet.setAhead(Double.POSITIVE_INFINITY * target.velocity);
        } else if (mirrorMode == COORDINATE_MOVEMENT_CENTER || mirrorMode == COORDINATE_MOVEMENT_HORIZONTAL
                || mirrorMode == COORDINATE_MOVEMENT_VERTICAL) {
            double enemyX = ((Radar) alphabet.componentCore.getComponent("Radar")).target.location.x;
            double enemyY = ((Radar) alphabet.componentCore.getComponent("Radar")).target.location.y;

            if (mirrorMode == COORDINATE_MOVEMENT_CENTER) {
                enemyX -= alphabet.getBattleFieldWidth();
                enemyY -= alphabet.getBattleFieldHeight();
            } else if (mirrorMode == COORDINATE_MOVEMENT_HORIZONTAL) {
                enemyX -= alphabet.getBattleFieldWidth();
            } else if (mirrorMode == COORDINATE_MOVEMENT_VERTICAL) {
                enemyY -= alphabet.getBattleFieldHeight();
            }

            goTo(enemyX, enemyY);
        } else if (mirrorMode == MIRROR_PERPENDICULAR) {
            alphabet.setTurnRightRadians(Math.cos(target.lastScan.getBearingRadians()));
            alphabet.setAhead(4 * target.velocity);
        }
    }

    // Copied exactly from https://robowiki.net/wiki/Mirror_Movement
    private void goTo(double x, double y) {
        /* Transform our coordinates into a vector */
        x -= alphabet.myLocation.x;
        y -= alphabet.myLocation.y;

        /* Calculate the angle to the target position */
        double angleToTarget = Math.atan2(x, y);

        /* Calculate the turn required get there */
        double targetAngle = Utils.normalRelativeAngle(angleToTarget - alphabet.getHeadingRadians());

        /*
         * The Java Hypot method is a quick way of getting the length
         * of a vector. Which in this case is also the distance between
         * our robot and the target location.
         */
        double distance = Math.hypot(x, y);

        /* This is a simple method of performing set front as back */
        double turnAngle = Math.atan(Math.tan(targetAngle));
        alphabet.setTurnRightRadians(turnAngle);
        if (targetAngle == turnAngle) {
            alphabet.setAhead(distance);
        } else {
            alphabet.setBack(distance);
        }
    }
}
