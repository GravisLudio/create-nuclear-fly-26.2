package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent storage for fluid locks associated with multiblock controllers.
 * Locks are persisted to world saved data so controllers keep their preferred
 * fluid across server restarts.
 * <p>
 * 26.2 saved data is codec-driven ({@link SavedDataType}) instead of a factory reading and writing
 * a {@code CompoundTag} by hand. The codec keeps upstream's layout -- a {@code locks} list of
 * {@code {x, y, z, fluid}} -- so the fields line up with what upstream wrote.
 */
public class PersistentFluidLocks extends SavedData {
    private record Lock(int x, int y, int z, Fluid fluid) {
        static final Codec<Lock> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("x").forGetter(Lock::x),
            Codec.INT.fieldOf("y").forGetter(Lock::y),
            Codec.INT.fieldOf("z").forGetter(Lock::z),
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(Lock::fluid)
        ).apply(i, Lock::new));
    }

    private static final Codec<PersistentFluidLocks> CODEC = RecordCodecBuilder.create(i -> i.group(
        Lock.CODEC.listOf().optionalFieldOf("locks", List.of()).forGetter(PersistentFluidLocks::lockList)
    ).apply(i, PersistentFluidLocks::new));

    private static final SavedDataType<PersistentFluidLocks> TYPE = new SavedDataType<>(
        CreateNuclear.asResource("fluid_locks"), PersistentFluidLocks::new, CODEC, null);

    private final Map<BlockPos, Fluid> locks = new ConcurrentHashMap<>();

    public PersistentFluidLocks() {
    }

    private PersistentFluidLocks(List<Lock> list) {
        for (Lock lock : list)
            locks.put(new BlockPos(lock.x(), lock.y(), lock.z()), lock.fluid());
    }

    private List<Lock> lockList() {
        return locks.entrySet().stream()
            .map(e -> new Lock(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ(), e.getValue()))
            .toList();
    }

    public static PersistentFluidLocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

   /**
    * Attempt to acquire a persistent lock for the specified controller position.
    * @param pos controller block position
    * @param fluid fluid to lock to; if null the call is a no-op and returns true
    * @return true if the lock was acquired or already held for the same fluid
    */
   public boolean tryLock(BlockPos pos, Fluid fluid) {
        if (fluid == null) return true;
        boolean ok = locks.compute(pos, (k,v) -> v == null ? fluid : v) == fluid;
        if (ok) setDirty();
        return ok;
   }

    /**
     * Check whether the given fluid is acceptable for the controller at {@code pos}.
     * @param pos controller position
     * @param fluid fluid to check
     * @return true if no lock is present or the lock matches the provided fluid
     */
    public boolean canAccept(BlockPos pos, Fluid fluid) {
        if (fluid == null) return true;
        Fluid locked = locks.get(pos);
        return locked == null || locked == fluid;
    }

    /**
     * Clear any persistent lock for the specified controller position.
     * Marks the saved data as dirty when a lock was removed.
     */
    public void clearLock(BlockPos pos) {
        if (locks.remove(pos) != null) setDirty();
    }
}
