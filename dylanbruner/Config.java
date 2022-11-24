package dylanbruner;

public class Config {
    //Virtual Gun Config
    public static final int fireInterval = 25;
    
    //Painting
    public static final boolean drawTrackedBullets = true;

    //Uh-Oh Preventer
    public static final int uhOhTriggerTime = 100;

    //Radar
    public static final int MAX_SNAPSHOTS_PER_ENEMY = 4000; //May have to be lowered if we start getting turn skips


    //Game mode specific ============================================
    //[Melee]
    public static final int MELEE_MAX_RADAR_LOCK_TIME = 50;
}
