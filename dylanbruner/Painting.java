package dylanbruner;

import java.awt.geom.*;
import java.awt.event.MouseEvent;


public class Painting {
    Alphabet alphabet;
    AlphabetLogger logger = new AlphabetLogger("Painting");

    //Debug values
    Point2D.Double mouseLocation = new Point2D.Double(0, 0);
    
    public void init(Alphabet alphabet){
        this.alphabet = alphabet;
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

        for (Enemy enemy : alphabet.radar.enemies.values()){
            if (!alphabet.shadowGun.computedWeights.containsKey(enemy.name)) continue;
            double weight = alphabet.shadowGun.computedWeights.get(enemy.name);

            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
            //Draw a black background behind the text
            
            //Set the color to mint green
            g.setColor(new java.awt.Color(0, 255, 127));
            //Draw the text like 20 pixels above the enemy
            g.drawString(String.format("%.2f", weight), (int)enemy.location.x-20, (int)enemy.location.y + 40);
        }
    }

    public void onMouseMoved(MouseEvent e){
        mouseLocation = new Point2D.Double(e.getX(), e.getY());
    }
}
