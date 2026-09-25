package net.nuclearteam.createnuclear.infrastructure.worldgen.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chunks turned into irradiated biome, so they can be restored later.
 * <p>
 * Codec-driven {@link SavedDataType} since 26.2; the layout is upstream's, a {@code chunks} list
 * of {@code {x, z}}.
 */
public class PersistentIrradiatedZones extends SavedData {
    private record Chunk(int x, int z) {
        static final Codec<Chunk> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("x").forGetter(Chunk::x),
            Codec.INT.fieldOf("z").forGetter(Chunk::z)
        ).apply(i, Chunk::new));
    }

    private static final Codec<PersistentIrradiatedZones> CODEC = RecordCodecBuilder.create(i -> i.group(
        Chunk.CODEC.listOf().optionalFieldOf("chunks", List.of()).forGetter(PersistentIrradiatedZones::chunkList)
    ).apply(i, PersistentIrradiatedZones::new));

    private static final SavedDataType<PersistentIrradiatedZones> TYPE = new SavedDataType<>(
        CreateNuclear.asResource("irradiated_zones"), PersistentIrradiatedZones::new, CODEC, null);

    private final Set<ChunkPos> chunks = new HashSet<>();

    public PersistentIrradiatedZones() {
    }

    private PersistentIrradiatedZones(List<Chunk> list) {
        for (Chunk chunk : list)
            chunks.add(new ChunkPos(chunk.x(), chunk.z()));
    }

    private List<Chunk> chunkList() {
        return chunks.stream().map(pos -> new Chunk(pos.x(), pos.z())).toList();
    }

    public static PersistentIrradiatedZones get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void addChunks(Iterable<ChunkPos> newChunks) {
        boolean changed = false;
        for (ChunkPos pos : newChunks) {
            changed |= chunks.add(pos);
        }

        if (changed) setDirty();
    }

    public boolean isInsideAnyZone(BlockPos pos) {
        return chunks.contains(ChunkPos.containing(pos));
    }

    public boolean containsChunk(ChunkPos pos) {
        return chunks.contains(pos);
    }

    public void removeChunk(ChunkPos pos) {
        if (chunks.remove(pos)) setDirty();
    }
}
