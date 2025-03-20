package me.beanes.acid.plugin.player.tracker.impl.world;

import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_8.Chunk_v1_8;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import it.unimi.dsi.fastutil.shorts.Short2CharArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2CharMap;
import lombok.Getter;
import lombok.Setter;
import me.beanes.acid.plugin.util.splitstate.SplitState;
import org.bukkit.Bukkit;

import java.util.Arrays;

public class ChunkData {
    @Getter
    private final BaseChunk[] sections;
    private final BaseChunk[] oldSections;

    /*
       So we have 2 systems in place to store old blocks when a block update happens:
       The first system is if a chunk section gets overridden we just store the old section in the oldData variable inside the section (until this is confirmed and nullified)
       The second system is for small block changes using multi block change or block change
       To prevent allocating lots of memory per section we can just store the old blocks inside the chunk object
       Now we know every chunk has blocks within these bounds
           - X 0-15 - 4 bits
           - Y 0-255 - 8 bits
           - Z 0-15 - 4 bits
       The total of this is 16 bits which means we can store the coordinates inside a short to not waste any memory
       So now we can make a map with Short -> Char (block data) which should be 2 bytes per key and 2 bytes for the value
       We also have to make a map with Short -> Int (transaction) which stores the pre transaction to prevent 2 block processes in the same transaction map (we could further optimize this if we changed the transaction system to count up to short limit and then switch back to 0)
       This means 10 bytes per block which is nothing compared to if we allocate another data array per section for old data (8192 bytes just for the data array)
       Of-course this does not keep in mind the map implementation memory overhead but I doubt it's a lot
    */

    private final Short2CharMap oldBlocks = new Short2CharArrayMap(20); // If this code somehow uses too much memory we can replace this to a simple Long -> Char inside world tracker (this however is open to hash collisions then)
    @Getter @Setter
    private int lastPreTransaction;
    @Getter @Setter
    private boolean confirmed; // This defines whether we are 100% sure if the chunk is loaded on the client (this is false during before post trans chunk packet & after pre trans unload)

    // TODO: strip the light data

    public ChunkData(BaseChunk[] sections) {
        this.sections = sections;
        this.oldSections = new BaseChunk[16];
    }

    public void overwriteSections(BaseChunk[] newSections) {
        for (int i = 0; i < newSections.length; i++) {
            BaseChunk chunk = newSections[i];
            if (chunk != null) {
                oldSections[i] = sections[i];
                sections[i] = chunk;
            }
        }
    }

    protected void storeOldBlock(short key, int blockState) {
        oldBlocks.put(key, (char) blockState); // We can cast to chars because 1.8.9 never uses block states bigger than a char
    }

    protected void confirmOldBlock(short key) {
        oldBlocks.remove(key);
    }

    protected SplitState<WrappedBlockState> getBlock(ClientVersion version, int x, int y, int z) {
        short compactedBlockPos = (short) (y << 8 | z << 4 | x);

        WrappedBlockState oldBlock = null;
        if (oldBlocks.containsKey(compactedBlockPos)) {
            oldBlock = WrappedBlockState.getByGlobalId(version, oldBlocks.get(compactedBlockPos));
        }

        WrappedBlockState latestBlock = WrappedBlockState.getByGlobalId(0);

        if (y >= 0 && y >> 4 < this.sections.length) {
            BaseChunk section = sections[y >> 4];
            BaseChunk oldSection = oldSections[y >> 4];

            if (section != null) {
                latestBlock = section.get(version, x, y & 15, z);
            }

            if (oldSection != null) {
                if (oldBlock != null) {
                    throw new IllegalStateException("Block fetch failure");
                }

                oldBlock = oldSection.get(version, x, y & 15, z);
            }
        }

        // If the chunk is not confirmed, return air
        if (!confirmed) {
            oldBlock = WrappedBlockState.getByGlobalId(0);
        }

        return new SplitState<>(latestBlock, oldBlock);
    }

    protected void setBock(int x, int y, int z, WrappedBlockState state) {
        if (y >= 0 && (y >> 4) < this.sections.length) {
            BaseChunk section = sections[y >> 4];

            if (section == null) {
                section = new Chunk_v1_8(false);
                sections[y >> 4] = section;
            }

            section.set(x, y & 15, z, state);
        }
    }

    protected void setOldBock(int x, int y, int z, WrappedBlockState state) {
        short compactedBlockPos = (short) (y << 8 | z << 4 | x);

        // Only override the old block value if it already exists
        if (oldBlocks.containsKey(compactedBlockPos)) {
            oldBlocks.put(compactedBlockPos, (char) state.getGlobalId());
        }

        if (y >= 0 && y >> 4 < this.oldSections.length) {
            BaseChunk oldSection = oldSections[y >> 4];

            if (oldSection != null) {
                oldSection.set(x, y & 15, z, state);
            }
        }
    }

    public void confirmSections() {
        Arrays.fill(this.oldSections, null);
    }
}
