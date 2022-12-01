package dylanbruner.data;
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
    public static final boolean HIGHLIGHT_SELF         = false; //Draw a circle around the robot

    //Fun
    public static final boolean USE_MIRROR_MODE = true;//If we are gauranteed to win, we can use mirror mode to make the fight more interesting
                                                       //Currently this config value does nothing, it will be adjusted when I figure out how scoring
                                                       //TODO: Mirror Movement Toggling
    //Uh-Oh Preventer
    public static final int UHOH_TRIGGER_TIME = 100;

    //Radar
    public static final int MAX_SNAPSHOTS_PER_ENEMY         = 4000; //May have to be lowered if we start getting turn skips
    public static final boolean CLR_MAN_RADAR_LOCK_ON_SWTCH = true; //When we switch to 1v1 clear any manual radar lock

    //Funny Stuff
    //When in 1v1 we will try to only dodge robots in this list and not shoot them until were below n health or they are below n health 
    public static final String[] DODGE_ME = {"travis", "Sample"};
    public static final int ABORT_DODGE_HEALTH = 50;
    public static final int ABORT_DODGE_ENEMY_HEALTH = 10;
    public static final int ABORT_AFTER_ROUNDS = 1;
    public static final long ABORT_AFTER_TIME = 5000;//Idk what units this is in MS maybe

    //Game mode specific ============================================
    //[Melee]
    public static final int MELEE_MAX_RADAR_LOCK_TIME = 15;//Warning this number has a massive impact the robot's physical performance

    //Don't worry about this :)
    public static final String SUPER_SECRET_STRING = "TmljZSB0cnk6IGQ3MGNmN2YyZDg1MGExZmI2MDE1ODE3OTQzNjM1NTBiNGQxZjRkOTM5M2I4NDllZWRkNWM1MzYzODljODgzZDM=";
}
