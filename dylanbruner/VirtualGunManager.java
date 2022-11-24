package dylanbruner;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Hashtable;

import robocode.RobotDeathEvent;

/*
 * Used to virtually benchmark different guns and see which one performs the best
 * Note this won't really help when robots have adaptive movement because they wont
 * be reacting to the virtual bullets. But i highly doubt anyone will be using adaptive
 * movement in the competition. (I hope)
*/

public class VirtualGunManager extends Component {
    AlphabetLogger logger = new AlphabetLogger("VirtualGunManager");

    //Virtual gun variables 'n stuff
    ArrayList<TrackedBullet> bullets = new ArrayList<TrackedBullet>();
    //                     Name,              Gun,     Shots hit
    public static Hashtable<String, Hashtable<Integer, Integer>> gunStats = new Hashtable<String, Hashtable<Integer, Integer>>();

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
                Hashtable<Integer, Integer> targetStats = gunStats.get(alphabet.radar.target.name);
                if (targetStats == null){
                    targetStats = new Hashtable<Integer, Integer>();
                    targetStats.put(alphabet.GUN_GUESS_FACTOR, 0);
                    targetStats.put(alphabet.GUN_LINEAR, 0);
                    targetStats.put(alphabet.GUN_PATTERN, 0);
                    targetStats.put(alphabet.GUN_HEAD_ON, 0);
                    gunStats.put(alphabet.radar.target.name, targetStats);
                }
                targetStats.put(bullet.parentGun, targetStats.get(bullet.parentGun) + 1); //Increment the gun's hit count
            }
        }

        bullets.removeAll(bulletsToRemove);

        //Use gun stats to determine which gun to use for alphabet.radar.target.name
        Hashtable<Integer, Integer> targetStats = gunStats.get(alphabet.radar.target.name);
        if (targetStats != null){
            int guessFactorGunHits = targetStats.get(alphabet.GUN_GUESS_FACTOR);
            int linearGunHits      = targetStats.get(alphabet.GUN_LINEAR);
            int patternGunHits     = targetStats.get(alphabet.GUN_PATTERN);
            int headOnGunHits      = targetStats.get(alphabet.GUN_HEAD_ON);

            //Select the gun with the most hits
            if (guessFactorGunHits > linearGunHits && guessFactorGunHits > patternGunHits && guessFactorGunHits > headOnGunHits){
                alphabet.selectedGun = alphabet.GUN_GUESS_FACTOR;
            } else if (linearGunHits > guessFactorGunHits && linearGunHits > patternGunHits && linearGunHits > headOnGunHits){
                alphabet.selectedGun = alphabet.GUN_LINEAR;
            } else if (patternGunHits > guessFactorGunHits && patternGunHits > linearGunHits && patternGunHits > headOnGunHits){
                alphabet.selectedGun = alphabet.GUN_PATTERN;
            } else if (headOnGunHits > guessFactorGunHits && headOnGunHits > linearGunHits && headOnGunHits > patternGunHits){
                alphabet.selectedGun = alphabet.GUN_HEAD_ON;
            } else {
                alphabet.selectedGun = alphabet.GUN_LINEAR;
            }
        }
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
        TrackedBullet patternGun   = guessFactorBullet.copy();
        TrackedBullet headOnGun    = guessFactorBullet.copy();
        linearBullet.parentGun = alphabet.GUN_LINEAR;
        patternGun.parentGun   = alphabet.GUN_PATTERN;
        headOnGun.parentGun    = alphabet.GUN_HEAD_ON;

        //Do the bullet calculations (They are relative at first)
        guessFactorBullet.absFireRadians = alphabet.guessFactorGun.doGuessFactorGun(absBearing, bulletPower) + absBearing;
        linearBullet.absFireRadians      = alphabet.linearGun.doLinearGun(alphabet.radar.target.lastScan, bulletPower) + absBearing;
        Point2D.Double location = alphabet.patternMatchGun.doPatternGun(alphabet.radar.target.lastScan, bulletPower);
        patternGun.absFireRadians = MathUtils.absoluteBearing(alphabet.myLocation, location);

        Point2D.Double headOnLocation = alphabet.headOnGun.doHeadOnGun(alphabet.radar.target, bulletPower);
        headOnGun.absFireRadians = MathUtils.absoluteBearing(alphabet.myLocation, headOnLocation);

        //Add the bullets to the list
        bullets.add(patternGun); bullets.add(linearBullet); bullets.add(guessFactorBullet); bullets.add(headOnGun);
    }

    //Events
    public void onRobotDeath(RobotDeathEvent e){
        Hashtable<Integer, Integer> targetStats = gunStats.get(e.getName());
        if (targetStats != null){
            //Log gun stats like this: LinearGun: 10, GuessFactorGun: 5, PatternGun: 3, HeadOnGun: 2
            System.out.println("LinearGun: " + targetStats.get(alphabet.GUN_LINEAR) + ", GuessFactorGun: " + targetStats.get(alphabet.GUN_GUESS_FACTOR) + ", PatternGun: " + targetStats.get(alphabet.GUN_PATTERN) + ", HeadOnGun: " + targetStats.get(alphabet.GUN_HEAD_ON));
        }
    }
}