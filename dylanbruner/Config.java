package dylanbruner;

public class Config {
    //Virtual Gun Config
    public static final int fireInterval         = 25;
    public static final boolean DISABLE_AUTO_GUN = true;
    
    //Painting
    public static final boolean DRAW_VIRTUAL_BULLETS   = true;
    public static final boolean DRAW_BOX_AROUND_TARGET = true;
    public static final boolean DRAW_MOUSE_COORDS      = true;
    public static final boolean DRAW_SHADOW_GUN_DATA   = true; //Draw targeting data for the shadow gun

    //Uh-Oh Preventer
    public static final int uhOhTriggerTime = 100;

    //Radar
    public static final int MAX_SNAPSHOTS_PER_ENEMY = 4000; //May have to be lowered if we start getting turn skips


    //Game mode specific ============================================
    //[Melee]
    public static final int MELEE_MAX_RADAR_LOCK_TIME = 15;
}
