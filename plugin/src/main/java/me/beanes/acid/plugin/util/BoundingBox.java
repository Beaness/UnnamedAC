package me.beanes.acid.plugin.util;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;

// Why not use packetevents box? well because in this I can add some methods + make it immutable
@Getter
public class BoundingBox {
    private static final double COLLISION_EPSILON = 1.0E-7;

    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;

    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public BoundingBox offset(double x, double y, double z) {
        return new BoundingBox(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public double offsetX(BoundingBox other, double offsetX) {
        if (offsetX >= 0.0) {
            double max_move = minX - other.maxX; // < 0.0 if no strict collision
            if (max_move < -COLLISION_EPSILON) {
                return offsetX;
            }
            return Math.min(max_move, offsetX);
        } else {
            double max_move = maxX - other.minX; // > 0.0 if no strict collision
            if (max_move > COLLISION_EPSILON) {
                return offsetX;
            }
            return Math.max(max_move, offsetX);
        }
    }

    public double offsetY(BoundingBox other, double offsetY) {
        if (offsetY >= 0.0) {
            double max_move = minY - other.maxY; // < 0.0 if no strict collision
            if (max_move < -COLLISION_EPSILON) {
                return offsetY;
            }
            return Math.min(max_move, offsetY);
        } else {
            double max_move = maxY - other.minY; // > 0.0 if no strict collision
            if (max_move > COLLISION_EPSILON) {
                return offsetY;
            }
            return Math.max(max_move, offsetY);
        }
    }

    public double offsetZ(BoundingBox other, double offsetZ) {
        if (offsetZ >= 0.0) {
            double max_move = minZ - other.maxZ; // < 0.0 if no strict collision
            if (max_move < -COLLISION_EPSILON) {
                return offsetZ;
            }
            return Math.min(max_move, offsetZ);
        } else {
            double max_move = maxZ - other.minZ; // > 0.0 if no strict collision
            if (max_move > COLLISION_EPSILON) {
                return offsetZ;
            }
            return Math.max(max_move, offsetZ);
        }
    }

    public BoundingBox addCoord(double x, double y, double z) {
        double minX = this.minX;
        double minY = this.minY;
        double minZ = this.minZ;
        double maxX = this.maxX;
        double maxY = this.maxY;
        double maxZ = this.maxZ;

        if (x < 0.0D) {
            minX += x;
        } else if (x > 0.0D) {
            maxX += x;
        }

        if (y < 0.0D) {
            minY += y;
        } else if (y > 0.0D) {
            maxY += y;
        }

        if (z < 0.0D) {
            minZ += z;
        } else if (z > 0.0D) {
            maxZ += z;
        }

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BoundingBox expand(double x, double y, double z) {
        double newMinX = this.minX - x;
        double newMinY = this.minY - y;
        double newMinZ = this.minZ - z;
        double newMaxX = this.maxX + x;
        double newMaxY = this.maxY + y;
        double newMaxZ = this.maxZ + z;
        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox contract(double x, double y, double z) {
        double newMinX = this.minX + x;
        double newMinY = this.minY + y;
        double newMinZ = this.minZ + z;
        double newMaxX = this.maxX - x;
        double newMaxY = this.maxY - y;
        double newMaxZ = this.maxZ - z;
        return new BoundingBox(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    public BoundingBox expand(double value) {
        return expand(value, value, value);
    }

    public BoundingBox contract(double value) {
        return contract(value, value, value);
    }

    public BoundingBox expandXZ(double value) {
        return expand(value, 0, value);
    }

    public BoundingBox contractXZ(double value) {
        return contract(value, 0, value);
    }

    public InterceptData calculateIntercept(Vector3d start, Vector3d end) {
        Vector3d minX = VectorUtils.getIntermediateWithXValue(start, end, this.minX);
        Vector3d maxX = VectorUtils.getIntermediateWithXValue(start, end, this.maxX);
        Vector3d minY = VectorUtils.getIntermediateWithYValue(start, end, this.minY);
        Vector3d maxY = VectorUtils.getIntermediateWithYValue(start, end, this.maxY);
        Vector3d minZ = VectorUtils.getIntermediateWithZValue(start, end, this.minZ);
        Vector3d maxZ = VectorUtils.getIntermediateWithZValue(start, end, this.maxZ);

        if (!isVecInYZ(minX)) {
            minX = null;
        }

        if (!isVecInYZ(maxX)) {
            maxX = null;
        }

        if (!isVecInXZ(minY)) {
            minY = null;
        }

        if (!isVecInXZ(maxY)) {
            maxY = null;
        }

        if (!isVecInXY(minZ)) {
            minZ = null;
        }

        if (!isVecInXY(maxZ)) {
            maxZ = null;
        }

        Vector3d best = null;
        BlockFace bestFace = null;

        if (minX != null) {
            best = minX;
            bestFace = BlockFace.WEST;
        }

        if (maxX != null && (best == null || start.distanceSquared(maxX) < start.distanceSquared(best))) {
            best = maxX;
            bestFace = BlockFace.EAST;
        }

        if (minY != null && (best == null || start.distanceSquared(minY) < start.distanceSquared(best))) {
            best = minY;
            bestFace = BlockFace.DOWN;
        }

        if (maxY != null && (best == null || start.distanceSquared(maxY) < start.distanceSquared(best))) {
            best = maxY;
            bestFace = BlockFace.UP;
        }

        if (minZ != null && (best == null || start.distanceSquared(minZ) < start.distanceSquared(best))) {
            best = minZ;
            bestFace = BlockFace.NORTH;
        }

        if (maxZ != null && (best == null || start.distanceSquared(maxZ) < start.distanceSquared(best))) {
            best = maxZ;
            bestFace = BlockFace.SOUTH;
        }

        if (best == null) {
            return null;
        } else {
            return new InterceptData(best, bestFace);
        }
    }

    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
     * passed in vector, or null if not possible.
     */
    private static Vector3d getIntermediateWithXValue(Vector3d self, Vector3d vec, double x) {
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
    private static Vector3d getIntermediateWithYValue(Vector3d self, Vector3d vec, double y) {
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
    private static Vector3d getIntermediateWithZValue(Vector3d self, Vector3d vec, double z) {
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

    /**
     * Checks if the specified vector is within the YZ dimensions of the bounding box. Args: Vec3D
     */
    private boolean isVecInYZ(Vector3d vec) {
        return vec != null && vec.getY() >= this.getMinY() && vec.getY() <= this.getMaxY() && vec.getZ() >= this.getMinZ() && vec.getZ() <= this.getMaxZ();
    }

    /**
     * Checks if the specified vector is within the XZ dimensions of the bounding box. Args: Vec3D
     */
    private boolean isVecInXZ(Vector3d vec) {
        return vec != null && vec.getX() >= this.getMinX() && vec.getX() <= this.getMaxX() && vec.getZ() >= this.getMinZ() && vec.getZ() <= this.getMaxZ();
    }

    /**
     * Checks if the specified vector is within the XY dimensions of the bounding box. Args: Vec3D
     */
    private boolean isVecInXY(Vector3d vec) {
        return vec != null && vec.getX() >= this.getMinX() && vec.getX() <= this.getMaxX() && vec.getY() >= this.getMinY() && vec.getY() <= this.getMaxY();
    }

    public boolean intersectsWith(BoundingBox other) {
        return other.maxX > this.minX
                && other.minX < this.maxX
                && other.maxY > this.minY
                && other.minY < this.maxY
                && other.maxZ > this.minZ
                && other.minZ < this.maxZ;
    }

    /**
     * Returns if the supplied Vec3D is completely inside the bounding box
     */
    public boolean isVecInside(Vector3d vector3d)
    {
        return vector3d.x > this.minX && vector3d.x < this.maxX && vector3d.y > this.minY && vector3d.y < this.maxY && vector3d.z > this.minZ && vector3d.z < this.maxZ;
    }

    public double getClosestDistance(Vector3d point) {
        double dx = Math.max(0, Math.max(minX - point.x, point.x - maxX));
        double dy = Math.max(0, Math.max(minY - point.y, point.y - maxY));
        double dz = Math.max(0, Math.max(minZ - point.z, point.z - maxZ));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public String toString()
    {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }
}
