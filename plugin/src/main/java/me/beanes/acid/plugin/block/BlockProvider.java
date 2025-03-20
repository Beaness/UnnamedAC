package me.beanes.acid.plugin.block;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3f;
import me.beanes.acid.plugin.player.PlayerData;
import me.beanes.acid.plugin.util.BoundingBox;
import me.beanes.acid.plugin.util.splitstate.SplitStateBoolean;

import java.util.List;

public class BlockProvider {
    public void addPossibleCollisionBoxes(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask, List<BoundingBox> list) {
        BoundingBox box = this.getCollisionBoundingBox(data, x, y, z, state);

        if (box != null && mask.intersectsWith(box)) {
            list.add(box);
        }
    }

    // In minecraft this method is only used for simple blocks but in this anticheat we only use it for blocks that never change state
    // We use addPossibleCollisionBoxes if the state can be different states at the same time due to split states
    public BoundingBox getCollisionBoundingBox(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    // This method is needed due to addPossibleCollisionBoxes being greedy which means it adds as many as possible boxes
    // In sneaking we wanna know if its possible there are no collisions which means the greedy fetching could false flag sneaking
    public SplitStateBoolean isColliding(PlayerData data, int x, int y, int z, WrappedBlockState state, BoundingBox mask) {
        if (mask.intersectsWith(getCollisionBoundingBox(data, x, y, z, state))) {
            return SplitStateBoolean.TRUE;
        } else {
            return SplitStateBoolean.FALSE;
        }
    }

    public BoundingBox getBoundsBasedOnState(PlayerData data, int x, int y, int z, WrappedBlockState state) {
        return new BoundingBox(0F, 0F, 0F, 1F, 1F, 1F);
    }

    public boolean isFullCube(WrappedBlockState state) {
        return true;
    }

    public SplitStateBoolean onActivate(PlayerData data, int x, int y, int z, WrappedBlockState state, boolean latest) {
        return SplitStateBoolean.FALSE;
    }

    public void onPlace(PlayerData data, int x, int y, int z, BlockFace face, Vector3f hit, int meta, WrappedBlockState state) {
    }
}