package me.beanes.acid.plugin.util.trig;

public class FastMath {
    private static final float[] SIN_TABLE = new float[4096];
    private static final float radToIndex = roundToFloat(651.8986469044033D);

    static {
        for (int j = 0; j < SIN_TABLE.length; ++j)
        {
            SIN_TABLE[j] = roundToFloat(Math.sin((double)j * Math.PI * 2.0D / 4096.0D));
        }
    }

    public static float sin(float radians)
    {
        return SIN_TABLE[(int)(radians * radToIndex) & 4095];
    }

    public static float cos(float radians)
    {
        return SIN_TABLE[(int)(radians * radToIndex + 1024.0F) & 4095];
    }

    private static float roundToFloat(double d)
    {
        return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
    }
}
