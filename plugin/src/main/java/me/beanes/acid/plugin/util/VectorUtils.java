package me.beanes.acid.plugin.util;

import com.github.retrooper.packetevents.util.Vector3d;

public class VectorUtils {
    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public static Vector3d getIntermediateWithXValue(Vector3d self, Vector3d vec, double x) {
        double dX = vec.getX() - self.getX();
        double dY = vec.getY() - self.getY();
        double dZ = vec.getZ() - self.getZ();

        if (dX * dX < 1.0000000116860974E-7D) {
            return null;
        } else {
            double factor = (x - self.getX()) / dX;
            return factor >= 0.0D && factor <= 1.0D ? new Vector3d(self.getX() + dX * factor, self.getY() + dY * factor, self.getZ() + dZ * factor) : null;
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public static Vector3d getIntermediateWithYValue(Vector3d self, Vector3d vec, double y) {
        double dX = vec.getX() - self.getX();
        double dY = vec.getY() - self.getY();
        double d2 = vec.getZ() - self.getZ();

        if (dY * dY < 1.0000000116860974E-7D) {
            return null;
        } else {
            double factor = (y - self.getY()) / dY;
            return factor >= 0.0D && factor <= 1.0D ? new Vector3d(self.getX() + dX * factor, self.getY() + dY * factor, self.getZ() + d2 * factor) : null;
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    public static Vector3d getIntermediateWithZValue(Vector3d self, Vector3d vec, double z) {
        double dX = vec.getX() - self.getX();
        double dY = vec.getY() - self.getY();
        double dZ = vec.getZ() - self.getZ();

        if (dZ * dZ < 1.0000000116860974E-7D) {
            return null;
        } else {
            double factor = (z - self.getZ()) / dZ;
            return factor >= 0.0D && factor <= 1.0D ? new Vector3d(self.getX() + dX * factor, self.getY() + dY * factor, self.getZ() + dZ * factor) : null;
        }
    }

    public static double getDistanceXZ(Vector3d first, Vector3d second) {
        return Math.sqrt(getDistanceXZSquared(first, second));
    }

    public static double getDistanceXZSquared(Vector3d first, Vector3d other) {
        double distX = (first.x - other.x) * (first.x - other.x);
        double distZ = (first.z - other.z) * (first.z - other.z);
        return distX + distZ;
    }
}
