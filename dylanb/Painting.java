package dylanb;

import java.awt.geom.*;
import java.awt.event.MouseEvent;


public class Painting {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Painting");

    //Debug values
    Point2D.Double mouseLocation = new Point2D.Double(0, 0);
    
    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
        logger.log("Painting initialized");
    }

    public void execute(){}

    public void onPaint(java.awt.Graphics2D g){
        //Draw the tracked virtual bullets
        for (TrackedBullet bullet : alphabet.vGunManager.bullets){
            Point2D.Double bulletLocation = bullet.getLocation(alphabet);
            g.setColor(java.awt.Color.RED);
            g.drawOval((int)bulletLocation.x - 2, (int)bulletLocation.y - 2, 40, 40);
        }

        //Draw a box around target
        if (alphabet.radar.target.initialized){
            g.setColor(java.awt.Color.GREEN);
            g.drawRect((int)alphabet.radar.target.location.x-40, (int)alphabet.radar.target.location.y-40, 40, 40);
        }

        //Draw text in the top left that says mouseLocation
        g.setColor(java.awt.Color.WHITE);
        g.drawString("Mouse location: " + mouseLocation, 10, 10);
    }

    public void onMouseMoved(MouseEvent e){
        mouseLocation = new Point2D.Double(e.getX(), e.getY());
    }
}
