package dylanbruner;

import java.util.Hashtable;
//import java.awt.geom.*;

import robocode.*;
import robocode.util.Utils;

public class Radar extends Component {
    //Component stuff
    AlphabetLogger logger = new AlphabetLogger("Radar");

    //Radar stuff
    public Enemy target = new Enemy();
    public Hashtable<String, Enemy> enemies = new Hashtable<String, Enemy>();
    
    private boolean disableManagement = false;
    public boolean radarLocked       = false;
    public boolean radarLockCooldown = false;
    public long radarLockStarted     = 0;

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

        if (enemies.get(e.getName()).snapshots.size() > Config.MAX_SNAPSHOTS_PER_ENEMY) {
            enemies.get(e.getName()).snapshots.remove(0);
        }
        
        enemies.get(e.getName()).snapshots.add(new EnemySnapshot(e, alphabet.myLocation));
        //Log amount of snapshots
        //logger.log("Enemy " + e.getName() + " has " + enemies.get(e.getName()).snapshots.size() + " snapshots");

        // Deciding which enemy to target
        if (alphabet.movementMode == alphabet.MOVEMENT_SURFING){
            //It's a easy choice, just target most recently scanned enemy
            target = enemies.get(e.getName());
        } else if (alphabet.movementMode == alphabet.MOVEMENT_MELEE && !radarLocked){
            target = getOptimalMeleeTarget();
        }
    }

    public void onHitByBullet(HitByBulletEvent e){
        Enemy hitBy = enemies.get(e.getName());
        if (hitBy == null) return; //Wait till they get added to our database

        hitBy.lastHitTime = alphabet.getTime(); //Update the last hit time used for targeting
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

    public Enemy getOptimalMeleeTarget(){        
        //Scores will now be based on the following:
        // 1. Distance to enemy (less is better)
        // 2. Amount of energy the enemy has (less is better)
        // 3. Virtual leaderboard placement (higher is better) we want to take out the strongest bots first
        // 4. The last time they hit us (higher is better) we want to take out the bots that are shooting us first
        
        //Other info on how this works
        //If we dont have line of sight to the enemy, we will take the next best enemy
        if (target == null) return null;
        
        Hashtable<String, Double> enemyScores = new Hashtable<String, Double>();
        
        //Get the scores for each enemy
        for (Enemy enemy : alphabet.radar.enemies.values()){
            //logger.log("Getting optimal target...");
            if (!enemyScores.keySet().contains(enemy.name)){
                enemyScores.put(enemy.name, 0d);
            }

            double score = enemyScores.get(enemy.name); //I have no clue why this wouldn't be 0, there are no duplicats in radar.enemies
            //Start calculation (calculated seperate for debugging and readability)
            //                                            The higher this is the more it will affect the outcome
            double distanceWeight    = 1 / (enemy.distance / 1000);
            double energyWeight      = 1 / (enemy.energy   / 500);
            //                                                                         Prevents devide by 0 errors
            double lastHitTimeWeight = Math.min(0, Math.max(0.45, 1 / (750 / Math.max(1, enemy.lastHitTime)))); //Limit to 0 -> 0.45
            score += (distanceWeight + energyWeight + lastHitTimeWeight);

            enemyScores.put(enemy.name, score);
        }

        //Return the best enemy
        Enemy bestEnemy  = null;
        double bestScore = 0;

        for (String name : enemyScores.keySet()){
            double score = enemyScores.get(name);
            if (score > bestScore || bestEnemy == null){
                bestEnemy = enemies.get(name);
            }
        }

        return bestEnemy;
    }
}