package dylanbruner;

import java.awt.geom.*;
import java.util.List;

public class MathUtils {
    public static final double A_LITTLE_LESS_THAN_HALF_PI = 1.25;
    public static final int WALL_STICK = 160;
    public static java.awt.geom.Rectangle2D.Double fieldBox = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);

    public static int sign(double d) {
        if (d < 0) { return -1; } return 1;
    }
    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!fieldBox.contains(project(botLocation, angle, WALL_STICK))) {
            angle += orientation*0.05;
        }
        return angle;
    }

    public static Point2D.Double project(Point2D.Double sourceLocation,
        double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
            sourceLocation.y + Math.cos(angle) * length);
    }

    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double bulletVelocity(double power) {
        return (20.0 - (3.0*power));
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0/velocity);
    }

    public static double distance(Point2D.Double source, Point2D.Double target) {
        return source.distance(target);
    }

    public static double getAngle(Point2D.Double point2, Point2D.Double point1){
        return Math.atan2(point2.x - point1.x, point2.y - point1.y);
    }

    //Make a function that resizes a list of any type
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType(); //Yeah yeah i know this is a raw type but idk how else to make it work
        Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
        int preserveLength = Math.min(oldSize,newSize);
        if (preserveLength > 0)
            System.arraycopy (oldArray,0,newArray,0,preserveLength);
        return newArray; 
    }
}
