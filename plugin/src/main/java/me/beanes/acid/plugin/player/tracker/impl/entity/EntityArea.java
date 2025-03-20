package me.beanes.acid.plugin.player.tracker.impl.entity;

import com.github.retrooper.packetevents.util.Vector3d;

public class EntityArea {
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public EntityArea(Vector3d vector3d) {
        this.minX = vector3d.getX();
        this.minY = vector3d.getY();
        this.minZ = vector3d.getZ();
        this.maxX = vector3d.getX();
        this.maxY = vector3d.getY();
        this.maxZ = vector3d.getZ();
    }

    public EntityArea(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void set(EntityArea area) {
        this.minX = area.minX;
        this.minY = area.minY;
        this.minZ = area.minZ;
        this.maxX = area.maxX;
        this.maxY = area.maxY;
        this.maxZ = area.maxZ;
    }

    public void set(double x, double y, double z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;
    }

    public void expand(double x, double y, double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
    }

    public void interpolate(EntityArea target, int interpolation) {
        this.minX = interpolate(this.minX, target.minX, interpolation);
        this.maxX = interpolate(this.maxX, target.maxX, interpolation);
        this.minY = interpolate(this.minY, target.minY, interpolation);
        this.maxY = interpolate(this.maxY, target.maxY, interpolation);
        this.minZ = interpolate(this.minZ, target.minZ, interpolation);
        this.maxZ = interpolate(this.maxZ, target.maxZ, interpolation);
    }

    private static double interpolate(double value, double destination, int interpolation) {
        return value + (destination - value) / (double) interpolation;
    }

    public void allow(EntityArea area) {
        this.minX = Math.min(area.minX, this.minX);
        this.minY = Math.min(area.minY, this.minY);
        this.minZ = Math.min(area.minZ, this.minZ);
        this.maxX = Math.max(area.maxX, this.maxX);
        this.maxY = Math.max(area.maxY, this.maxY);
        this.maxZ = Math.max(area.maxZ, this.maxZ);
    }

    public void allow(double x, double y, double z) {
        this.minX = Math.min(x, this.minX);
        this.minY = Math.min(y, this.minY);
        this.minZ = Math.min(z, this.minZ);
        this.maxX = Math.max(x, this.maxX);
        this.maxY = Math.max(y, this.maxY);
        this.maxZ = Math.max(z, this.maxZ);
    }

    public double getDistanceXZ(double x, double y, double z) {
        double distX = distanceX(x);
        double distZ = distanceZ(z);

        return Math.sqrt(distX * distX + distZ * distZ);
    }

    public double distanceX(double x) {
        return x >= this.minX && x <= this.maxX ? 0.0 : Math.min(Math.abs(x - this.minX), Math.abs(x - this.maxX));
    }

    public double distanceY(double y) {
        return y >= this.minY && y <= this.maxY ? 0.0 : Math.min(Math.abs(y - this.minY), Math.abs(y - this.maxY));
    }

    public double distanceZ(double z) {
        return z >= this.minZ && z <= this.maxZ ? 0.0 : Math.min(Math.abs(z - this.minZ), Math.abs(z - this.maxZ));
    }

    public boolean isCertain() {
        return this.minX == this.maxX && this.minY == this.maxY && this.minZ == this.maxZ;
    }

    @Override
    public String toString() {
        return "EntityArea[" + minX + " -> " + maxX + ", " + minY + " -> " + maxY + ", " + minZ + " -> " + maxZ + "]";
    }
}
