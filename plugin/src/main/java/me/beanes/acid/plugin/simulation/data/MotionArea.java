package me.beanes.acid.plugin.simulation.data;

import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import me.beanes.acid.plugin.util.MCMath;

import java.util.UUID;

public class MotionArea {
    public double minX, minY, minZ, maxX, maxY, maxZ;
    public boolean using; // Used to prevent noslow (because mojang funny netcode)
    @Getter
    private final UUID track = UUID.randomUUID(); // testing purposes

    public MotionArea() {
        this.minX = Double.POSITIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.minZ = Double.POSITIVE_INFINITY;
        this.maxX = Double.NEGATIVE_INFINITY;
        this.maxY = Double.NEGATIVE_INFINITY;
        this.maxZ = Double.NEGATIVE_INFINITY;
    }

    public MotionArea(Vector3d vector3d) {
        this.minX = vector3d.getX();
        this.minY = vector3d.getY();
        this.minZ = vector3d.getZ();
        this.maxX = vector3d.getX();
        this.maxY = vector3d.getY();
        this.maxZ = vector3d.getZ();
    }

    public MotionArea(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public MotionArea copy() {
        return new MotionArea(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public void allowX(double x) {
        this.minX = Math.min(x, this.minX);
        this.maxX = Math.max(x, this.maxX);
    }

    public void allowY(double y) {
        this.minY = Math.min(y, this.minY);
        this.maxY = Math.max(y, this.maxY);
    }

    public void allowZ(double z) {
        this.minZ = Math.min(z, this.minZ);
        this.maxZ = Math.max(z, this.maxZ);
    }

    public void extendX(double x) {
        if (x > 0) {
            this.maxX += x;
        } else {
            this.minX += x;
        }
    }

    public void extendY(double y) {
        if (y > 0) {
            this.maxY += y;
        } else {
            this.minY += y;
        }
    }

    public void extendZ(double z) {
        if (z > 0) {
            this.maxZ += z;
        } else {
            this.minZ += z;
        }
    }

    public void extend(MotionArea area) {
        if (area.minX < 0) {
            this.minX += area.minX;
        }

        if (area.minY < 0) {
            this.minY += area.minY;
        }

        if (area.minZ < 0) {
            this.minZ += area.minZ;
        }

        if (area.maxX > 0) {
            this.maxX += area.maxX;
        }

        if (area.maxY > 0) {
            this.maxY += area.maxY;
        }

        if (area.maxZ > 0) {
            this.maxZ += area.maxZ;
        }
    }

    public void add(MotionArea area) {
        this.minX += area.minX;
        this.minY += area.minY;
        this.minZ += area.minZ;
        this.maxX += area.maxX;
        this.maxY += area.maxY;
        this.maxZ += area.maxZ;
    }

    // hahaha what am  I doing haha
    public MotionArea createNormalisedArea() {
        MotionArea normalisedArea = new MotionArea();

        for (double x : new double[]{this.minX, this.maxX}) {
            for (double y : new double[]{this.minY, this.maxY}) {
                for (double z : new double[]{this.minZ, this.maxZ}) {
                    double sqrt = MCMath.sqrt_double(x * x + y * y + z * z);

                    double allowX = x / sqrt;
                    double allowY = y / sqrt;
                    double allowZ = z / sqrt;

                    if (Double.isInfinite(allowX) || Math.abs(allowX) < 1.0E-10D) {
                        normalisedArea.allowX(0);
                    } else {
                        normalisedArea.allowX(allowX);
                    }

                    if (Double.isInfinite(allowY) || Math.abs(allowY) < 1.0E-10D) {
                        normalisedArea.allowY(0);
                    } else {
                        normalisedArea.allowY(allowY);
                    }

                    if (Double.isInfinite(allowZ) || Math.abs(allowZ) < 1.0E-10D) {
                        normalisedArea.allowZ(0);
                    } else {
                        normalisedArea.allowZ(allowZ);
                    }
                }
            }
        }

        return normalisedArea;
    }

    @Override
    public String toString() {
        return "MotionArea[" + minX + " -> " + maxX + ", " + minY + " -> " + maxY + ", " + minZ + " -> " + maxZ + "] track=" + track;
    }

    public void expand(double value) {
        this.minX -= value;
        this.minY -= value;
        this.minZ -= value;
        this.maxX += value;
        this.maxY += value;
        this.maxZ += value;
    }
}
