package net.nuclearteam.createnuclear;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Upstream carried a copy of Create's sound registry with the datagen that wrote {@code sounds.json}
 * and the subtitle lang entries. Both outputs are committed under {@code src/generated/resources},
 * and registration is eager against vanilla rather than through {@code RegisterEvent}, so the
 * builder and the data provider are gone. Same approach as the Connected port.
 */
public class CNSoundEvents {

    public static final SoundEntry REACTOR_ACTIVATION = register("reacteur/activation", SoundSource.BLOCKS);
    public static final SoundEntry REACTOR_RUNNING = register("reacteur/running", SoundSource.BLOCKS);
    public static final SoundEntry REACTOR_SHUT_OFF = register("reacteur/shut_off", SoundSource.BLOCKS);
    public static final SoundEntry REACTOR_ALARM_ONESHOT = register("alarm/reactor_alarm", SoundSource.BLOCKS);
    public static final SoundEntry REACTOR_ALARM_LOOP = register("alarm/alarm", SoundSource.BLOCKS);
    public static final SoundEntry NUCLEAR_EXPLOSION = register("explosion/nuclear_explosion", SoundSource.AMBIENT);
    public static final SoundEntry NUCLEAR_EXPLOSION_LARGE = register("explosion/large_nuclear_explosion", SoundSource.AMBIENT);
    public static final SoundEntry NUCLEAR_EXPLOSION_RINGING = register("explosion/ringing", SoundSource.AMBIENT);
    public static final SoundEntry NUCLEAR_EXPLOSION_RUMBLE = register("explosion/nuclear_explosion_rumble", SoundSource.AMBIENT);
    public static final SoundEntry MOTOR_ASSEMBLE = register("reacteur/assemble_deassemble/motor_assemble", SoundSource.BLOCKS);
    public static final SoundEntry MOTOR_DISASSEMBLE = register("reacteur/assemble_deassemble/motor_disassemble", SoundSource.BLOCKS);
    public static final SoundEntry NUCLEAR_EXPLOSION_RUMBLE_2 = register("explosion/rumble", SoundSource.AMBIENT);
    public static final SoundEntry NUCLEAR_EXPLOSION_MAIN = register("explosion/main", SoundSource.AMBIENT);
    public static final SoundEntry NUCLEAR_EXPLOSION_SHOCKWAVE = register("explosion/shockwave", SoundSource.AMBIENT);
    public static final SoundEntry GEIGER_HIGH = register("geiger/high", SoundSource.AMBIENT);
    public static final SoundEntry GEIGER_LOW = register("geiger/low", SoundSource.AMBIENT);
    public static final SoundEntry GEIGER_MEDIUM = register("geiger/medium", SoundSource.AMBIENT);
    public static final SoundEntry BIOME_WASTELAND = register("biomes/wasteland", SoundSource.AMBIENT);

    private static SoundEntry register(String name, SoundSource category) {
        Identifier id = CreateNuclear.asResource(name);
        Holder.Reference<SoundEvent> holder = Registry.registerForHolder(
            BuiltInRegistries.SOUND_EVENT,
            id,
            SoundEvent.createVariableRangeEvent(id)
        );
        return new SoundEntry(id, holder, category);
    }

    /** Forces class loading from the initialiser; the fields above do the registering. */
    public static void register() {
    }

    public static void playItemPickup(Player player) {
        player.level().playSound(
            null,
            player.blockPosition(),
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            .2f,
            1f + player.level().getRandom().nextFloat()
        );
    }

    public record SoundEntry(Identifier id, Holder<SoundEvent> holder, SoundSource category) {
        public Identifier getId() {
            return id;
        }

        public Holder<SoundEvent> getMainEventHolder() {
            return holder;
        }

        public SoundEvent getMainEvent() {
            return holder.value();
        }

        public void play(Level world, @Nullable Player entity, double x, double y, double z, float volume, float pitch) {
            world.playSound(entity, x, y, z, getMainEvent(), category, volume, pitch);
        }

        public void play(Level world, @Nullable Player entity, Vec3i pos, float volume, float pitch) {
            play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
        }

        public void playOnServer(Level world, Vec3i pos) {
            play(world, null, pos, 1, 1);
        }

        public void playOnServer(Level world, Vec3i pos, float volume, float pitch) {
            play(world, null, pos, volume, pitch);
        }
    }
}
