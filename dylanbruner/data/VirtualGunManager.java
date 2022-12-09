package dylanbruner.data;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Hashtable;

// import dylanbruner.gun.GuessFactorGun;
import dylanbruner.gun.HeadOnGun;
import dylanbruner.gun.LinearGun;
import dylanbruner.gun.PatternGunV2;
import dylanbruner.gun.PatternMatchGun;
import dylanbruner.util.AlphabetLogger;
import dylanbruner.util.Component;
import dylanbruner.util.MathUtils;
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
    public ArrayList<TrackedBullet> bullets = new ArrayList<TrackedBullet>();
    //                     Name,              Gun,     Shots hit
    public static Hashtable<String, Hashtable<Integer, Integer>> gunStats = new Hashtable<String, Hashtable<Integer, Integer>>();

    public void execute() {
        if (!((Radar) alphabet.componentCore.getComponent("Radar")).target.initialized) return;
        
        if (alphabet.getTime() % Config.fireInterval == 0){
            //Actually fire the bullets
            fireVirtualBullets();
        }

        //Check for collisions
        ArrayList<TrackedBullet> bulletsToRemove = new ArrayList<TrackedBullet>();
        for (TrackedBullet bullet : bullets){
            Point2D.Double bulletLocation = bullet.getLocation(alphabet);
            if (bulletLocation.distance(((Radar) alphabet.componentCore.getComponent("Radar")).target.location) < 20){
                //Update the target's stats
                Hashtable<Integer, Integer> targetStats = gunStats.get(((Radar) alphabet.componentCore.getComponent("Radar")).target.name);
                if (targetStats == null){
                    targetStats = new Hashtable<Integer, Integer>();
                    targetStats.put(alphabet.GUN_GUESS_FACTOR, 0);
                    targetStats.put(alphabet.GUN_LINEAR, 0);
                    targetStats.put(alphabet.GUN_PATTERN, 0);
                    targetStats.put(alphabet.GUN_HEAD_ON, 0);
                    targetStats.put(alphabet.GUN_PATTERN_V2, 0);
                    gunStats.put(((Radar) alphabet.componentCore.getComponent("Radar")).target.name, targetStats);
                }
                targetStats.put(bullet.parentGun, targetStats.get(bullet.parentGun) + 1); //Increment the gun's hit count
            }
        }

        bullets.removeAll(bulletsToRemove);

        //Use gun stats to determine which gun to use for alphabet.radar.target.name
        Hashtable<Integer, Integer> targetStats = gunStats.get(((Radar) alphabet.componentCore.getComponent("Radar")).target.name);
        if (targetStats != null){
            int guessFactorGunHits = targetStats.get(alphabet.GUN_GUESS_FACTOR);
            int linearGunHits      = targetStats.get(alphabet.GUN_LINEAR);
            int patternGunHits     = targetStats.get(alphabet.GUN_PATTERN);
            int headOnGunHits      = targetStats.get(alphabet.GUN_HEAD_ON);
            int patterGunV2Hits    = targetStats.get(alphabet.GUN_PATTERN_V2);

            //Select the gun with the most hits
            if (!Config.DISABLE_AUTO_GUN){
                if (guessFactorGunHits > linearGunHits && guessFactorGunHits > patternGunHits && guessFactorGunHits > headOnGunHits && guessFactorGunHits > patterGunV2Hits){
                    alphabet.selectedGun = alphabet.GUN_GUESS_FACTOR;
                } else if (linearGunHits > patternGunHits && linearGunHits > headOnGunHits && linearGunHits > patterGunV2Hits){
                    alphabet.selectedGun = alphabet.GUN_LINEAR;
                } else if (patternGunHits > headOnGunHits && patternGunHits > patterGunV2Hits){
                    alphabet.selectedGun = alphabet.GUN_PATTERN;
                } else if (headOnGunHits > patterGunV2Hits){
                    alphabet.selectedGun = alphabet.GUN_HEAD_ON;
                } else {
                    alphabet.selectedGun = alphabet.GUN_PATTERN_V2;
                }
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

        if (!((Radar) alphabet.componentCore.getComponent("Radar")).target.initialized) return;
        double bulletPower = alphabet.getFirePower();
        double absBearing  = alphabet.getHeadingRadians() + ((Radar) alphabet.componentCore.getComponent("Radar")).target.bearingRadians;

        TrackedBullet guessFactorBullet = new TrackedBullet();
        guessFactorBullet.fireTime      = alphabet.getTime();
        guessFactorBullet.fireLocation  = alphabet.myLocation;
        guessFactorBullet.power         = bulletPower;
        guessFactorBullet.parentGun     = alphabet.GUN_GUESS_FACTOR;

        TrackedBullet linearBullet = guessFactorBullet.copy();
        TrackedBullet patternGun   = guessFactorBullet.copy();
        TrackedBullet headOnGun    = guessFactorBullet.copy();
        TrackedBullet patternGunV2 = guessFactorBullet.copy();

        linearBullet.parentGun = alphabet.GUN_LINEAR;
        patternGun.parentGun   = alphabet.GUN_PATTERN;
        headOnGun.parentGun    = alphabet.GUN_HEAD_ON;
        patternGunV2.parentGun = alphabet.GUN_PATTERN_V2;

        //Do the bullet calculations (They are relative at first)
        // guessFactorBullet.absFireRadians = ((GuessFactorGun) alphabet.componentCore.getComponent("GuessFactorGun")).doGuessFactorGun(absBearing, bulletPower) + absBearing;
        linearBullet.absFireRadians      = ((LinearGun) alphabet.componentCore.getComponent("LinearGun")).doLinearGun(((Radar) alphabet.componentCore.getComponent("Radar")).target.lastScan, bulletPower) + absBearing;
        
        Point2D.Double location = ((PatternMatchGun) alphabet.componentCore.getComponent("PatternMatchGun")).doPatternGun(((Radar) alphabet.componentCore.getComponent("Radar")).target.lastScan, bulletPower);
        patternGun.absFireRadians = MathUtils.absoluteBearing(alphabet.myLocation, location);
        
        Point2D.Double headOnLocation = ((HeadOnGun) alphabet.componentCore.getComponent("HeadOnGun")).doHeadOnGun(((Radar) alphabet.componentCore.getComponent("Radar")).target, bulletPower);
        headOnGun.absFireRadians = MathUtils.absoluteBearing(alphabet.myLocation, headOnLocation);
        
        patternGunV2.absFireRadians = ((PatternGunV2) alphabet.componentCore.getComponent("PatternGunV2")).doPatternGunV2(((Radar) alphabet.componentCore.getComponent("Radar")).target.lastScan, bulletPower);

        //Add the bullets to the list
        bullets.add(patternGun); bullets.add(linearBullet); bullets.add(guessFactorBullet); 
        bullets.add(headOnGun);  bullets.add(patternGunV2);
    }

    //Events
    public void onRobotDeath(RobotDeathEvent e){
        Hashtable<Integer, Integer> targetStats = gunStats.get(e.getName());
        if (targetStats != null){
            //Log gun stats like this: LinearGun: 10, GuessFactorGun: 5, PatternGun: 3, HeadOnGun: 2
            if (Config.LOG_STATS_ON_KILL){
                logger.log("LinearGun: " + targetStats.get(alphabet.GUN_LINEAR) + ", GuessFactorGun: " + targetStats.get(alphabet.GUN_GUESS_FACTOR) + ", PatternGun: " + targetStats.get(alphabet.GUN_PATTERN) + ", HeadOnGun: " + targetStats.get(alphabet.GUN_HEAD_ON)+ ", PatternGunV2: " + targetStats.get(alphabet.GUN_PATTERN_V2));
            }
        }
    }
}