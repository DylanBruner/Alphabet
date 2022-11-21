package dylanbruner;

public class Config {
    //Virtual Gun Config
    public static final int fireInterval = 25;
    
    //Painting
    public static final boolean drawTrackedBullets = true;

    //Anti-Grav
    public static final double FORCE_CENTER = 0.0005;
    public static final double FORCE_ENEMY  = 1.2;

    //Game mode specific ============================================
    //[Melee]
    public static final int MELEE_MAX_RADAR_LOCK_TIME        = 100;

    //anti-grav values
    public static final double MELEE_TARGETING_DISTANCE_BASE = 1000; //The base score to use for targeting (higher = more likely to target)
    public static final double MELEE_TARGETING_ENERGY_BASE   = 1000; //The base score to use for targeting (higher = more likely to target)
    public static final double MELEE_IDLE                    = 1000; //The score we give to an enemy when they are idle
}
