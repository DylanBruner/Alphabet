package dylanbruner.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.function.Function;

import dylanbruner.Alphabet;
import dylanbruner.data.Statistics;

import java.awt.event.MouseEvent;
import robocode.*;

public class ComponentCore {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("ComponentCore");

    ArrayList<Component> components = new ArrayList<Component>();
    //Make a hashtable like this <String(componentName), <String(eventName), Function>>
    Hashtable<String, Hashtable<String, Function<Alphabet, Boolean>>> executionConditionals = new Hashtable<String, Hashtable<String, Function<Alphabet, Boolean>>>();
    Hashtable<String, Boolean> disabled = new Hashtable<String, Boolean>();

    public Hashtable<String, Component> componentLookup = new Hashtable<String, Component>();

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
        Component component = componentLookup.get(name);
        if (component == null){logger.error("Component " + name + " does not exist");}
        return component;
    }

    public void setComponentState(String name, boolean state) {
        disabled.put(name, !state);
    }

    public void setComponentStateByTag(String tag, boolean state) {
        for (Component component : components) {
            if (component.componentTag != null && component.componentTag.equals(tag)){
                setComponentState(component.getClass().getSimpleName(), state);
            }
        }
    }

    public void unloadAll(){
        components.clear();
        componentLookup.clear();
        executionConditionals.clear();
        disabled.clear();
    }

    public void setEventConditional(String componentName, String eventName, Function<Alphabet, Boolean> conditional){
        if (!executionConditionals.containsKey(componentName)){
            executionConditionals.put(componentName, new Hashtable<String, Function<Alphabet, Boolean>>());
        }
        executionConditionals.get(componentName).put(eventName, conditional);
    }

    public void setEventConditional(String componentName, String[] eventNames, Function<Alphabet, Boolean> conditional){
        for (String eventName : eventNames){
            setEventConditional(componentName, eventName, conditional);
        }
    }

    public void registerComponent(Component component) {
        components.add(component);
        componentLookup.put(component.getClass().getSimpleName(), component);
        try {
            component.init(alphabet);
            component.setupConditionals(this);
        } catch (Exception e) {
            logger.error("initializing component failed: " + component.getClass().getSimpleName());
            e.printStackTrace();
            logUncaughtException();
        }
    }

    public void registerComponents(Component[] components) {
        for (Component component : components) {
            registerComponent(component);
        }
    }

    private boolean shouldExecute(String componentName, String eventName){
        if (disabled.containsKey(componentName) && disabled.get(componentName) == true){return false;}
        if (executionConditionals.containsKey(componentName)){
            if (executionConditionals.get(componentName).containsKey(eventName)){
                return executionConditionals.get(componentName).get(eventName).apply(alphabet);
            }
        }
        return true;
    }

    private void callComponents(robocode.Event e) {
        //Yeah, yeah this is some ugly code, but i will never have to look at it again so it's fine!
        //Check the execution conditionals and see if the component should be executed
        //Make a copy of components so that if a component is unloaded it doesn't cause a concurrent modification exception
        try {
            ArrayList<Component> componentsCopy = new ArrayList<Component>(components);
            for (Component component : componentsCopy) {
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
                    } else if (e instanceof CustomEvent && shouldExecute(componentName, ON_CUSTOM_EVENT)){
                        component.onCustomEvent((CustomEvent) e);
                    }
                } catch (Exception ex) {
                    logger.error("calling component failed: " + component.getClass().getSimpleName());
                    ex.printStackTrace();
                    logUncaughtException();
                }
            }
        } catch (ConcurrentModificationException ex){
            logger.error("Concurrent modification exception while calling components!");
            // ex.printStackTrace();
            // logUncaughtException();
        }
    }

    private void logUncaughtException() {Statistics.counter_uncaughtExceptions++;}
    
    public void execute(){ 
        for (Component component : components) {
            if (!shouldExecute(component.getClass().getSimpleName(), ON_EXECUTE)) continue;
            try {
                component.execute();
            } catch (Exception e) {
                logger.error("executing component failed: " + component.getClass().getSimpleName());
                e.printStackTrace();
                logUncaughtException();
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
                logUncaughtException();
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
                logUncaughtException();
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
    public void onCustomEvent(CustomEvent e)              {callComponents(e);}
}
