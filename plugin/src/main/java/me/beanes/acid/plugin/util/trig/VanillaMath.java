package me.beanes.acid.plugin.util.trig;

public class VanillaMath {
    private static final float[] SIN_TABLE = new float[65536];

    static {
        for (int i = 0; i < SIN_TABLE.length; ++i) {
            SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0D / 65536.0D);
        }
    }

    public static float sin(float radians)
    {
        return SIN_TABLE[(int)(radians * 10430.378F) & 65535];
    }

    public static float cos(float radians)
    {
        return SIN_TABLE[(int)(radians * 10430.378F + 16384.0F) & 65535];
    }
}
