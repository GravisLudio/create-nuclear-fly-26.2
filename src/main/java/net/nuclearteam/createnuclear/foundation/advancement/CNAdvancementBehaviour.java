package net.nuclearteam.createnuclear.foundation.advancement;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Remembers who placed a block entity and awards them its advancements.
 * <p>
 * Ported the way the Connected port did Create's {@code AdvancementBehaviour}: the owner is a
 * {@link UUIDUtil#CODEC} field in a {@code ValueOutput}, players are looked up across dimensions,
 * and fake players are Create Fly's {@link FakePlayerEntity} rather than NeoForge's.
 * <p>
 * <b>Diverges from the NeoForge branch on purpose.</b> Its {@code award} granted an advancement
 * only when it was <em>not</em> in this behaviour's set ({@code !advancements.contains(a)}), which
 * never awards any of the advancements a block entity registers -- the reactor tiers included. The
 * Forge branch, which the NeoForge one tracks for parity, checks
 * {@code !advancement.isAlreadyAwardedTo(player)}, and that is what this does.
 */
public class CNAdvancementBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<CNAdvancementBehaviour> TYPE = new BehaviourType<>();

    private UUID playerId;
    private final Set<CreateNuclearAdvancement> advancements;

    public CNAdvancementBehaviour(SmartBlockEntity be, CreateNuclearAdvancement... advancement) {
        super(be);
        this.advancements = new HashSet<>();
        add(advancement);
    }

    public void add(CreateNuclearAdvancement... advancement) {
        Collections.addAll(this.advancements, advancement);
    }

    public boolean isOwnerPresent() {
        return playerId != null;
    }

    public void setPlayer(UUID id) {
        Player player = getLevel().getPlayerInAnyDimension(id);
        if (player == null) return;
        playerId = id;
        removeAwarded();
        blockEntity.setChanged();
    }

    @Override
    public void initialize() {
        super.initialize();
        removeAwarded();
    }

    private void removeAwarded() {
        Player player = getPlayer();
        if (player == null) return;

        advancements.removeIf(c -> c.isAlreadyAwardedTo(player));
        if (advancements.isEmpty()) {
            playerId = null;
            blockEntity.setChanged();
        }
    }

    public void awardPlayerIfNear(CreateNuclearAdvancement advancement, int maxDistance) {
        Player player = getPlayer();
        if (player == null)
            return;
        if (player.distanceToSqr(Vec3.atCenterOf(getPos())) > maxDistance * maxDistance)
            return;
        award(advancement, player);
    }

    public void awardPlayer(CreateNuclearAdvancement advancement) {
        Player player = getPlayer();
        if (player == null)
            return;
        award(advancement, player);
    }

    private void award(CreateNuclearAdvancement advancement, Player player) {
        if (!advancement.isAlreadyAwardedTo(player)) advancement.awardTo(player);
        removeAwarded();
    }

    private Player getPlayer() {
        if (playerId == null) return null;
        return getLevel().getPlayerInAnyDimension(playerId);
    }

    @Override
    public void write(ValueOutput nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        if (playerId != null)
            nbt.store("Owner", UUIDUtil.CODEC, playerId);
    }

    @Override
    public void read(ValueInput nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);
        playerId = nbt.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    public static void tryAward(BlockGetter reader, BlockPos pos, CreateNuclearAdvancement advancement) {
        CNAdvancementBehaviour behaviour = BlockEntityBehaviour.get(reader, pos, TYPE);
        if (behaviour != null)
            behaviour.awardPlayer(advancement);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public static void setPlacedBy(Level worldIn, BlockPos pos, LivingEntity placer) {
        CNAdvancementBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, TYPE);
        if (behaviour == null)
            return;
        if (placer instanceof FakePlayerEntity)
            return;
        if (placer instanceof ServerPlayer)
            behaviour.setPlayer(placer.getUUID());
    }
}
