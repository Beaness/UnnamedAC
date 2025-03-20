package me.beanes.acid.plugin.simulation.data;

// Used for sneaking
public class MotionAreaXZ {
    public double minX, minZ, maxX, maxZ;

    public MotionAreaXZ(double minX, double minZ, double maxX, double maxZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    @Override
    public String toString() {
        return "MotionArea[" + minX + " -> " + maxX + ", " + minZ + " -> " + maxZ + "]";
    }
}
