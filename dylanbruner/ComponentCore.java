package dylanbruner;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.function.Function;
import java.awt.event.MouseEvent;
import robocode.*;

public class ComponentCore {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("ComponentCore");

    ArrayList<Component> components = new ArrayList<Component>();
    //Make a hashtable like this <String(componentName), <String(eventName), Function>>
    Hashtable<String, Hashtable<String, Function<Alphabet, Boolean>>> executionConditionals = new Hashtable<String, Hashtable<String, Function<Alphabet, Boolean>>>();

    Hashtable<String, Component> componentLookup = new Hashtable<String, Component>();

    //Event names as string
    public final String ON_SCANNED_ROBOT = "onScannedRobot";
    public final String ON_HIT_BY_BULLET = "onHitByBullet";
    public final String ON_BULLET_HIT = "onBulletHit";
    public final String ON_BULLET_MISSED = "onBulletMissed";
    public final String ON_BULLET_HIT_BULLET = "onBulletHitBullet";
    public final String ON_HIT_ROBOT = "onHitRobot";
    public final String ON_ROBOT_DEATH = "onRobotDeath";
    public final String ON_HIT_WALL = "onHitWall";
    public final String ON_PAINT = "onPaint";
    public final String ON_MOUSE_MOVED = "onMouseMoved";
    public final String ON_DEATH = "onDeath";
    public final String ON_ROUND_ENDED = "onRoundEnded";
    public final String ON_WIN = "onWin";
    public final String ON_CUSTOM_EVENT = "onCustomEvent";
    public final String ON_BATTLE_ENDED = "onBattleEnded";
    public final String ON_SKIPPED_TURN = "onSkippedTurn";
    public final String ON_STATUS = "onStatus";

    //Other event names
    public final String ON_EXECUTE = "onExecute";
    
    public ComponentCore(Alphabet alphabet){
        this.alphabet = alphabet;
    }

    public Component getComponent(String name) {
        return componentLookup.get(name);
    }

    public void setEventConditional(String componentName, String eventName, Function<Alphabet, Boolean> conditional){
        if (!executionConditionals.containsKey(componentName)){
            executionConditionals.put(componentName, new Hashtable<String, Function<Alphabet, Boolean>>());
        }
        executionConditionals.get(componentName).put(eventName, conditional);
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

    public void registerComponents(Component[] components) {
        for (Component component : components) {
            registerComponent(component);
        }
    }

    public boolean shouldExecute(String componentName, String eventName){
        if (executionConditionals.containsKey(componentName)){
            if (executionConditionals.get(componentName).containsKey(eventName)){
                return executionConditionals.get(componentName).get(eventName).apply(alphabet);
            }
        }
        return true;
    }

    public void callComponents(robocode.Event e) {
        //Yeah, yeah this is some ugly code, but i will never have to look at it again so it's fine!
        //Check the execution conditionals and see if the component should be executed
        for (Component component : components) {
            String componentName = component.getClass().getSimpleName();
            try {
                if (e instanceof ScannedRobotEvent && shouldExecute(componentName, ON_SCANNED_ROBOT)) {
                    component.onScannedRobot((ScannedRobotEvent) e);
                } else if (e instanceof HitByBulletEvent && shouldExecute(componentName, ON_HIT_BY_BULLET)) {
                    component.onHitByBullet((HitByBulletEvent) e);
                } else if (e instanceof BulletHitEvent && shouldExecute(componentName, ON_BULLET_HIT)) {
                    component.onBulletHit((BulletHitEvent) e);
                } else if (e instanceof BulletMissedEvent && shouldExecute(componentName, ON_BULLET_MISSED)) {
                    component.onBulletMissed((BulletMissedEvent) e);
                } else if (e instanceof BulletHitBulletEvent && shouldExecute(componentName, ON_BULLET_HIT_BULLET)) {
                    component.onBulletHitBullet((BulletHitBulletEvent) e);
                } else if (e instanceof HitRobotEvent && shouldExecute(componentName, ON_HIT_ROBOT)) {
                    component.onHitRobot((HitRobotEvent) e);
                } else if (e instanceof RobotDeathEvent && shouldExecute(componentName, ON_ROBOT_DEATH)) {
                    component.onRobotDeath((RobotDeathEvent) e);
                } else if (e instanceof HitWallEvent && shouldExecute(componentName, ON_HIT_WALL)) {
                    component.onHitWall((HitWallEvent) e);
                } else if (e instanceof DeathEvent && shouldExecute(componentName, ON_DEATH)) {
                    component.onDeath((DeathEvent) e);
                } else if (e instanceof WinEvent && shouldExecute(componentName, ON_WIN)) {
                    component.onWin((WinEvent) e);
                } else if (e instanceof RoundEndedEvent && shouldExecute(componentName, ON_ROUND_ENDED)) {
                    component.onRoundEnded((RoundEndedEvent) e);
                } else if (e instanceof BattleEndedEvent && shouldExecute(componentName, ON_BATTLE_ENDED)) {
                    component.onBattleEnded((BattleEndedEvent) e);
                } else if (e instanceof SkippedTurnEvent && shouldExecute(componentName, ON_SKIPPED_TURN)) {
                    component.onSkippedTurn((SkippedTurnEvent) e);
                } else if (e instanceof StatusEvent && shouldExecute(componentName, ON_STATUS)) {
                    component.onStatus((StatusEvent) e);
                }
            } catch (Exception ex) {
                logger.error("calling component failed: " + component.getClass().getSimpleName());
                ex.printStackTrace();
            }
        }
    }
    
    public void execute(){ 
        for (Component component : components) {
            if (!shouldExecute(component.getClass().getSimpleName(), ON_EXECUTE)) continue;
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
            if (!shouldExecute(component.getClass().getSimpleName(), ON_PAINT)) continue;
            try {
                component.onPaint(g);
            } catch (Exception e) {
                logger.error("calling component failed: " + component.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
    public void onMouseMoved(MouseEvent e){
        for (Component component : components) {
            if (!shouldExecute(component.getClass().getSimpleName(), ON_MOUSE_MOVED)) continue;
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
