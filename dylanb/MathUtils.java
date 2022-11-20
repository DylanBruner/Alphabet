package dylanb;

public class MathUtils {
    public static int sign(double d) {
        if (d < 0) { return -1; } return 1;
    }
    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
