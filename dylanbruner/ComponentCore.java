package dylanbruner;

import java.util.ArrayList;
import java.util.Hashtable;
import java.awt.event.MouseEvent;
import robocode.*;

public class ComponentCore {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("ComponentCore");

    ArrayList<Component> components = new ArrayList<Component>();
    Hashtable<String, Component> componentLookup = new Hashtable<String, Component>();

    public ComponentCore(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    public Component getComponent(String name) {
        return componentLookup.get(name);
    }

    public void registerComponent(Component component) {
        components.add(component);
        componentLookup.put(component.getClass().getSimpleName(), component);
        try {
            component.init(alphabet);
        } catch (Exception e) {
            logger.error("initializing component failed: " + component.getClass().getSimpleName());
            e.printStackTrace();
        }
    }

    public void callComponents(robocode.Event e) {
        //Yeah, yeah this is some ugly code, but i will never have to look at it again so it's fine!
        for (Component component : components) {
            try {
                if (e instanceof ScannedRobotEvent) {
                    component.onScannedRobot((ScannedRobotEvent) e);
                } else if (e instanceof HitByBulletEvent) {
                    component.onHitByBullet((HitByBulletEvent) e);
                } else if (e instanceof BulletHitEvent) {
                    component.onBulletHit((BulletHitEvent) e);
                } else if (e instanceof BulletMissedEvent) {
                    component.onBulletMissed((BulletMissedEvent) e);
                } else if (e instanceof BulletHitBulletEvent) {
                    component.onBulletHitBullet((BulletHitBulletEvent) e);
                } else if (e instanceof HitRobotEvent) {
                    component.onHitRobot((HitRobotEvent) e);
                } else if (e instanceof RobotDeathEvent) {
                    component.onRobotDeath((RobotDeathEvent) e);
                } else if (e instanceof HitWallEvent) {
                    component.onHitWall((HitWallEvent) e);
                } else if (e instanceof DeathEvent) {
                    component.onDeath((DeathEvent) e);
                } else if (e instanceof WinEvent) {
                    component.onWin((WinEvent) e);
                } else if (e instanceof RoundEndedEvent) {
                    component.onRoundEnded((RoundEndedEvent) e);
                } else if (e instanceof BattleEndedEvent) {
                    component.onBattleEnded((BattleEndedEvent) e);
                } else if (e instanceof SkippedTurnEvent) {
                    component.onSkippedTurn((SkippedTurnEvent) e);
                } else if (e instanceof StatusEvent) {
                    component.onStatus((StatusEvent) e);
                } else {
                    logger.error("unknown event type: " + e.getClass().getSimpleName());
                }
            } catch (Exception ex) {
                logger.error("calling component failed: " + component.getClass().getSimpleName());
                ex.printStackTrace();
            }
        }
    }
    
    public void execute(){ 
        for (Component component : components) {
            try {
                component.execute();
            } catch (Exception e) {
                logger.error("executing component failed: " + component.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
    public void onPaint(java.awt.Graphics2D g) {
        for (Component component : components) {
            try {
                component.onPaint(g);
            } catch (Exception e) {
                logger.error("calling component failed: " + component.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
    public void onMouseMoved(MouseEvent e)                {
        for (Component component : components) {
            try {
                component.onMouseMoved(e);
            } catch (Exception ex) {
                logger.error("calling component failed: " + component.getClass().getSimpleName());
                ex.printStackTrace();
            }
        }
    }

    public void onScannedRobot(ScannedRobotEvent e)       {callComponents(e);}
    public void onHitByBullet(HitByBulletEvent e)         {callComponents(e);}
    public void onBulletHit(BulletHitEvent e)             {callComponents(e);}
    public void onBulletMissed(BulletMissedEvent e)       {callComponents(e);}
    public void onBulletHitBullet(BulletHitBulletEvent e) {callComponents(e);}
    public void onHitRobot(HitRobotEvent e)               {callComponents(e);}
    public void onRobotDeath(RobotDeathEvent e)           {callComponents(e);}
    public void onHitWall(HitWallEvent e)                 {callComponents(e);}
    public void onDeath(DeathEvent e)                     {callComponents(e);}
    public void onWin(WinEvent e)                         {callComponents(e);}
    public void onRoundEnded(RoundEndedEvent e)           {callComponents(e);}
    public void onBattleEnded(BattleEndedEvent e)         {callComponents(e);}
    public void onSkippedTurn(SkippedTurnEvent e)         {callComponents(e);}
    public void onStatus(StatusEvent e)                   {callComponents(e);}
}
