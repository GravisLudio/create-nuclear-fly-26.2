package net.nuclearteam.createnuclear.foundation.utility;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Bridges code written against {@code CompoundTag} to 26.2's {@link ValueInput}/{@link ValueOutput}.
 * <p>
 * Block entities used to receive the tag itself in {@code read}/{@code write}; now they receive a
 * view with no raw access. The reactor controller hands its tag to half a dozen managers and a
 * persistence service that all speak {@code CompoundTag}, so rather than rewrite each of them, the
 * tag is read from and merged into the view at the top level: {@code assumeMapUnsafe} treats the
 * compound's entries as fields of the enclosing map, which keeps upstream's key layout exactly.
 */
public final class NbtViews {
    private static final MapCodec<CompoundTag> INLINE = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);

    private NbtViews() {
    }

    /** Every key of the view, as one compound. */
    public static CompoundTag readAll(ValueInput view) {
        return view.read(INLINE).orElseGet(CompoundTag::new);
    }

    /** Writes each key of {@code tag} as a key of the view. */
    public static void writeAll(ValueOutput view, CompoundTag tag) {
        if (!tag.isEmpty())
            view.store(INLINE, tag);
    }
}
