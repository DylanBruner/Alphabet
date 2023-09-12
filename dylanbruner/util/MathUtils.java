package dylanbruner.util;

import java.awt.geom.*;

/*
 * This file is a collection of formulas I've made and found from robowiki.net
 * If i have a formula in here that's not mine its *probably* listed in the
 * credits file 
*/

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

    public static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
		return new Point2D.Double(p.x + dist*Math.sin(ang), p.y + dist*Math.cos(ang));
	}
	
	public static double calcAngle(Point2D.Double p2, Point2D.Double p1){
		return Math.atan2(p2.x - p1.x, p2.y - p1.y);
	}

    public static double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!fieldBox.contains(project(botLocation, angle, WALL_STICK))) {
            angle += orientation*0.05;
        }
        return angle;
    }

    public static Point2D.Double project(Point2D sourceLocation,
        double angle, double length) {
        return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
            sourceLocation.getY() + Math.cos(angle) * length);
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

    public static double minMax(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int minMax(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    @SuppressWarnings("rawtypes")
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
        int preserveLength = Math.min(oldSize,newSize);
        if (preserveLength > 0)
            System.arraycopy (oldArray,0,newArray,0,preserveLength);
        return newArray; 
    }
}
