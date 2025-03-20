package me.beanes.acid.plugin.block;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.Half;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.beanes.acid.plugin.Acid;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.block.impl.*;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;
import java.util.Set;

public class BlockManager {

    private final Object2ObjectMap<StateType, BlockProvider> providers = new Object2ObjectOpenHashMap<>();
    private final BlockProvider DEFAULT_PROVIDER = new BlockProvider();

    public BlockManager() {
        register(new CakeBlockProvider(), StateTypes.CAKE);
        register(new SnowBlockProvider(), StateTypes.SNOW);
        register(new EnderPortalFrameBlockProvider(), StateTypes.END_PORTAL_FRAME);
        register(new SoulSandBlockProvider(), StateTypes.SOUL_SAND);
        register(new LadderBlockProvider(), StateTypes.LADDER);
        register(new DaylightBlockProvider(), StateTypes.DAYLIGHT_DETECTOR);
        register(new BrewingStandBlockProvider(), StateTypes.BREWING_STAND);
        register(new BedBlockProvider(), StateTypes.RED_BED);
        register(new VineBlockProvider(), StateTypes.VINE);
        register(new LilyPadBlockProvider(), StateTypes.LILY_PAD);
        register(new CactusBlockProvider(), StateTypes.CACTUS);
        register(new EnderChestBlockProvider(), StateTypes.ENDER_CHEST);
        register(new EnchantmentTableBlockProvider(), StateTypes.ENCHANTING_TABLE);
        register(new AnvilBlockProvider(), StateTypes.ANVIL);
        register(new PistonExtensionBlockProvider(), StateTypes.PISTON_HEAD);
        register(new HopperBlockProvider(), StateTypes.HOPPER);
        register(new CocoaBlockProvider(), StateTypes.COCOA);
        register(new DragonEggBlockProvider(), StateTypes.DRAGON_EGG);
        register(new BeaconBlockProvider(), StateTypes.BEACON);
        register(new FurnaceBlockProvider(), StateTypes.FURNACE);
        register(new JukeboxBlockProvider(), StateTypes.JUKEBOX);
        register(new LeverBlockProvider(), StateTypes.LEVER);
        register(new NoteBlockProvider(), StateTypes.NOTE_BLOCK);
        register(new CraftingTableBlockProvider(), StateTypes.CRAFTING_TABLE);
        register(new CommandBlockProvider(), StateTypes.COMMAND_BLOCK);
        register(new HayBlockProvider(), StateTypes.HAY_BLOCK);
        register(new TripWireBlockProvider(), StateTypes.TRIPWIRE);
        register(new TripWireHookBlockProvider(), StateTypes.TRIPWIRE_HOOK);
        register(new FlowerPotBlockProvider(), StateTypes.FLOWER_POT);
        register(new DeadBushBlockProvider(), StateTypes.DEAD_BUSH);
        register(new NetherPortalBlockProvider(), StateTypes.NETHER_PORTAL);
        register(new SugarCaneBlockProvider(), StateTypes.SUGAR_CANE);
        register(new TallGrassBlockProvider(), StateTypes.TALL_GRASS);
        register(new TNTBlockProvider(), StateTypes.TNT);
        register(new NetherWartBlockProvider(), StateTypes.NETHER_WART);
        register(new FarmLandBlockProvider(), StateTypes.FARMLAND);
        register(new SaplingBlockProvider(), BlockTags.SAPLINGS.getStates());
        register(new DoorBlockProvider(), BlockTags.DOORS.getStates());
        register(new CauldronBlockProvider(), BlockTags.CAULDRONS.getStates());
        register(new SlabBlockProvider(), BlockTags.SLABS.getStates());
        register(new CarpetBlockProvider(), BlockTags.WOOL_CARPETS.getStates());
        register(new FenceBlockProvider(), BlockTags.FENCES.getStates());
        register(new FenceGateBlockProvider(), BlockTags.FENCE_GATES.getStates());
        register(new WallBlockProvider(), BlockTags.WALLS.getStates());
        register(new TrapDoorBlockProvider(), BlockTags.TRAPDOORS.getStates());
        register(new StairBlockProvider(), BlockTags.STAIRS.getStates());
        register(new ButtonBlockProvider(), BlockTags.BUTTONS.getStates());
        register(new SignBlockProvider(), BlockTags.SIGNS.getStates());
        register(new BannerBlockProvider(), BlockTags.BANNERS.getStates());
        register(new FlowerBlockProvider(), BlockTags.SMALL_FLOWERS.getStates());
        register(new BlockPressurePlateProvider(), BlockTags.PRESSURE_PLATES.getStates());
        register(new RailBlockProvider(), BlockTags.RAILS.getStates());
        register(new MushroomBlockProvider(), StateTypes.BROWN_MUSHROOM, StateTypes.RED_MUSHROOM);
        register(new DispenserBlockProvider(), StateTypes.DISPENSER, StateTypes.DROPPER);
        register(new ChestBlockProvider(), StateTypes.CHEST, StateTypes.TRAPPED_CHEST);
        register(new SkullBlockProvider(), StateTypes.SKELETON_SKULL, StateTypes.PLAYER_HEAD);
        register(new WallSkullBlockProvider(), StateTypes.SKELETON_WALL_SKULL, StateTypes.PLAYER_WALL_HEAD);
        register(new RedstoneDiodeBlockProvider(), StateTypes.REPEATER, StateTypes.COMPARATOR);
        register(new PistonBaseBlockProvider(), StateTypes.PISTON, StateTypes.STICKY_PISTON);
        register(new StemBlockProvider(), StateTypes.MELON_STEM, StateTypes.PUMPKIN_STEM);
        register(new CropsBlockProvider(), StateTypes.WHEAT, StateTypes.CARROTS, StateTypes.POTATOES);
        register(new PumpkinBlockProvider(), StateTypes.PUMPKIN, StateTypes.CARVED_PUMPKIN, StateTypes.JACK_O_LANTERN);
        register(new TorchBlockProvider(), StateTypes.TORCH, StateTypes.WALL_TORCH, StateTypes.REDSTONE_TORCH, StateTypes.REDSTONE_WALL_TORCH);
        PaneBlockProvider paneBlockProvider = new PaneBlockProvider();
        register(paneBlockProvider, BlockTags.GLASS_PANES.getStates());
        register(paneBlockProvider, StateTypes.IRON_BARS);
        // Piston extension bounds is not coded, because uh.. it would be insanely hard to do properly
    }

    private void register(BlockProvider provider, StateType... types) {
        for (StateType type : types) {
            if (providers.containsKey(type)) {
                throw new IllegalStateException("Duplicate register for: " + type.getName());
            }

            providers.put(type, provider);
        }
    }

    private void register(BlockProvider provider, Set<StateType> types) {
        for (StateType type : types) {
            register(provider, type);
        }
    }

    public boolean isFullBlock(WrappedBlockState state) {
        if (!state.getType().isSolid()) {
            return false;
        }

        if (BlockTags.GLASS_BLOCKS.contains(state.getType())) {
            return false;
        }

        if (state.getType() == StateTypes.BEACON) {
            return false;
        }

        return providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).isFullCube(state);
    }

    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).onActivate(data, x, y, z, state, latest);
    }

    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
        providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).onPlace(data, x, y, z, face, hit, meta, state);
    }

    // This method greedily adds boxes to a list, this means it will try to add as much as possible collision bounding boxes to the list
    // If a connecting block is in a split state it will try adding every possible bounding box
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        if (!state.getType().isSolid()) {
            return;
        }

        providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).addPossibleCollisionBoxes(data, x, y, z, state, mask, list);
    }

    public BoundingBox getBoundingBoxForRaytrace(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).getBoundsBasedOnState(data, x, y, z, state);
    }

    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        if (!state.getType().isSolid()) {
            return SplitStateBoolean.FALSE;
        }

        return providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).isColliding(data, x, y, z, state, mask);
    }

    public BoundingBox getCollisionBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        if (!state.getType().isSolid()) {
            return null;
        }

        return providers.getOrDefault(state.getType(), DEFAULT_PROVIDER).getCollisionBoundingBox(data, x, y, z, state);
    }

    public boolean isNormalCube(WrappedBlockState state) {
        return state.getType().isSolid() && isFullBlock(state) && state.getType() != StateTypes.REDSTONE_BLOCK;
    }

    public boolean doesBlockHaveSolidTopSurface(WrappedBlockState state) {
        if (state.getType().isSolid() && Acid.get().getBlockManager().isFullBlock(state)) {
            return true;
        }

        if (BlockTags.STAIRS.contains(state.getType()) || BlockTags.SLABS.contains(state.getType())) {
            return state.getHalf() == Half.TOP;
        }

        if (state.getType() == StateTypes.HOPPER) {
            return true;
        }

        if (state.getType() == StateTypes.SNOW) {
            return state.getLayers() == 7;
        }

        return false;
    }
}
