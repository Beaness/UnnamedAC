package me.beanes.acid.plugin.player.tracker.impl.world;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_8.Chunk_v1_8;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.ItemTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import it.unimi.dsi.fastutil.longs.*;
import lombok.Getter;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.command.debug.impl.ResyncCommand;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.player.tracker.Tracker;
import me.beanes.acid.plugin.player.tracker.impl.world.resync.WorldResyncHandler;
import me.beanes.acid.plugin.util.*;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;
import org.bukkit.Bukkit;

import java.util.*;

public class WorldTracker extends Tracker {
    public WorldTracker(PlayerData data) {
        super(data);
    }

    private final Long2ObjectMap<ChunkData> chunks = new Long2ObjectOpenHashMap<>();
    private final Long2IntMap preTransactions = new Long2IntArrayMap();
    private final Deque<BlockPlace> queuedBlockPlaces = new ArrayDeque<>(); // We have to queue them until the client sends the flying packet as yaw & pitch are not up to date the moment the client sends out the block place packet
    @Getter
    private final WorldResyncHandler resyncHandler = new WorldResyncHandler(data);

    public void debug() {
        for (Long2ObjectMap.Entry<ChunkData> entry : chunks.long2ObjectEntrySet()) {
            long chunkXZ = entry.getLongKey();

            int x = getX(chunkXZ);
            int z = getZ(chunkXZ);

            Column column = new Column(x, z, true, entry.getValue().getSections(), new TileEntity[]{});

            WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(column);

            data.getUser().sendPacket(wrapper);
        }
    }

    public void handleBlockPlace(WrapperPlayClientPlayerBlockPlacement wrapper) {
        if (data.getStateTracker().getGameMode().getValue() == GameMode.ADVENTURE) { // TODO: support block nbt tags where you can place and break
            return;
        }

        BlockFace face = wrapper.getFace();
        ItemStack itemStack = wrapper.getItemStack().orElse(null);

        if (itemStack == null) {
            return;
        }

        Vector3i blockPosition = wrapper.getBlockPosition();
        Vector3f cursor = wrapper.getCursorPosition();

        BlockPlace blockPlace = new BlockPlace(blockPosition, face, cursor, itemStack);
        queuedBlockPlaces.add(blockPlace);
    }

    public void onClientTick() {
        // Try catch to make sure we dont infinite loop on errors at world simulation
        try {
            for (BlockPlace blockPlace : queuedBlockPlaces) {
                processBlockPlace(blockPlace);
            }

            queuedBlockPlaces.clear();
        } catch (Exception ex) {
            throw ex; // TODO: report to cloud!
        }
    }

    // self todo: water, lily pad, slab

    private void processBlockPlace(BlockPlace blockPlace) {
        Vector3i blockPos = blockPlace.getBlockPosition();
        SplitState<WrappedBlockState> hitSplitState = getBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        boolean canActivate = !data.getActionTracker().isSneaking() || blockPlace.getItemStack() == null;

        SplitStateBoolean activated = SplitStateBoolean.FALSE;
        if (canActivate) {
            SplitStateBoolean latestActivation = Acid.get().getBlockManager().onActivate(data, blockPos.getX(), blockPos.getY(), blockPos.getZ(), hitSplitState.getValue(), true);
            SplitStateBoolean oldActivation = hitSplitState.getOldValue() != null ? Acid.get().getBlockManager().onActivate(data, blockPos.getX(), blockPos.getY(), blockPos.getZ(), hitSplitState.getOldValue(), false) : latestActivation;

            activated = SplitStateBoolean.result(latestActivation, oldActivation);
        }

        if (activated.possible()) {
            // Server should respond with an update but to be sure lets resync around the activated possible
            resyncHandler.scheduleResync(blockPos.getX() - 1, blockPos.getY() - 1, blockPos.getZ() - 1, blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
            return;
        }

        ItemStack stack = blockPlace.getItemStack();

        if (stack == null || stack.isEmpty()) {
            return;
        }

        // These are not clientside simulated for placement, yay! we can just wait for server
        ItemType type = stack.getType();
        if (ItemTags.SIGNS.contains(type) || ItemTags.SKULLS.contains(type) || ItemTags.BANNERS.contains(type)) {
            return;
        }

        BlockFace face = blockPlace.getFace();

        // Cocoa custom code path
        if (type == ItemTypes.INK_SAC && stack.getLegacyData() == 3) {
            CustomBlockPlaces.placeCocoa(data, face, blockPos.getX(), blockPos.getY(), blockPos.getZ());
            return;
        }

        if (ItemTags.SLABS.contains(type) && CustomBlockPlaces.placeSlab(data, face, type.getPlacedType(), blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
            return;
        }

        if (type == ItemTypes.SNOW && CustomBlockPlaces.placeSnow(data, face, blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
            return;
        }

        if (type == ItemTypes.LILY_PAD) {
            if (face == BlockFace.OTHER) {
                CustomBlockPlaces.rayTraceLily(data);
                return;
            }
        }

        if (type == ItemTypes.BUCKET) {
            if (face == BlockFace.OTHER) {
                CustomBlockPlaces.rayTraceEmptyBucket(data);
                return;
            }

            return;
        }

        if (type == ItemTypes.WATER_BUCKET || type == ItemTypes.LAVA_BUCKET) {
            if (face == BlockFace.OTHER) {
                CustomBlockPlaces.rayTraceFullBucket(data, type == ItemTypes.WATER_BUCKET ? StateTypes.WATER : StateTypes.LAVA);
                return;
            }

            return; // Do not process any other face than OTHER as its raytraced
        }

        // All blocks under here can't be placed with face other
        if (face == BlockFace.OTHER) {
            return;
        }

        StateType stateType = type.getPlacedType();

        if (stateType == null) {
            return;
        }

        WrappedBlockState hitBlock = hitSplitState.getValue();
        boolean replaceable = BlockUtil.isBlockReplaceable(hitBlock);

        // Snow placement doesnt always move the blockpos to the offset
        boolean snowException = stateType != StateTypes.SNOW || face != BlockFace.UP || hitBlock.getType() != StateTypes.SNOW || (hitBlock.getType() == StateTypes.SNOW && hitBlock.getLayers() >= 8);

        if (!replaceable && snowException) {
            blockPos = blockPos.offset(face);
        }

        if (ItemTags.DOORS.contains(type) && !CustomBlockPlaces.canPlaceDoor(data, face, blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
            return;
        }

        if (type == ItemTypes.FLOWER_POT && !CustomBlockPlaces.canPlaceFlowerPot(data, blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
            return;
        }

        if (type == ItemTypes.REDSTONE && !CustomBlockPlaces.canPlaceRedstone(data, blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
            return;
        }

        if (ItemTags.RAILS.contains(type) && !CustomBlockPlaces.canPlaceRail(data, blockPos.getX(), blockPos.getY(), blockPos.getZ())) {
            return;
        }

        WrappedBlockState createdState = WrappedBlockState.getDefaultState(data.getUser().getClientVersion(), stateType);

        // PacketEvents funky torches :(
        if (face != BlockFace.UP) {
            if (type == ItemTypes.TORCH) {
                createdState = WrappedBlockState.getDefaultState(data.getUser().getClientVersion(), StateTypes.WALL_TORCH);
            } else if (type == ItemTypes.REDSTONE_TORCH) {
                createdState = WrappedBlockState.getDefaultState(data.getUser().getClientVersion(), StateTypes.REDSTONE_WALL_TORCH);
            }
        }

        /* for (Map.Entry<StateValue, Object> entry : createdState.getInternalData().entrySet()) {
            Bukkit.broadcastMessage(entry.getKey().getName() + ": §b" + entry.getValue());
        } */

        Acid.get().getBlockManager().onPlace(data, blockPos.getX(), blockPos.getY(), blockPos.getZ(), face, blockPlace.getCursorPosition(), stack.getLegacyData(), createdState);

        /* for (Map.Entry<StateValue, Object> entry : createdState.getInternalData().entrySet()) {
            Bukkit.broadcastMessage(entry.getKey().getName() + ": §a" + entry.getValue());
        } */

        // "Hack" to prevent invalid snow block placements
        if (createdState.getType() == StateTypes.SNOW && createdState.getLayers() == -1) {
            return;
        }

        setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ(), createdState);

        data.getInventoryTracker().removeOneItemFromHeldItemIfNotCreative();
    }

    public void handleDigging(WrapperPlayClientPlayerDigging wrapper) {
        Vector3i blockPos = wrapper.getBlockPosition();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        // TODO: left clicking cake -> eats, dragon egg -> to air, redstone ore -> lit redstone ore,

        // Bukkit.broadcastMessage("action=" + wrapper.getAction());

        if (wrapper.getAction() == DiggingAction.FINISHED_DIGGING) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

            if (BlockUtil.isBreakable(splitState.getValue().getType())) {
                data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
            }

            if (splitState.getOldValue() != null && BlockUtil.isBreakable(splitState.getOldValue().getType())) {
                data.getWorldTracker().setOldBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
            }
        }

        if (wrapper.getAction() == DiggingAction.START_DIGGING) {
            SplitState<WrappedBlockState> splitState = data.getWorldTracker().getBlock(x, y, z);

            // If 100% certain -> insta break
            if (data.getStateTracker().isCreative() == SplitStateBoolean.TRUE) {
                data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
                data.getWorldTracker().setOldBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
                return;
            }

            boolean instaBreak = false;
            boolean takesTime = false;

            for (float speed : BreakSpeedUtil.getBreakSpeeds(data, splitState.getValue().getType())) {
                if (speed >= 1.0F) {
                    data.getWorldTracker().setBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
                    instaBreak = true;
                } else {
                    takesTime = true;
                }
            }

            if (splitState.getOldValue() != null) {
                for (float speed : BreakSpeedUtil.getBreakSpeeds(data, splitState.getOldValue().getType())) {
                    if (speed >= 1.0F) {
                        data.getWorldTracker().setOldBlock(x, y, z, WrappedBlockState.getDefaultState(StateTypes.AIR));
                        instaBreak = true;
                    } else {
                        takesTime = true;
                    }
                }
            }

            if (instaBreak && takesTime) {
                resyncHandler.scheduleResync(x, y, z);
            }
        }
    }

    public void handleChunk(WrapperPlayServerChunkData wrapper) {
        long xz = encodeToLong(wrapper.getColumn().getX(), wrapper.getColumn().getZ());
        Column column = wrapper.getColumn();
        boolean fullChunk = column.isFullChunk();
        BaseChunk[] sections = column.getChunks();

        if (preTransactions.get(xz) == data.getTransactionTracker().getLastTransactionSent()) {
            data.getTransactionTracker().sendTransaction();
        }

        preTransactions.put(xz, data.getTransactionTracker().getLastTransactionSent());

        boolean noChunkDataSent = true;

        if (fullChunk) {
            for (BaseChunk baseChunk : sections) {
                if (baseChunk != null) {
                    noChunkDataSent = false;
                    break;
                }
            }
        }

        boolean unload = fullChunk && noChunkDataSent;

        data.getTransactionTracker().pre(() -> {
            ChunkData chunkData = chunks.get(xz);

            if (chunkData == null) {
                if (!fullChunk) {
                    throw new IllegalStateException("Useless chunk data being sent");
                }

                chunks.put(xz, new ChunkData(sections));
            } else {
                if (unload) {
                    chunkData.setConfirmed(false);
                } else {
                    chunkData.overwriteSections(sections);
                }
            }
        });

        data.getTransactionTracker().post(() -> {
            if (unload) {
                chunks.remove(xz);
                preTransactions.remove(xz);
                return;
            }

            ChunkData chunkData = chunks.get(xz);
            chunkData.setConfirmed(true);
            chunkData.confirmSections();
        });
    }

    public void handleChunkBulk(WrapperPlayServerChunkDataBulk wrapper) {
        int[] xList = wrapper.getX();
        int[] zList = wrapper.getZ();
        long[] xzs = new long[xList.length];

        BaseChunk[][] sectionsList = wrapper.getChunks();

        for (int i = 0; i < xzs.length; i++) {
            int x = xList[i];
            int z = zList[i];
            long xz = encodeToLong(x, z);

            xzs[i] = xz;

            if (preTransactions.get(xz) == data.getTransactionTracker().getLastTransactionSent()) {
                data.getTransactionTracker().sendTransaction();
            }

            preTransactions.put(xz, data.getTransactionTracker().getLastTransactionSent());
        }

        data.getTransactionTracker().pre(() -> {
            for (int i = 0; i < xzs.length; i++) {
                long xz = xzs[i];

                ChunkData chunkData = chunks.get(xz);

                if (chunkData == null) {
                    chunks.put(xz, new ChunkData(sectionsList[i]));
                } else {
                    chunkData.overwriteSections(sectionsList[i]);
                }
            }
        });

        data.getTransactionTracker().post(() -> {
            for (long xz : xzs) {
                ChunkData chunkData = chunks.get(xz);

                chunkData.setConfirmed(true);
                chunkData.confirmSections();
            }
        });
    }

    // TODO: Handle falling block setting a block toa ir on first tick
    public void handleBlockChange(WrapperPlayServerBlockChange wrapper) {
        int x = wrapper.getBlockPosition().getX();
        int y = wrapper.getBlockPosition().getY();
        int z = wrapper.getBlockPosition().getZ();
        int blockId = wrapper.getBlockId();

        processBlockChange(x, y, z, blockId);

        // Update the transaction of the block change
        long xz = encodeToLong(x >> 4, z >> 4);
        preTransactions.put(xz, data.getTransactionTracker().getLastTransactionSent());
    }

    public void handleMultiBlockChange(WrapperPlayServerMultiBlockChange wrapper) {
        WrapperPlayServerMultiBlockChange.EncodedBlock[] changes = wrapper.getBlocks();

        // We make a unique set of all the possible chunks (if we checked the pre trans per block it would just cause itself to spam transaction)
        // Then we check the unique set chunks on eby one

        LongArraySet xzs = new LongArraySet();
        for (WrapperPlayServerMultiBlockChange.EncodedBlock block : changes) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            int blockId = block.getBlockId();

            processBlockChange(x, y, z, blockId);

            long xz = encodeToLong(x >> 4, z >> 4);
            xzs.add(xz);
        }

        // Update the transactions after the block changes got processed, if we updated it inside the block change processing it would cause transaction spams
        // This is due to multiple block changes in the same chunk will trigger the last trans to be updated
        for (long xz : xzs) {
            preTransactions.put(xz, data.getTransactionTracker().getLastTransactionSent());
        }
    }

    public void handleExplosion(WrapperPlayServerExplosion explosion) {
        LongArraySet xzs = new LongArraySet();

        for (Vector3i pos : explosion.getRecords()) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            processBlockChange(x, y, z, 0);

            long xz = encodeToLong(x >> 4, z >> 4);
            xzs.add(xz);
        }

        for (long xz : xzs) {
            preTransactions.put(xz, data.getTransactionTracker().getLastTransactionSent());
        }
    }


    public void processBlockChange(int x, int y, int z, int blockId) {
        // Temporarily disable for clientside testing
        if (ResyncCommand.DEBUG_WORLD) {
            return;
        }

        long xz = encodeToLong(x >> 4, z >> 4);
        int ySection = y >> 4;
        int xInSection = x & 15;
        int yInSection = y & 15;
        int zInSection = z & 15;
        short compactBlockPosInChunk = (short) (y << 8 | zInSection << 4 | xInSection); // Compacted block pos

        long blockHash = Objects.hash(x, y, z); // There's probs a better way to hash 3 integers but it'll do for now
        int lastTrans = data.getTransactionTracker().getLastTransactionSent();
        if (preTransactions.get(blockHash) == lastTrans || preTransactions.get(xz) == data.getTransactionTracker().getLastTransactionSent()) { // Use same preTransactions map as for chunks
            // A block update happened in the same transaction sandwich, let's send another transaction
            data.getTransactionTracker().sendTransaction();
        }

        preTransactions.put(blockHash, data.getTransactionTracker().getLastTransactionSent());

        data.getTransactionTracker().pre(() -> {
            ChunkData chunkData = chunks.get(xz);

            if (chunkData == null) {
                return;
            }

            if (chunkData.getSections()[ySection] == null) {
                chunkData.getSections()[ySection] = new Chunk_v1_8(false);
            }

            chunkData.storeOldBlock(compactBlockPosInChunk, chunkData.getSections()[ySection].getBlockId(xInSection, yInSection, zInSection));
            chunkData.getSections()[ySection].set(xInSection, yInSection, zInSection, blockId);
        });

        data.getTransactionTracker().post(() -> {
            ChunkData chunkData = chunks.get(xz);

            if (chunkData == null) {
                return;
            }

            chunkData.confirmOldBlock(compactBlockPosInChunk);

            preTransactions.remove(blockHash);
        });
    }

    public SplitState<WrappedBlockState> getBlock(int x, int y, int z) {
        if (y > 256 || y < 0) {
            return new SplitState<>(WrappedBlockState.getByGlobalId(0), null);
        }

        long xz = encodeToLong(x >> 4, z >> 4);
        ChunkData chunkData = chunks.get(xz);

        if (chunkData == null) {
            return new SplitState<>(WrappedBlockState.getByGlobalId(0), null);
        }

        return chunkData.getBlock(data.getUser().getClientVersion(), x & 15, y, z & 15);
    }

    public void setBlock(int x, int y, int z, WrappedBlockState state) {
        long xz = encodeToLong(x >> 4, z >> 4);
        ChunkData chunkData = chunks.get(xz);

        // I think mc does nothing here, might be wrong though
        if (chunkData == null) {
            return;
        }

        chunkData.setBock(x & 15, y, z & 15, state);
    }

    public void setOldBlock(int x, int y, int z, WrappedBlockState state) {
        long xz = encodeToLong(x >> 4, z >> 4);
        ChunkData chunkData = chunks.get(xz);

        // I think mc does nothing here, might be wrong though
        if (chunkData == null) {
            return;
        }

        chunkData.setOldBock(x & 15, y, z & 15, state);
    }

    public SplitStateBoolean isMaterial(int x, int y, int z, StateType type) {
        SplitState<WrappedBlockState> state = getBlock(x, y, z);

        if (state.getValue().getType() == type) {
            if (state.getOldValue() != null) {
                return state.getOldValue().getType() == type ? SplitStateBoolean.TRUE : SplitStateBoolean.POSSIBLE;
            } else {
                return SplitStateBoolean.TRUE;
            }
        } else {
            if (state.getOldValue() != null) {
                return state.getOldValue().getType() == type ? SplitStateBoolean.POSSIBLE : SplitStateBoolean.FALSE;
            } else {
                return SplitStateBoolean.FALSE;
            }
        }
    }

    // The following methods can't be used with consumeBlocksinBB because java lambdas dont want reference to non final vars :(
    public SplitStateBoolean isMaterialInBB(BoundingBox box, StateType ...types) {
        int minX = MCMath.floor_double(box.getMinX());
        int minY = MCMath.floor_double(box.getMinY());
        int minZ = MCMath.floor_double(box.getMinZ());
        int maxX = MCMath.floor_double(box.getMaxX());
        int maxY = MCMath.floor_double(box.getMaxY());
        int maxZ = MCMath.floor_double(box.getMaxZ());

        boolean isIn = false;
        Boolean isOldIn = null;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {

                    SplitState<WrappedBlockState> state = getBlock(x, y, z);

                    for (StateType type : types) {
                        if (state.getValue().getType() == type) {
                            isIn = true;
                        }

                        if (state.getOldValue() != null) {
                            if (state.getOldValue().getType() == type) {
                                isOldIn = true;
                            } else if (isOldIn == null) { // It's possible the old value was not in
                                isOldIn = false;
                            }
                        }
                    }
                }
            }
        }

        // Either we are 100% sure of our old data, or our old data is the same as our new data
        if (isOldIn == null || isOldIn == isIn) {
            return isIn ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        }

        // Old & new state are not the same -> both are possible
        return SplitStateBoolean.POSSIBLE;
    }

    public boolean isPossibleNotLiquid(BoundingBox box) {
        int minX = MCMath.floor_double(box.getMinX());
        int maxX = MCMath.floor_double(box.getMaxX());
        int minY = MCMath.floor_double(box.getMinY());
        int maxY = MCMath.floor_double(box.getMaxY());
        int minZ = MCMath.floor_double(box.getMinZ());
        int maxZ = MCMath.floor_double(box.getMaxZ());

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    SplitState<WrappedBlockState> state = getBlock(x, y, z);

                    if (state.getValue().getType() != StateTypes.LAVA && state.getValue().getType() != StateTypes.WATER) {
                        return true;
                    }

                    if (state.getOldValue() != null) {
                        if (state.getOldValue().getType() != StateTypes.LAVA && state.getOldValue().getType() != StateTypes.WATER) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public SplitStateBoolean isChunkLoadedAtXZ(int x, int z) {
        long xz = encodeToLong(x >> 4, z >> 4);

        ChunkData data = chunks.get(xz);

        if (data == null) {
            return SplitStateBoolean.FALSE;
        }

        return data.isConfirmed() ? SplitStateBoolean.TRUE : SplitStateBoolean.POSSIBLE;
    }

    public BlockRayHit rayTraceBlocks(Vector3d start, Vector3d end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (Double.isNaN(start.getX()) || Double.isNaN(start.getY()) || Double.isNaN(start.getZ())) {
            return null;
        }

        if (Double.isNaN(end.getX()) || Double.isNaN(end.getY()) || Double.isNaN(end.getZ())) {
            return null;
        }

        int endX = MCMath.floor_double(end.getX());
        int endY = MCMath.floor_double(end.getY());
        int endZ = MCMath.floor_double(end.getZ());
        int startX = MCMath.floor_double(start.getX());
        int startY = MCMath.floor_double(start.getY());
        int startZ = MCMath.floor_double(start.getZ());

        WrappedBlockState state = getBlock(startX, startY, startZ).getValue();

        if ((!ignoreBlockWithoutBoundingBox || Acid.get().getBlockManager().getCollisionBox(data, startX, startY, startZ, state) != null) && BlockUtil.canCollide(state, stopOnLiquid)) {
            BlockRayHit blockRayHit = getRayTrace(startX, startY, startZ, state, start, end);

            if (blockRayHit != null) {
                return blockRayHit;
            }
        }

        BlockRayHit movingobjectposition2 = null;
        int maxIterations = 200;

        while (maxIterations-- >= 0) {
            if (startX == endX && startY == endY && startZ == endZ) {
                return returnLastUncollidableBlock ? movingobjectposition2 : null;
            }

            boolean flag2 = true;
            boolean flag = true;
            boolean flag1 = true;
            double d0 = 999.0D;
            double d1 = 999.0D;
            double d2 = 999.0D;

            if (endX > startX) {
                d0 = (double) startX + 1.0D;
            } else if (endX < startX) {
                d0 = (double) startX + 0.0D;
            } else {
                flag2 = false;
            }

            if (endY > startY) {
                d1 = (double) startY + 1.0D;
            } else if (endY < startY) {
                d1 = (double) startY + 0.0D;
            } else {
                flag = false;
            }

            if (endZ > startZ) {
                d2 = (double) startZ + 1.0D;
            } else if (endZ < startZ) {
                d2 = (double) startZ + 0.0D;
            } else {
                flag1 = false;
            }

            double d3 = 999.0D;
            double d4 = 999.0D;
            double d5 = 999.0D;
            double d6 = end.getX() - start.getX();
            double d7 = end.getY() - start.getY();
            double d8 = end.getZ() - start.getZ();

            if (flag2) {
                d3 = (d0 - start.getX()) / d6;
            }

            if (flag) {
                d4 = (d1 - start.getY()) / d7;
            }

            if (flag1) {
                d5 = (d2 - start.getZ()) / d8;
            }

            if (d3 == -0.0D) {
                d3 = -1.0E-4D;
            }

            if (d4 == -0.0D) {
                d4 = -1.0E-4D;
            }

            if (d5 == -0.0D) {
                d5 = -1.0E-4D;
            }

            BlockFace enumfacing;

            if (d3 < d4 && d3 < d5) {
                enumfacing = endX > startX ? BlockFace.WEST : BlockFace.EAST;
                start = new Vector3d(d0, start.getY() + d7 * d3, start.getZ() + d8 * d3);
            } else if (d4 < d5) {
                enumfacing = endY > startY ? BlockFace.DOWN : BlockFace.UP;
                start = new Vector3d(start.getX() + d6 * d4, d1, start.getZ() + d8 * d4);
            } else {
                enumfacing = endZ > startZ ? BlockFace.NORTH : BlockFace.SOUTH;
                start = new Vector3d(start.getX() + d6 * d5, start.getY() + d7 * d5, d2);
            }

            startX = MCMath.floor_double(start.getX()) - (enumfacing == BlockFace.EAST ? 1 : 0);
            startY = MCMath.floor_double(start.getY()) - (enumfacing == BlockFace.UP ? 1 : 0);
            startZ = MCMath.floor_double(start.getZ()) - (enumfacing == BlockFace.SOUTH ? 1 : 0);

            WrappedBlockState block = getBlock(startX, startY, startZ).getValue();

            if (!ignoreBlockWithoutBoundingBox || Acid.get().getBlockManager().getCollisionBox(data, startX, startY, startZ, block) != null) {
                if (BlockUtil.canCollide(block, stopOnLiquid)) {
                    BlockRayHit blockRayHit = getRayTrace(startX, startY, startZ, block, start, end);

                    if (blockRayHit != null) {
                        return blockRayHit;
                    }
                } else {
                    movingobjectposition2 = new BlockRayHit(startX, startY, startZ, null, null);
                }
            }
        }

        return returnLastUncollidableBlock ? movingobjectposition2 : null;
    }

    public BlockRayHit getRayTrace(int x, int y, int z, WrappedBlockState state, Vector3d start, Vector3d end) {
        // TODO: custom raytrace for stairs for some reason (which does a binary search??)
        BoundingBox box = Acid.get().getBlockManager().getBoundingBoxForRaytrace(data, x, y, z, state);

        start = start.add(-x, -y, -z);
        end = end.add(-x, -y, -z);

        InterceptData interceptData = box.calculateIntercept(start, end);

        if (interceptData == null) {
            return null;
        }

        return new BlockRayHit(x, y, z, interceptData.getVector().add(x, y, z), interceptData.getPlane());
    }

    public static long encodeToLong(int x, int z) {
        return ((long) x << 32) + z - Integer.MIN_VALUE;
    }

    public static int getX(long l) {
        return (int) (l >> 32);
    }

    public static int getZ(long l) {
        return (int) (l & 0xFFFFFFFFL) + Integer.MIN_VALUE;
    }
}
