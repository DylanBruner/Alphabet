package dylanbruner;

import java.util.Hashtable;
//import java.awt.geom.*;

import robocode.*;
import robocode.util.Utils;

public class Radar {
    //Component stuff
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Radar");

    //Radar stuff
    public Enemy target;
    public Hashtable<String, Enemy> enemies = new Hashtable<String, Enemy>();
    
    private boolean disableManagement = false;
    public boolean radarLocked       = false;
    public boolean radarLockCooldown = false;
    public long radarLockStarted     = 0;

    public void init(Alphabet robot){
        alphabet = robot;

        target = new Enemy();
    }

    public void execute(){
        if (disableManagement) return;
        //If we are doing melee limit the max time we can lock onto an enemy
        if ((radarLocked && alphabet.movementMode == alphabet.MOVEMENT_MELEE) && alphabet.getTime() - radarLockStarted > Config.MELEE_MAX_RADAR_LOCK_TIME){
            radarLocked = false;
            radarLockCooldown = true;
            alphabet.setTurnRadarRight(360);
        }

        if (!radarLocked && !radarLockCooldown){
            alphabet.setTurnRadarRight(Double.POSITIVE_INFINITY);
        } else if (radarLockCooldown && alphabet.getRadarTurnRemaining() == 0){
            radarLockCooldown = false;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (disableManagement) return;
        double absBearing = e.getBearingRadians() + alphabet.getHeadingRadians();

        if (!radarLockCooldown && !radarLocked){
            radarLocked = true;
            radarLockStarted = alphabet.getTime();
        }

        if (radarLocked){
            alphabet.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - alphabet.getRadarHeadingRadians()) * 2);
        }

        //Update the enemy in our database
        if (enemies.containsKey(e.getName())){
            Enemy newEnemy = enemies.get(e.getName());
            newEnemy.update(e, alphabet);
        } else {
            Enemy newEnemy = new Enemy();
            newEnemy.populateData(e, alphabet.myLocation, alphabet);
            enemies.put(e.getName(), newEnemy);
        }

        enemies.get(e.getName()).snapshots.add(new EnemySnapshot(e, alphabet.myLocation));
        //Log amount of snapshots
        //logger.log("Enemy " + e.getName() + " has " + enemies.get(e.getName()).snapshots.size() + " snapshots");

        // Deciding which enemy to target
        if (alphabet.movementMode == alphabet.MOVEMENT_SURFING){
            //It's a easy choice, just target most recently scanned enemy
            target = enemies.get(e.getName());
        } else if (alphabet.movementMode == alphabet.MOVEMENT_MELEE && !radarLocked){
            target = getOptimalMeleeEnemy();
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (enemies.containsKey(e.getName())){
            Enemy enemy = enemies.get(e.getName());
            if (!enemy.initialized) return; 
            enemies.get(e.getName()).alive = false;
        }

        if (target.name == null) return;
        if (target.name.equals(e.getName())){
            radarLocked = false;
        }
    }

    public void disableRadarManagement(){disableManagement = true; logger.warn("Radar management disabled");}
    public void enableRadarManagement(){disableManagement = false; logger.warn("Radar management enabled");}

    public Enemy getOptimalMeleeEnemy(){
        Hashtable<String, Double> enemyScores = new Hashtable<String, Double>();

        //Scores will now be based on the following:
        // 1. Distance to enemy (less is better)
        // 2. Amount of energy the enemy has (less is better)
        // 3. Virtual leaderboard placement (higher is better) we want to take out the strongest bots first
        // 4. The last time they shot us (higher is better) we want to take out the bots that are shooting us first
        
        //Other info on how this works
        //If we dont have line of sight to the enemy, we will take the next best enemy 
    }
}
