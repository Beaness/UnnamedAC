package me.beanes.acid.plugin.util.trig;

import me.beanes.acid.plugin.util.MCMath;

// OptiFine is very fun, 1.7 uses this but also older versions of 1.8 OptiFine...
// Very nice!
public class LegacyFastMath {
    private static final float[] SIN_TABLE = new float[4096];

    static {
        for (int i = 0; i < 4096; ++i) {
            SIN_TABLE[i] = (float)Math.sin(((float)i + 0.5F) / 4096.0F * 6.2831855F);
        }

        for(int i = 0; i < 360; i += 90) {
            SIN_TABLE[(int)((float)i * 11.377778F) & 4095] = (float)Math.sin((float)i * MCMath.DEGREES_TO_RADIANS);
        }
    }

    public static float sin(float radians)
    {
        return SIN_TABLE[(int)(radians * 651.8986F) & 4095];
    }

    public static float cos(float radians)
    {
        return SIN_TABLE[(int)((radians + ((float)Math.PI / 2F)) * 651.8986F) & 4095];
    }
}
