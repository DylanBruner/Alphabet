package dylanbruner;

public class Config {
    //Virtual Gun Config
    public static final int fireInterval          = 25;
    public static final boolean DISABLE_AUTO_GUN  = true;
    public static final boolean LOG_STATS_ON_KILL = false;
    
    //Painting
    public static final boolean DRAW_VIRTUAL_BULLETS   = true;
    public static final boolean DRAW_BOX_AROUND_TARGET = true;
    public static final boolean DRAW_MOUSE_COORDS      = true;
    public static final boolean DRAW_SHADOW_GUN_DATA   = false; //Draw targeting data for the shadow gun
    public static final boolean DRAW_LINE_OF_SIGHT     = false; //Draw a line from the robot to any robots it can see

    //Fun
    public static final boolean USE_MIRROR_MODE = true;//If we are gauranteed to win, we can use mirror mode to make the fight more interesting

    //Uh-Oh Preventer
    public static final int uhOhTriggerTime = 100;

    //Radar
    public static final int MAX_SNAPSHOTS_PER_ENEMY         = 4000; //May have to be lowered if we start getting turn skips
    public static final boolean CLR_MAN_RADAR_LOCK_ON_SWTCH = true; //When we switch to 1v1 clear any manual radar lock

    //Game mode specific ============================================
    //[Melee]
    public static final int MELEE_MAX_RADAR_LOCK_TIME = 15;//Warning this number has a massive impact the robot's physical performance
}
