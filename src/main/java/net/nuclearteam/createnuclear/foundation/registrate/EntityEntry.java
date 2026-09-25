package net.nuclearteam.createnuclear.foundation.registrate;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for Registrate's {@code EntityEntry}: a thin holder around an already-registered type,
 * eager for the same reason as {@link BlockEntry}.
 */
public final class EntityEntry<T extends Entity> {
    private final Identifier id;
    private final EntityType<T> type;

    public EntityEntry(Identifier id, EntityType<T> type) {
        this.id = id;
        this.type = type;
    }

    public EntityType<T> get() {
        return type;
    }

    public Identifier getId() {
        return id;
    }

    /** Registrate forwarded this to {@code EntityType.create(Level)}, which now takes a spawn reason. */
    @Nullable
    public T create(Level level, EntitySpawnReason reason) {
        return type.create(level, reason);
    }

    public boolean is(Entity entity) {
        return entity.getType() == type;
    }
}
