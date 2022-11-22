package dylanbruner;

import java.awt.geom.*;
import java.util.ArrayList;

/*
 * Used to virtually benchmark different guns and see which one performs the best
 * Note this won't really help when robots have adaptive movement because they wont
 * be reacting to the virtual bullets. But i highly doubt anyone will be using adaptive
 * movement in the competition. (I hope)
*/

public class VirtualGunManager {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("VirtualGunManager");

    //Virtual gun variables 'n stuff
    ArrayList<TrackedBullet> bullets = new ArrayList<TrackedBullet>();

    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    public void execute() {
        if (!alphabet.radar.target.initialized) return;
        
        if (alphabet.getTime() % Config.fireInterval == 0){
            //Actually fire the bullets
            fireVirtualBullets();
        }

        //Check for collisions
        ArrayList<TrackedBullet> bulletsToRemove = new ArrayList<TrackedBullet>();
        for (TrackedBullet bullet : bullets){
            Point2D.Double bulletLocation = bullet.getLocation(alphabet);
            if (bulletLocation.distance(alphabet.radar.target.location) < 20){
                //Update the target's stats
                if (bullet.parentGun == alphabet.GUN_GUESS_FACTOR){
                    alphabet.radar.target.tracker_guessFactorGun++;
                } else if (bullet.parentGun == alphabet.GUN_LINEAR){
                    alphabet.radar.target.tracker_linearGun++;
                }

                //logger.log("Linear: " + alphabet.radar.target.tracker_linearGun+ " Guess factor:" + alphabet.radar.target.tracker_guessFactorGun);
            }
        }

        bullets.removeAll(bulletsToRemove);

        // if (alphabet.radar.target.tracker_linearGun < alphabet.radar.target.tracker_guessFactorGun && alphabet.selectedGun != alphabet.GUN_GUESS_FACTOR){
        //     alphabet.selectedGun = alphabet.GUN_GUESS_FACTOR;
        // } else if (alphabet.selectedGun != alphabet.GUN_LINEAR) {
        //     alphabet.selectedGun = alphabet.GUN_LINEAR;
        // }
    }

    public void fireVirtualBullets(){
        ArrayList<TrackedBullet> toRemove = new ArrayList<TrackedBullet>();
        for (TrackedBullet bullet : bullets){
            if (!MathUtils.fieldBox.contains(bullet.getLocation(alphabet))){
                toRemove.add(bullet);
                continue;
            } 
        }
        bullets.removeAll(toRemove);

        if (!alphabet.radar.target.initialized) return;
        double bulletPower = alphabet.getFirePower();
        double absBearing  = alphabet.getHeadingRadians() + alphabet.radar.target.bearingRadians;

        TrackedBullet guessFactorBullet = new TrackedBullet();
        guessFactorBullet.fireTime      = alphabet.getTime();
        guessFactorBullet.fireLocation  = alphabet.myLocation;
        guessFactorBullet.power         = bulletPower;
        guessFactorBullet.parentGun     = alphabet.GUN_GUESS_FACTOR;

        TrackedBullet linearBullet = guessFactorBullet.copy();
        linearBullet.parentGun = alphabet.GUN_LINEAR;

        //Do the bullet calculations (They are relative at first)
        guessFactorBullet.absFireRadians = alphabet.guessFactorGun.doGuessFactorGun(absBearing, bulletPower) + absBearing;
        linearBullet.absFireRadians      = alphabet.linearGun.doLinearGun(alphabet.radar.target.lastScan, bulletPower) + absBearing;

        //Add the bullets to the list
        bullets.add(guessFactorBullet); bullets.add(linearBullet);
    }
}