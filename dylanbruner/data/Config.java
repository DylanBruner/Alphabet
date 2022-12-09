package dylanbruner.data;

public class Config {
    //Radar
    public static final int MAX_SNAPSHOTS_PER_ENEMY         = 4000; //May have to be lowered if we start getting turn skips
    public static final boolean CLR_MAN_RADAR_LOCK_ON_SWTCH = true; //When we switch to 1v1 clear any manual radar lock
}