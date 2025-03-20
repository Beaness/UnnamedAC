package me.beanes.acid.plugin.simulation;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.impl.velocity.TrackedVelocity;
import me.beanes.acid.plugin.simulation.data.*;
import me.beanes.acid.plugin.simulation.prepare.MotionAreaCreator;
import me.beanes.acid.plugin.simulation.prepare.WaterFlow;
import me.beanes.acid.plugin.simulation.processor.impl.*;
import me.beanes.acid.plugin.util.MCMath;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.ReleaseItemUtil;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;
import org.bukkit.Bukkit;


public class SimulationEngine {
    private final static double ACCURACY = 1E-9D;
    private final PlayerData data;
    // All the processors
    private final BlockPushProcessor blockPushProcessor;
    private final JumpProcessor jumpProcessor;

    @Getter private final LadderProcessor ladderProcessor;
    private final InputProcessor inputProcessor;
    @Getter
    private final WebProcessor webProcessor;
    private final SneakProcessor sneakProcessor;
    @Getter
    private final CollisionProcessor collisionProcessor;
    @Getter
    private final MotionAreaCreator creator;
    @Getter private final CollisionResult collisionResult = new CollisionResult(SplitStateBoolean.FALSE, SplitStateBoolean.FALSE, SplitStateBoolean.FALSE, 0, 0);
    @Getter
    private MotionAreaXZ hiddenSneakMotion = null;
    @Getter
    private MotionArea waterPush = new MotionArea(0, 0, 0, 0, 0, 0);

    @Getter
    private LiquidState liquidState;

    @Getter @Setter
    private MotionArea teleportUncertaintyLeniency;
    private MotionArea nextArea = new MotionArea(0, 0, 0, 0, 0, 0);
    private boolean canStayUsing;

    public SimulationEngine(PlayerData data) {
        this.data = data;
        this.blockPushProcessor = new BlockPushProcessor(data);
        this.jumpProcessor = new JumpProcessor(data);
        this.ladderProcessor = new LadderProcessor(data);
        this.inputProcessor = new InputProcessor(data);
        this.webProcessor = new WebProcessor(data);
        this.sneakProcessor = new SneakProcessor(data);
        this.collisionProcessor = new CollisionProcessor(data);
        this.creator = new MotionAreaCreator(data);
    }

    // TODO: use bed on player resets motions & teleports player

    public boolean attemptSimulation() {
        this.preSimulation();
        SimulationResult result = this.simulate();

        data.getCheckManager().onSimulate(result);

        this.postSimulation();
        return result.isCorrect();
    }

    private SimulationResult simulate() {
        TrackedVelocity latestVelocity = data.getVelocityTracker().getTrackedVelocity().getValue();
        TrackedVelocity oldVelocity = data.getVelocityTracker().getTrackedVelocity().getOldValue();

        boolean valid = false;
        boolean validJump = false;
        boolean validUsing = false;

        // Old velocity has not yet processed -> process
        if (oldVelocity != null && !oldVelocity.isProcessed()) {
            AreaResult result = processArea(new MotionArea(oldVelocity.getVelocity()));

            valid |= result.isValid();
            validJump |= result.isJumpValid();

            if (result.isValid() || result.isJumpValid()) {
                oldVelocity.setProcessed(true);
            }
        }

        // Latest velocity has not yet processed -> process
        if (!latestVelocity.isProcessed()) {
            AreaResult result = processArea(new MotionArea(latestVelocity.getVelocity()));

            valid |= result.isValid();
            validJump |= result.isJumpValid();

            if (result.isValid() || result.isJumpValid()) {
                latestVelocity.setProcessed(true);

                // If the player processed the new velocity correctly he might have skipped the old velocity (because it was accepted in the same tick!)
                if (oldVelocity != null) {
                    oldVelocity.setProcessed(true);
                }
            } else {
                if (latestVelocity.isConfirmed()) {
                    latestVelocity.setProcessed(true);
                }
            }
        }

        /*
            The player can run a no server velocity simulation if:
                - Old velocity has been calculated (because it had to be this tick!)
                - Latest velocity is not yet been confirmed or already has been processed
         */


        boolean canProcessNoVelocity = (oldVelocity == null || oldVelocity.isProcessed()) && (!latestVelocity.isConfirmed() || latestVelocity.isProcessed());

        if (canProcessNoVelocity) {
            AreaResult result = processArea(nextArea);

            valid |= result.isValid();
            validJump |= result.isJumpValid();
        }

        /* if (validJump) {
            if (valid) {
                Bukkit.broadcastMessage("possible jump");
            } else {
                Bukkit.broadcastMessage("def jump");
            }
        } */

        boolean correct = valid || validJump;

        // A lot of funky shit happens when the player is respawning (A LOT!) the netcode is fucked. exempt.
        if (data.getRespawnTracker().isPossibleRespawning()) {
            correct = true;
        }

        if (data.getUsingTracker().getUsing().possible()) {
            Bukkit.broadcastMessage("USING=" + data.getUsingTracker().getUsing());
        }

        if (!correct) {
            // System.out.println(data.getPositionTracker().getX() + " " + data.getPositionTracker().getY() + " " + data.getPositionTracker().getZ());
            // Bukkit.broadcastMessage("§csimulation fail!");
            // Bukkit.broadcastMessage(data.getUser().getName() + " §1Uncertain=§6" + data.getPositionTracker().isUncertain() + " §1lastUncertain=§6" + data.getPositionTracker().isLastUncertain() + " §clava=§e" + possibleLava + " §cwater=§e" + water.possible() + " §cnormal=§e" + possibleNormal + " §ctpValues=§e" + data.getPositionTracker().isTeleport() + " " + data.getPositionTracker().isLastTeleport() + " " + data.getPositionTracker().isLastLastTeleport());

            // data.getWorldTracker().getResyncHandler().scheduleResync();

            // Bukkit.shutdown();


            data.getSetbackTracker().setback();
        } else {
            data.getSetbackTracker().registerSafePositionIfPossible(data.getPositionTracker().getX(), data.getPositionTracker().getY(), data.getPositionTracker().getZ());
        }

        return new SimulationResult(
                correct,
                liquidState,
                !canProcessNoVelocity,
                validJump ? (valid ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE) : SplitStateBoolean.FALSE
        );
    }

    private void preSimulation() {
        // Set the liquid states
        this.checkLiquids();

        // Prepare the ladder state
        this.ladderProcessor.prepareLadderState();

        // Prepare the sneak state
        this.sneakProcessor.prepareSneakReduce();

        // Reset inertia & prepare the input processor
        this.creator.reset();
        this.inputProcessor.prepareInputMovement();

        // Reset collision states & sneak motion
        this.collisionResult.setCollidedX(SplitStateBoolean.FALSE);
        this.collisionResult.setCollidedY(SplitStateBoolean.FALSE);
        this.collisionResult.setCollidedZ(SplitStateBoolean.FALSE);
        this.collisionResult.setMinimalMotionY(0);
        this.hiddenSneakMotion = null;

        // Reset motion area if last tick was a teleport (because motion values get reset also on client)
        if (data.getPositionTracker().isLastTeleport()) {
            this.nextArea = new MotionArea(0, 0, 0, 0, 0, 0);
        }

        // Reset can stay using
        this.canStayUsing = false;
    }

    private void checkLiquids() {
        BoundingBox lavaBoxCheck = data.getPositionTracker().getLastBox().expand(-0.1F, -0.4F, -0.1F);
        BoundingBox waterBoxCheck = data.getPositionTracker().getLastBox().expand(0.0D, -0.4F, 0.0D).contract(0.001D, 0.001D, 0.001D);

        SplitStateBoolean waterState = handleWaterPush(waterBoxCheck);
        SplitStateBoolean lavaState = data.getWorldTracker().isMaterialInBB(lavaBoxCheck, StateTypes.LAVA);

        if (waterState == SplitStateBoolean.TRUE && !data.getPositionTracker().isLastUncertain()) {
            this.liquidState = new LiquidState(false, true, false);
            return;
        }

        if (lavaState == SplitStateBoolean.TRUE && !data.getPositionTracker().isLastUncertain()) {
            this.liquidState = new LiquidState(false, false, true);
            return;
        }

        this.liquidState = new LiquidState(true, waterState.possible(), lavaState.possible());
    }


    private void postSimulation() {
        this.nextArea = creator.create();
        if (!canStayUsing) {
            if (data.getUsingTracker().getUsing().possible()) {
                ReleaseItemUtil.releaseOnMainThread(data);
            }
            data.getUsingTracker().setUsing(SplitStateBoolean.FALSE);
        }
    }


    public AreaResult processArea(MotionArea startingArea) {
        System.out.println("ticks=" + data.getPositionTracker().getClientTicks() + " preArea=" + startingArea);

        if (data.getPositionTracker().isUncertain()) {
            System.out.println("(uncertain)");
        }

        // Add the water push
        if (data.getPositionTracker().isLastUncertain()) {
            startingArea.extend(waterPush);
        } else {
            startingArea.add(waterPush);
        }

        // The area gets reduced if the player is attacking
        if (data.getUsingTracker().getReducedByAttacking() == SplitStateBoolean.TRUE) {
            startingArea.minX = startingArea.minX * 0.6D;
            startingArea.maxX = startingArea.maxX * 0.6D;
            startingArea.minZ = startingArea.minZ * 0.6D;
            startingArea.maxZ = startingArea.maxZ * 0.6D;
        } else if (data.getUsingTracker().getReducedByAttacking() == SplitStateBoolean.POSSIBLE) {
            startingArea.allowX(startingArea.minX * 0.6D);
            startingArea.allowX(startingArea.maxX * 0.6D);
            startingArea.allowZ(startingArea.minZ * 0.6D);
            startingArea.allowZ(startingArea.maxZ * 0.6D);
        }

        System.out.println("postReduce=" + startingArea);

        // Push out of blocks if possible
        this.blockPushProcessor.processArea(startingArea);

        // Very small motions are set to 0 on tick start
        this.minimizeSmallMotion(startingArea);

        // Read more inside jump processor why we split it
        MotionArea jumpArea = jumpProcessor.wasJumpPossible() ? startingArea.copy() : null;

        if (jumpArea != null) {
            jumpProcessor.process(jumpArea);
        }
        System.out.println("preInput=" + startingArea);

        boolean valid = processAreaWithInputs(startingArea);
        boolean jumpValid = jumpArea != null && processAreaWithInputs(jumpArea);

        return new AreaResult(valid, jumpValid);
    }

    private boolean processAreaWithInputs(MotionArea startingArea) {
        boolean valid = false;

        System.out.println("preInput=" + startingArea);

        for (MotionArea area : inputProcessor.process(startingArea)) {
            System.out.println("afterInput=" + area);

            // First run ladder processor if we are possible not in liquid
            if (liquidState.isNormalPossible()) {
                ladderProcessor.process(area);
            }

            webProcessor.processArea(area);

            // Sneaking can reduce the motion area but does not actually change the motion for the next tick
            // The sneak processor returns either null or the original motion possible (depending if sneaking modified the motion)
            MotionAreaXZ originalMotion = sneakProcessor.process(area);

            System.out.println("preCollision=" + area);

            // Stepping processor
            CollisionResult collision = collisionProcessor.process(area);

            if (checkValidArea(area, collision.getCollidedY())) {
                valid = true;
                canStayUsing |= area.using;

                // Collision result priority: POSSIBLE -> TRUE -> FALSE TODO: if true is an option we gotta fix this
                if (collision.getCollidedX().possible() && collisionResult.getCollidedX() != SplitStateBoolean.POSSIBLE) {
                    collisionResult.setCollidedX(collision.getCollidedX());
                }

                if (collision.getCollidedY().possible() && collisionResult.getCollidedY() != SplitStateBoolean.POSSIBLE) {
                    collisionResult.setCollidedY(collision.getCollidedY());
                }

                if (collision.getCollidedZ().possible() && collisionResult.getCollidedZ() != SplitStateBoolean.POSSIBLE) {
                    collisionResult.setCollidedZ(collision.getCollidedZ());
                }

                collisionResult.setMinimalMotionY(Math.min(collision.getMinimalMotionY(), collisionResult.getMinimalMotionY()));
                collisionResult.setMaximumMotionY(Math.max(collision.getMaximumMotionY(), collisionResult.getMaximumMotionY()));

                if (originalMotion != null) {
                   if (hiddenSneakMotion != null) {
                       hiddenSneakMotion.minX = Math.min(hiddenSneakMotion.minX, originalMotion.minX);
                       hiddenSneakMotion.minZ = Math.min(hiddenSneakMotion.minZ, originalMotion.minZ);
                       hiddenSneakMotion.maxX = Math.max(hiddenSneakMotion.maxX, originalMotion.maxX);
                       hiddenSneakMotion.maxZ = Math.max(hiddenSneakMotion.maxZ, originalMotion.maxZ);
                   } else {
                       hiddenSneakMotion = originalMotion;
                   }
                }
            }
        }

        return valid;
    }

    private boolean checkValidArea(MotionArea area, SplitStateBoolean collidedY) {
        if (data.getPositionTracker().isLastUncertain()) {
            // Allow for a way higher leniency when the the uncertainty was just after a teleport (this means the player could have a way higher delta)
            if (data.getPositionTracker().isLastUncertainTeleportation() && !data.getPositionTracker().isLastUncertainTeleportationSetback()) { // Don't give expansion for setback teleport uncertainty
                area.extend(teleportUncertaintyLeniency); // We might have to give this bounds to prevent dumb 0.03 uncertainty bypasses
            } else {
                area.expand(0.03D);
            }
        }

        area.expand(ACCURACY);

        boolean checkGround;

        if (data.getPositionTracker().isOnGround()) {
            checkGround = collidedY.possible() && area.minY < 0.0D;
        } else {
            checkGround = collidedY != SplitStateBoolean.TRUE || area.minY > 0.0D;
        }

        // I think this calculation is right, this checks if 0.03 was possible
        if (data.getPositionTracker().isUncertain() && checkGround && isPossibleUncertain(area)) {
            return true;
        }

        double deltaX = data.getPositionTracker().getDeltaX();
        double deltaY = data.getPositionTracker().getDeltaY();
        double deltaZ = data.getPositionTracker().getDeltaZ();

        boolean checkX = deltaX <= area.maxX && deltaX >= area.minX;
        boolean checkY = deltaY <= area.maxY && deltaY >= area.minY;
        boolean checkZ = deltaZ <= area.maxZ && deltaZ >= area.minZ;

        System.out.println("area=" + area);
        System.out.println("checkX=" + checkX + " checkY=" + checkY + " checkZ=" + checkZ + " checkGround=" + checkGround + " possibleUncertain=" + isPossibleUncertain(area));

        return checkX && checkY && checkZ && checkGround;
    }

    private boolean isPossibleUncertain(MotionArea area) {
        if (data.getPositionTracker().isUncertainTeleportation() && !data.getPositionTracker().isUncertainTeleportationSetback()) { // Dont allow teleport uncertainty on an anticheat setback
            // Thanks 0.03 uncertainty + teleports (check MotionAreaCreator for the explanation)!!
            // We have to check if the player is able to get in the range of 0.03 of lastReportedPos with the area movement

            // TODO: technically this isnt correct as its a 0.03 box all directions when its distance for 0.03 check

            double offsetX = data.getPositionTracker().getLastReportedX() - data.getPositionTracker().getX();
            double offsetY = data.getPositionTracker().getLastReportedY() - data.getPositionTracker().getY();
            double offsetZ = data.getPositionTracker().getLastReportedZ() - data.getPositionTracker().getZ();

            // We create an special made  0.03 area as its not normal 0.03 (due to teleport)
            MotionArea allowed = new MotionArea(offsetX - 0.03D, offsetY - 0.03D, offsetZ - 0.03D, offsetX + 0.03D, offsetY + 0.03D, offsetZ + 0.03D);

            // We check if the motion area is colliding with the special made 0.03 area, if it does collide it means 0.03 is allowed
            return (area.minX < allowed.maxX) && (area.maxX > allowed.minX) &&
                    (area.minY < allowed.maxY) && (area.maxY > allowed.minY) &&
                    (area.minZ < allowed.maxZ) && (area.maxZ > allowed.minZ);
        } else {
            double minimalX = (area.minX <= 0 && 0 <= area.maxX) ? 0 : Math.min(Math.abs(area.minX), Math.abs(area.maxX));
            double minimalY = (area.minY <= 0 && 0 <= area.maxY) ? 0 : Math.min(Math.abs(area.minY), Math.abs(area.maxY));
            double minimalZ = (area.minZ <= 0 && 0 <= area.maxZ) ? 0 : Math.min(Math.abs(area.minZ), Math.abs(area.maxZ));

            return minimalX * minimalX + minimalY * minimalY + minimalZ * minimalZ <= 9.0E-4D;
        }
    }

    private void minimizeSmallMotion(MotionArea area) {
        if (Math.abs(area.minX) < 0.005D) {
            area.minX = 0.0D;
        }

        if (Math.abs(area.minY) < 0.005D) {
            area.minY = 0.0D;
        }

        if (Math.abs(area.minZ) < 0.005D) {
            area.minZ = 0.0D;
        }

        if (Math.abs(area.maxX) < 0.005D) {
            area.maxX = 0.0D;
        }

        if (Math.abs(area.maxY) < 0.005D) {
            area.maxY = 0.0D;
        }

        if (Math.abs(area.maxZ) < 0.005D) {
            area.maxZ = 0.0D;
        }
    }

    public SplitStateBoolean handleWaterPush(BoundingBox box) {
        int minX = MCMath.floor_double(box.getMinX());
        int maxX = MCMath.floor_double(box.getMaxX() + 1.0D);
        int minY = MCMath.floor_double(box.getMinY());
        int maxY = MCMath.floor_double(box.getMaxY() + 1.0D);
        int minZ = MCMath.floor_double(box.getMinZ());
        int maxZ = MCMath.floor_double(box.getMaxZ() + 1.0D);

        boolean flag = false;
        Boolean oldFlag = null;

        MotionArea area = new MotionArea(0, 0, 0, 0, 0, 0);

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

                    WrappedBlockState latest = splitState.getValue();
                    WrappedBlockState old = splitState.getOldValue();

                    MotionArea best = new MotionArea();
                    boolean canAdd = false;

                    if (latest.getType() == StateTypes.WATER) {
                        double latestValue = (float) (y + 1) - WaterFlow.getLiquidHeightPercent(latest.getLevel());

                        if ((double)maxY >= latestValue) {
                            flag = true;

                            MotionArea flow = WaterFlow.getFlowArea(data, x, y, z, latest);

                            best.allowX(flow.minX);
                            best.allowX(flow.maxX);
                            best.allowY(flow.minY);
                            best.allowY(flow.maxY);
                            best.allowZ(flow.minZ);
                            best.allowZ(flow.maxZ);
                            canAdd = true;
                        }
                    }

                    if (old != null && old.getType() == StateTypes.WATER) {
                        double latestValue = (float) (y + 1) - WaterFlow.getLiquidHeightPercent(old.getLevel());

                        if ((double)maxY >= latestValue) {
                            oldFlag = true;

                            MotionArea flow = WaterFlow.getFlowArea(data, x, y, z, old);

                            best.allowX(flow.minX);
                            best.allowX(flow.maxX);
                            best.allowY(flow.minY);
                            best.allowY(flow.maxY);
                            best.allowZ(flow.minZ);
                            best.allowZ(flow.maxZ);

                            canAdd = true;
                        } else {
                            oldFlag = false;

                            best.allowX(0);
                            best.allowY(0);
                            best.allowZ(0);
                        }
                    }

                    if (canAdd) {
                        if (data.getPositionTracker().isLastUncertain()) {
                            area.extend(best);
                        } else {
                            area.add(best);
                        }
                    }
                }
            }
        }

        double maximumX = Math.max(Math.abs(area.minX), Math.abs(area.maxX));
        double maximumY = Math.max(Math.abs(area.minY), Math.abs(area.maxY));
        double maximumZ = Math.max(Math.abs(area.minZ), Math.abs(area.maxZ));

        double maxLength = MCMath.sqrt_double(maximumX * maximumX + maximumY * maximumY + maximumZ * maximumZ);

        if (maxLength > 0.0D) {
            waterPush = area.createNormalisedArea();

            waterPush.minX = waterPush.minX * 0.014D;
            waterPush.minY = waterPush.minY * 0.014D;
            waterPush.minZ = waterPush.minZ * 0.014D;
            waterPush.maxX = waterPush.maxX * 0.014D;
            waterPush.maxY = waterPush.maxY * 0.014D;
            waterPush.maxZ = waterPush.maxZ * 0.014D;
        } else {
            waterPush = new MotionArea(0, 0, 0, 0, 0, 0);
        }

        // Either we are 100% sure of our old data, or our old data is the same as our new data
        if (oldFlag == null || flag == oldFlag) {
            return flag ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        }

        // Old & new state are not the same -> both are possible
        return SplitStateBoolean.POSSIBLE;
    }
}
