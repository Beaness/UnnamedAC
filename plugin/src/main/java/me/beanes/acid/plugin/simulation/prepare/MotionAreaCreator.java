package me.beanes.acid.plugin.simulation.prepare;

import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import lombok.Getter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.simulation.data.MotionArea;
import me.beanes.acid.plugin.simulation.data.MotionAreaXZ;
import me.beanes.acid.plugin.simulation.processor.AreaProcessor;
import me.beanes.acid.plugin.util.BoxUtil;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

@Getter
public class MotionAreaCreator extends AreaProcessor {

    public MotionAreaCreator(PlayerData data) {
        super(data);
    }

    private final FloatList normalInertia = new FloatArrayList();
    private final FloatList waterInertia = new FloatArrayList();

    public void reset() {
        this.normalInertia.clear();
        this.waterInertia.clear();
    }

    public MotionArea create() {
        System.out.println("> lastUncertain=" + data.getPositionTracker().isLastUncertain() + " uncertain=" + data.getPositionTracker().isUncertain() + " tps=" + data.getPositionTracker().isLastTeleport() + " " + data.getPositionTracker().isLastLastTeleport());

        MotionArea in;

        if (!data.getPositionTracker().isUncertain()) {
            double dX = data.getPositionTracker().getDeltaX();
            double dY = data.getPositionTracker().getDeltaY();
            double dZ = data.getPositionTracker().getDeltaZ();
            in = new MotionArea(dX, dY, dZ, dX, dY, dZ);
        } else {
            if (data.getPositionTracker().isLastTeleport()) {
                /*
                Thanks to minecraft we gotta account for more 0.03 stupidity, basically the issue is this:
                1) Client tps 5 blocks next to lastReportedPos 0.03 area (because of a server teleport)
                2) The client ticks and moves into the 0.03 area by moving 5 blocks (this can be due to speed potion for example)
                3) The client believes it doesnt need to report its position even though it moved 5 blocks (because it thinks its in the 0.03 zone of lastReportedPos)
                4) The simulation thinks the client did 0.03 so it only uses 0.03 and calculates inertia on it when it actually should use 5
                 */
                in = new MotionArea(
                        data.getPositionTracker().getReportedX() - data.getPositionTracker().getX() - 0.03D,
                        data.getPositionTracker().getReportedY() - data.getPositionTracker().getY() - 0.03D,
                        data.getPositionTracker().getReportedZ() - data.getPositionTracker().getZ() - 0.03D,
                        data.getPositionTracker().getReportedX() - data.getPositionTracker().getX() + 0.03D,
                        data.getPositionTracker().getReportedY() - data.getPositionTracker().getY() + 0.03D,
                        data.getPositionTracker().getReportedZ() - data.getPositionTracker().getZ() + 0.03D
                );

                // Copy it to the simulation engine teleport uncertainty leniency
                data.getSimulationEngine().setTeleportUncertaintyLeniency(in.copy());
            } else {
                // This could use values from successful simulations to reduce the 0.03D value
                in = new MotionArea(-0.03D, -0.03D, -0.03D, 0.03D, 0.03D, 0.03D);
            }
        }

        if (data.getActionTracker().isSneaking() && data.getSimulationEngine().getHiddenSneakMotion() != null) {
            MotionAreaXZ xz = data.getSimulationEngine().getHiddenSneakMotion();
            in.minX = Math.min(xz.minX, in.minX);
            in.maxX = Math.max(xz.maxX, in.maxX);
            in.minZ = Math.min(xz.minZ, in.minZ);
            in.maxZ = Math.max(xz.maxZ, in.maxZ);
        }

        if (data.getSimulationEngine().getLiquidState().isWaterNotPossible()) {
            data.getSimulationEngine().handleWaterPush(data.getPositionTracker().getBox().expand(0.0D, -0.4F, 0.0D).contract(0.001D, 0.001D, 0.001D));
        }

        in.extend(data.getSimulationEngine().getWaterPush());

        SplitStateBoolean webState = this.doBlockCollision(in);

        if (data.getPositionTracker().isLastUncertain()) {
            in.expand(0.03D);
        }

        // If the player respawn they are at some dumb as position around 0, 0, 0 -> the netcode is so fucked
        // It causes the air inertia to be enforced
        // If respawning still somehow falses -> we can just exempt the tick after respawn to fix it ig
        if (data.getRespawnTracker().isPossibleRespawning()) {
            normalInertia.add(0.91F);
        }


        return this.calculateOutput(in, webState);
    }


    private MotionArea calculateOutput(MotionArea in, SplitStateBoolean webState) {
        System.out.println("> DELTAS deltaX=" + data.getPositionTracker().getDeltaX() + " deltaY=" + data.getPositionTracker().getDeltaY() + " deltaZ=" + data.getPositionTracker().getDeltaZ() + " onGround=" + data.getPositionTracker().isOnGround());

        System.out.println("> IN: " + in);

        MotionArea out = new MotionArea();

        // This web state is the state from last tick (mc also uses this)
        if (webState == SplitStateBoolean.TRUE) {
            out.minX = 0;
            out.maxX = 0;

            out.minY = -0.08D * (double) 0.98F;
            out.maxY = -0.08D * (double) 0.98F;

            out.minZ = 0;
            out.maxZ = 0;
            return out;
        } else if (webState == SplitStateBoolean.POSSIBLE) {
            out.allowX(0);
            out.allowY(-0.08D * (double) 0.98F);
            out.allowZ(0);
        }

        if (data.getSimulationEngine().getLiquidState().isLavaPossible()) {
            System.out.println("> LAVA");

            out.minX = Math.min(in.minX * 0.5D, in.minX);
            out.maxX = Math.max(in.maxX * 0.5D, in.maxX);

            out.minY = Math.min(in.minY * 0.5D - 0.02D, out.minY);
            out.maxY = Math.max(in.maxY * 0.5D - 0.02D, out.maxY);

            out.minZ = Math.min(in.minZ * 0.5D, out.minZ);
            out.maxZ = Math.max(in.maxZ * 0.5D, out.maxZ);
        }

        if (data.getSimulationEngine().getLiquidState().isWaterPossible()) {
            System.out.println("> WATER");

            for (float inertia : waterInertia) {
                out.minX = Math.min(in.minX * inertia, out.minX);
                out.maxX = Math.max(in.maxX * inertia, out.maxX);

                out.minY = Math.min((in.minY * 0.800000011920929D) - 0.02D, out.minY);
                out.maxY = Math.max((in.maxY * 0.800000011920929D) - 0.02D, out.maxY);

                out.minZ = Math.min(in.minZ * inertia, out.minZ);
                out.maxZ = Math.max(in.maxZ * inertia, out.maxZ);
            }
        }

        if (data.getSimulationEngine().getLiquidState().isLavaPossible() || data.getSimulationEngine().getLiquidState().isWaterPossible()) {
            boolean possibleCollideHorizontal = data.getSimulationEngine().getCollisionResult().getCollidedX().possible() || data.getSimulationEngine().getCollisionResult().getCollidedZ().possible();
            boolean possibleNotLiquidOffset = this.isPossibleNotLiquidOffset(out);

            if (possibleCollideHorizontal && possibleNotLiquidOffset) {
                out.allowY(0.3F);
            }
        }

        // Normal TODO: implement unloaded chunk motionY

        SplitStateBoolean isInLoadedChunk = isPlayerInLoadedChunk();

        System.out.println("> isInLoadedChunk = " + isInLoadedChunk);

        // For the sneaking extra movement we can just use the maximum area as a "maximum" extra sneak :D
        if (data.getSimulationEngine().getLiquidState().isNormalPossible()) {
            System.out.println("> NORMAL");

            if (isInLoadedChunk.possible()) {
                boolean possibleCollideHorizontal = data.getSimulationEngine().getCollisionResult().getCollidedX().possible() || data.getSimulationEngine().getCollisionResult().getCollidedZ().possible();
                SplitStateBoolean ladderState = data.getSimulationEngine().getLadderProcessor().getLadderState(false);

                if (possibleCollideHorizontal && ladderState.possible()) {
                    out.minY = Math.min((0.2D - 0.08D) * (double) 0.98F, out.minY);
                    out.maxY = Math.max((0.2D - 0.08D) * (double) 0.98F, out.maxY);
                }
            }

            for (double inertia : normalInertia) {
                System.out.println("> Inertia = " + inertia);

                out.minX = Math.min(in.minX * inertia, out.minX);
                out.maxX = Math.max(in.maxX * inertia, out.maxX);

                if (isInLoadedChunk.possible()) {
                    out.minY = Math.min((in.minY - 0.08D) * (double) 0.98F, out.minY);
                    out.maxY = Math.max((in.maxY - 0.08D) * (double) 0.98F, out.maxY);
                }

                out.minZ = Math.min(in.minZ * inertia, out.minZ);
                out.maxZ = Math.max(in.maxZ * inertia, out.maxZ);
            }
        }

        // Chunk unload falling logic
        if (isInLoadedChunk.notPossible()) {
            if (data.getPositionTracker().isUncertain()) {
                boolean aboveZeroNormal = data.getPositionTracker().getY() > 0;
                boolean aboveZeroUncertain = (data.getPositionTracker().getY() - 0.03D) > 0;

                SplitStateBoolean isAboveZero = SplitStateBoolean.result(aboveZeroNormal, aboveZeroUncertain);

                if (isAboveZero.possible()) {
                    out.allowY(-0.1D * (double) 0.98F);
                }

                if (isAboveZero.notPossible()) {
                    out.allowY(0.0D);
                }
            } else {
                if (isInLoadedChunk == SplitStateBoolean.TRUE) {
                    if (data.getPositionTracker().getY() > 0.0D) {
                        out.minY = -0.1D * (double) 0.98F;
                        out.maxY = -0.1D * (double) 0.98F;
                    } else {
                        out.minY = 0.0D;
                        out.maxY = 0.0D;
                    }
                } else {
                    if (data.getPositionTracker().getY() > 0.0D) {
                        out.allowY(-0.1D * (double) 0.98F);
                    } else {
                        out.allowY(0.0D);
                    }
                }
            }
        }

        if (data.getSimulationEngine().getCollisionResult().getCollidedX().possible()) {
            System.out.println("> X COLLISION");
            out.allowX(0D);
        }

        if (data.getSimulationEngine().getCollisionResult().getCollidedY().possible() && isInLoadedChunk.possible()) {
            System.out.println("> Y COLLISION");

            if (data.getSimulationEngine().getLiquidState().isNormalPossible()) {
                out.allowY(-0.08D * (double) 0.98F); // motionY is 0 here and then gravity is calculated
            }

            if (data.getSimulationEngine().getLiquidState().isLavaPossible() || data.getSimulationEngine().getLiquidState().isWaterPossible()) {
                out.allowY(-0.02D);
            }
        }

        if (data.getSimulationEngine().getCollisionResult().getCollidedZ().possible()) {
            System.out.println("> Z COLLISION");
            out.allowZ(0D);
        }

        // The player might be respawning which causes the motion values to reset (actually this code might not be needed as we wait for teleport if you think about it)
        if (data.getRespawnTracker().isPossibleRespawning()) {
            out.allowX(0);
            out.allowY(0);
            out.allowZ(0);
        }

        return out;
    }

    private SplitStateBoolean isPlayerInLoadedChunk() {
        if (data.getPositionTracker().isUncertain()) {
            int minX = (int) (data.getPositionTracker().getX() - 0.03D);
            int maxX = (int) (data.getPositionTracker().getX() + 0.03D);
            int minZ = (int) (data.getPositionTracker().getZ() - 0.03D);
            int maxZ = (int) (data.getPositionTracker().getZ() + 0.03D);

            SplitStateBoolean isInLoadedChunk = null;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (isInLoadedChunk == null) {
                        isInLoadedChunk = data.getWorldTracker().isChunkLoadedAtXZ(x, z);
                    } else {
                        isInLoadedChunk = SplitStateBoolean.result(isInLoadedChunk, data.getWorldTracker().isChunkLoadedAtXZ(x, z));
                    }

                    if (isInLoadedChunk == SplitStateBoolean.POSSIBLE) {
                        return isInLoadedChunk;
                    }
                }
            }

            return isInLoadedChunk;
        } else {
            return data.getWorldTracker().isChunkLoadedAtXZ((int) data.getPositionTracker().getX(), (int) data.getPositionTracker().getZ());
        }
    }

    // Gotta love MCP which has bad naming for this shit
    private boolean isPossibleNotLiquidOffset(MotionArea area) {
        // This box is the new movement box -> NOT THE OLD! because we run this check AFTER moveEntity() happened
        BoundingBox playerBox = data.getPositionTracker().getBox();

        double toAddMinY = area.minY + 0.6000000238418579D - data.getPositionTracker().getY() + data.getPositionTracker().getLastY();
        double toAddMaxY = area.maxY + 0.6000000238418579D - data.getPositionTracker().getY() + data.getPositionTracker().getLastY();

        // This system off checking for air for the liquid hop is more lenient than vanilla but there are some restrictions
        // We offset the player box with what we know the offset could be based on certain, we extend the box with what we do not know is certain
        double minX, minY, minZ, maxX, maxY, maxZ;

        // X
        if (area.minX < 0) {
            minX = playerBox.getMinX() + area.minX;
        } else {
            if (data.getSimulationEngine().getCollisionResult().getCollidedX().possible()) {
                minX = playerBox.getMinX();
            } else {
                minX = playerBox.getMinX() + area.minX;
            }
        }

        if (area.maxX > 0) {
            maxX = playerBox.getMaxX() + area.maxX;
        } else {
            if (data.getSimulationEngine().getCollisionResult().getCollidedX().possible()) {
                maxX = playerBox.getMaxX();
            } else {
                maxX = playerBox.getMaxX() + area.maxX;
            }
        }

        // Z
        if (area.minZ < 0) {
            minZ = playerBox.getMinZ() + area.minZ;
        } else {
            if (data.getSimulationEngine().getCollisionResult().getCollidedZ().possible()) {
                minZ = playerBox.getMinZ();
            } else {
                minZ = playerBox.getMinZ() + area.minZ;
            }
        }

        if (area.maxZ > 0) {
            maxZ = playerBox.getMaxZ() + area.maxZ;
        } else {
            if (data.getSimulationEngine().getCollisionResult().getCollidedZ().possible()) {
                maxZ = playerBox.getMaxZ();
            } else {
                maxZ = playerBox.getMaxZ() + area.maxZ;
            }
        }

        // Y
        if (toAddMinY < 0) {
            minY = playerBox.getMinY() + toAddMinY;
        } else {
            if (data.getSimulationEngine().getCollisionResult().getCollidedY().possible()) {
                minY = playerBox.getMinY();
            } else {
                minY = playerBox.getMinY() + toAddMinY;
            }
        }

        if (toAddMaxY > 0) {
            maxY = playerBox.getMaxY() + toAddMinY;
        } else {
            if (data.getSimulationEngine().getCollisionResult().getCollidedY().possible()) {
                maxY = playerBox.getMaxY();
            } else {
                maxY = playerBox.getMaxY() + toAddMinY;
            }
        }

        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        // So what I do is if there is a non liquid block in the box -> allow the hop!
        return data.getWorldTracker().isPossibleNotLiquid(box);
    }

    // Returns the last web state block used for the output motion
    private SplitStateBoolean doBlockCollision(MotionArea in) {
        this.checkSlimeCollision(in);

        BoundingBox box = BoxUtil.getPlayerBox(data.getPositionTracker().getReportedX(), data.getPositionTracker().getReportedY(), data.getPositionTracker().getReportedZ(), -0.001D);

        int minX = MCMath.floor_double(box.getMinX());
        int minY = MCMath.floor_double(box.getMinY());
        int minZ = MCMath.floor_double(box.getMinZ());
        int maxX = MCMath.floor_double(box.getMaxX());
        int maxY = MCMath.floor_double(box.getMaxY());
        int maxZ = MCMath.floor_double(box.getMaxZ());

        SplitStateBoolean lastWebState = data.getSimulationEngine().getWebProcessor().getState();

        data.getSimulationEngine().getWebProcessor().setState(SplitStateBoolean.FALSE);

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    SplitStateBoolean soulSand = data.getWorldTracker().isMaterial(x, y, z, StateTypes.SOUL_SAND);

                    // Change to possible if working with uncertainty
                    if (soulSand == SplitStateBoolean.TRUE && data.getPositionTracker().isUncertain()) soulSand = SplitStateBoolean.POSSIBLE;

                    if (soulSand == SplitStateBoolean.TRUE) {
                        in.minX *= 0.4D;
                        in.minZ *= 0.4D;
                        in.maxX *= 0.4D;
                        in.maxZ *= 0.4D;
                    } else if (soulSand == SplitStateBoolean.POSSIBLE) {
                        in.allowX(in.minX * 0.4D);
                        in.allowZ(in.minZ * 0.4D);
                        in.allowX(in.maxX * 0.4D);
                        in.allowZ(in.maxZ * 0.4D);
                    }

                    SplitStateBoolean web = data.getWorldTracker().isMaterial(x, y, z, StateTypes.COBWEB);

                    if (web == SplitStateBoolean.TRUE && data.getPositionTracker().isUncertain()) {
                        web = SplitStateBoolean.POSSIBLE;
                    }

                    if (web.possible()) {
                        data.getSimulationEngine().getWebProcessor().setState(web);
                    }
                }
            }
        }

        return lastWebState;
    }

    private void checkSlimeCollision(MotionArea in) {
        // This code is not triggered if the player is flying
        SplitStateBoolean slime = getCollisionState(StateTypes.SLIME_BLOCK);
        double absMinY = Math.abs(data.getSimulationEngine().getCollisionResult().getMaximumMotionY());

        if (slime.possible() && !data.getActionTracker().isSneaking() && data.getPositionTracker().isOnGround()) {
            double absMaxY = Math.abs(data.getSimulationEngine().getCollisionResult().getMinimalMotionY());

            boolean possibleSlowdown = Math.min(absMinY, absMaxY) < 0.1D;
            boolean certainSlowdown = Math.max(absMinY, absMaxY) < 0.1D;
            SplitStateBoolean slowdown = SplitStateBoolean.result(slime, SplitStateBoolean.result(possibleSlowdown, certainSlowdown));

            if (slowdown.possible()) {
                double first = 0.4D + Math.min(absMinY, 0.1D) * 0.2D;
                double second = 0.4D + Math.min(absMaxY, 0.1D) * 0.2D;
                if (slowdown == SplitStateBoolean.TRUE) {
                    in.minX = Math.min(in.minX * first, in.minX * second);
                    in.maxX = Math.max(in.maxX * first, in.maxX * second);
                    in.minZ = Math.min(in.minZ * first, in.minZ * second);
                    in.maxZ = Math.max(in.maxZ * first, in.maxZ * second);
                } else {
                    in.minX = Math.min(in.minX * first, in.minX);
                    in.maxX = Math.max(in.maxX * first, in.maxX);
                    in.minZ = Math.min(in.minZ * first, in.minZ);
                    in.maxZ = Math.max(in.maxZ * first, in.maxZ);

                    in.minX = Math.min(in.minX * second, in.minX);
                    in.maxX = Math.max(in.maxX * second, in.maxX);
                    in.minZ = Math.min(in.minZ * second, in.minZ);
                    in.maxZ = Math.max(in.maxZ * second, in.maxZ);
                }
            }
        }

        if (data.getSimulationEngine().getCollisionResult().getCollidedY().possible() && slime.possible()) {
            in.allowY(-data.getSimulationEngine().getCollisionResult().getMinimalMotionY());
        }
    }

    public SplitStateBoolean getCollisionState(StateType state) {
        if (data.getPositionTracker().isUncertain()) {
            BoundingBox uncertainBox = new BoundingBox(
                    data.getPositionTracker().getX() - 0.03D,
                    data.getPositionTracker().getY() - ((double) 0.2F) - 0.03D,
                    data.getPositionTracker().getZ() - 0.03D,
                    data.getPositionTracker().getX() + 0.03D,
                    data.getPositionTracker().getY() - ((double) 0.2F) + 0.03D,
                    data.getPositionTracker().getZ() + 0.03D
            );

            SplitStateBoolean materialInBB = data.getWorldTracker().isMaterialInBB(uncertainBox, state);

            return (materialInBB == SplitStateBoolean.TRUE || materialInBB == SplitStateBoolean.POSSIBLE) ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE;
        } else {
            int x = MCMath.floor_double(data.getPositionTracker().getX());
            int y = MCMath.floor_double(data.getPositionTracker().getY() - ((double) 0.2F));
            int z = MCMath.floor_double(data.getPositionTracker().getZ());

            return data.getWorldTracker().isMaterial(x, y, z, state);
        }
    }
}
