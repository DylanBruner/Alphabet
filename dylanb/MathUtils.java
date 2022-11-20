package dylanb;

public class MathUtils {
    public static final double A_LITTLE_LESS_THAN_HALF_PI = 1.25;
    public static java.awt.geom.Rectangle2D.Double fieldBox = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);

    public static int sign(double d) {
        if (d < 0) { return -1; } return 1;
    }
    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
