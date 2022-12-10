package dylanbruner.gun;

import dylanbruner.data.Enemy;
import dylanbruner.data.Radar;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/*
 * This gun will just use pattern matching to find how to shoot the target
 * but for picking the target it will use the shadowgun which is optimized for melee
 * For this reason we shouldn't actually need the onScannedRobot event
*/

public class MeleeGun extends Component {
    AlphabetLogger logger = new AlphabetLogger("MeleeGun");
    Enemy fireAt = null;
    long lockSetTime = -1;

    public void onScannedRobot(ScannedRobotEvent e) {
        if (alphabet.isTeammate(e.getName())) return;

        if (fireAt == null) return;
        if (e.getName().equals(fireAt.name)){
            fireGun();
        } 
    }
    
    public void fireGun(){
        if (alphabet.getGunTurnRemaining() != 0) return;
        PatternGunV2 patternV2Gun = (PatternGunV2) alphabet.componentCore.getComponent("PatternGunV2");
    
        double bulletPower = getBulletPower(fireAt);
        double absBearing  = patternV2Gun.doPatternGunV2(fireAt.lastScan, bulletPower);

        //Calculate our current speed into the absBearing
    

        if (absBearing != -1){
            alphabet.setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - alphabet.getGunHeadingRadians()));
            alphabet.setFire(bulletPower);
        }
    }

    public void execute(){
        ShadowGun shadowGun = (ShadowGun) alphabet.componentCore.getComponent("ShadowGun");

        //Set target
        fireAt = shadowGun.getBestTarget();
        if (fireAt != null) {
            if (((Radar) alphabet.componentCore.getComponent("Radar")).manualRadarLockName != fireAt.name){
                ((Radar) alphabet.componentCore.getComponent("Radar")).setRadarLock(fireAt.name);
                lockSetTime = alphabet.getTime();
            }
        }
    }

    //We use a custom bullet power function because the one from alphabet uses alphabet.radar.target
    public double getBulletPower(Enemy target){
        double distanceToTarget = alphabet.myLocation.distance(target.location);
        double bulletPower = Math.min(Math.min(alphabet.getEnergy()/6d, 1300d/distanceToTarget), target.energy/3d);
        return bulletPower;
    }
}
