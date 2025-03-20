package me.beanes.acid.plugin.util;

import com.github.retrooper.packetevents.util.Vector3d;
import me.beanes.acid.plugin.util.trig.TrigHandler;

// Copied from MCP 1.8.9
public class MCMath {
    public static final float DEGREES_TO_RADIANS = 0.017453292F;

    public static Vector3d getVectorForRotation(TrigHandler trig, float pitch, float yaw) {
        float f = trig.cos(-yaw * MCMath.DEGREES_TO_RADIANS - (float) Math.PI);
        float f1 = trig.sin(-yaw * MCMath.DEGREES_TO_RADIANS - (float) Math.PI);
        float f2 = -trig.cos(-pitch * MCMath.DEGREES_TO_RADIANS);
        float y = trig.sin(-pitch * MCMath.DEGREES_TO_RADIANS);
        return new Vector3d(f1 * f2, y, f * f2);
    }

    public static double clamp_double(double num, double min, double max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static float sqrt_float(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sqrt_double(double value) {
        return (float) Math.sqrt(value);
    }

    public static int floor_double(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static int floor_float(float value)
    {
        int i = (int)value;
        return value < (float)i ? i - 1 : i;
    }

    public static float abs(float value) {
        return value >= 0.0F ? value : -value;
    }

    public static int abs_int(int value) {
        return value >= 0 ? value : -value;
    }
}
