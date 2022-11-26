package dylanbruner;

import java.awt.geom.*;
import java.awt.event.MouseEvent;

public class Painting extends Component {
    AlphabetLogger logger = new AlphabetLogger("Painting");

    //Debug values
    Point2D.Double mouseLocation = new Point2D.Double(0, 0);
    
    public void onPaint(java.awt.Graphics2D g){
        if (Config.HIGHLIGHT_SELF){
            //Draw a transparent green circle around the robot
            g.setColor(new java.awt.Color(0, 255, 0, 75));
            //65 pixels is the radius of the robot
            g.fillOval((int)(alphabet.myLocation.getX() - 65), (int)(alphabet.myLocation.getY() - 65), 130, 130);
        }

        //Draw the tracked virtual bullets
        if (Config.DRAW_VIRTUAL_BULLETS){
            for (TrackedBullet bullet : ((VirtualGunManager) alphabet.componentCore.getComponent("VirtualGunManager")).bullets){
                Point2D.Double bulletLocation = bullet.getLocation(alphabet);
                g.setColor(java.awt.Color.RED);
                g.drawOval((int)bulletLocation.x - 2, (int)bulletLocation.y - 2, 40, 40);
            }
        }

        //Draw a box around target
        if (Config.DRAW_BOX_AROUND_TARGET){
            if (((Radar) alphabet.componentCore.getComponent("Radar")).target.initialized){
                g.setColor(java.awt.Color.GREEN);
                g.drawRect((int)((Radar) alphabet.componentCore.getComponent("Radar")).target.location.x-40, 
                           (int)((Radar) alphabet.componentCore.getComponent("Radar")).target.location.y-40, 40, 40);
            }
        }

        //Draw text in the top left that says mouseLocation
        if (Config.DRAW_VIRTUAL_BULLETS){
            g.setColor(java.awt.Color.WHITE);
            g.drawString("Mouse location: " + mouseLocation, 10, 10);
        }

        if (Config.DRAW_SHADOW_GUN_DATA){
            ShadowGun shadowGun = (ShadowGun) alphabet.componentCore.getComponent("ShadowGun");

            for (String robotName : shadowGun.computedWeights.keySet()){
                int placement = shadowGun.getRobotPlacement(robotName);
                if (placement >= 0){
                    //Draw a box around the robot, and write it's placement above it
                    if (placement == 0){
                        g.setColor(java.awt.Color.ORANGE);
                    } else {
                        g.setColor(java.awt.Color.WHITE);
                    }
                    
                    g.drawRect((int)((Radar) alphabet.componentCore.getComponent("Radar")).enemies.get(robotName).location.x-40, 
                               (int)((Radar) alphabet.componentCore.getComponent("Radar")).enemies.get(robotName).location.y-40, 40, 40);
                    g.drawString("" + placement, (int)((Radar) alphabet.componentCore.getComponent("Radar")).enemies.get(robotName).location.x-40, 
                                                 (int)((Radar) alphabet.componentCore.getComponent("Radar")).enemies.get(robotName).location.y-40);
                } else {
                    logger.warn("Could not find placement for robot " + robotName);
                }
            }
        }

        if (Config.DRAW_LINE_OF_SIGHT){
            g.setColor(java.awt.Color.WHITE);
            for (Enemy enemy : ((Radar) alphabet.componentCore.getComponent("Radar")).enemies.values()){
                if (((Radar) alphabet.componentCore.getComponent("Radar")).hasLineOfSight(enemy)){
                    g.drawLine((int)alphabet.myLocation.x, (int)alphabet.myLocation.y, (int)enemy.location.x, (int)enemy.location.y);
                }
            }
        }
    }

    public void onMouseMoved(MouseEvent e){
        mouseLocation = new Point2D.Double(e.getX(), e.getY());
    }
}
