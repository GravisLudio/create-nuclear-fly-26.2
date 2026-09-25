package net.nuclearteam.createnuclear.foundation.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.registrate.ItemProvider;

/**
 * Runtime half of an advancement. Upstream also built the {@code Advancement.Builder} and wrote the
 * JSON and lang from here; those outputs are committed under {@code src/generated/resources}, so
 * the builder methods that only shaped them are accepted and ignored.
 * <p>
 * <b>What is load-bearing is the builtin trigger.</b> An advancement with no external trigger gets
 * {@code createnuclear:<id>_builtin} registered, and the committed JSON names exactly that id as its
 * criterion. {@code externalTrigger} is kept because it decides whether that trigger exists; its
 * argument, the vanilla criterion, only ever fed the JSON and is gone.
 */
@SuppressWarnings("unused")
public class CreateNuclearAdvancement {
    private SimpleCreateNuclearTrigger builtinTrigger;
    private final Builder createNuclearBuilder = new Builder();

    final String id;

    public CreateNuclearAdvancement(String id, java.util.function.UnaryOperator<Builder> b) {
        this.id = id;

        b.apply(createNuclearBuilder);

        if (!createNuclearBuilder.externalTrigger) {
            builtinTrigger = CNTriggers.addSimple(id + "_builtin");
        }

        CNAdvancement.ENTRIES.add(this);
    }

    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        // ServerPlayer.getServer() is gone; the server is reached through the level.
        AdvancementHolder advancement = sp.level()
                .getServer()
                .getAdvancements()
                .get(CreateNuclear.asResource(id));
        if (advancement == null)
            return true;
        return sp.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone();
    }

    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (builtinTrigger == null)
            throw new UnsupportedOperationException(
                    "Advancement " + id + " uses external Triggers, it cannot be awarded directly");
        builtinTrigger.trigger(sp);
    }

    enum TaskType {
        SILENT, NORMAL, NOISY, EXPERT, SECRET
    }

    public class Builder {
        private boolean externalTrigger;

        // --- accepted and ignored: these only shaped the generated JSON ---

        Builder special(TaskType type) {
            return this;
        }

        Builder after(CreateNuclearAdvancement other) {
            return this;
        }

        Builder icon(ItemProvider item) {
            return this;
        }

        Builder icon(ItemLike item) {
            return this;
        }

        Builder icon(ItemStack stack) {
            return this;
        }

        Builder title(String title) {
            return this;
        }

        Builder description(String description) {
            return this;
        }

        // --- these decide whether a builtin trigger is created, so they are real ---

        Builder whenBlockPlaced(Block block) {
            return externalTrigger();
        }

        Builder whenIconCollected() {
            return externalTrigger();
        }

        Builder whenItemCollected(ItemProvider item) {
            return externalTrigger();
        }

        Builder whenItemCollected(ItemLike itemProvider) {
            return externalTrigger();
        }

        Builder whenItemCollected(TagKey<Item> tag) {
            return externalTrigger();
        }

        Builder awardedForFree() {
            return externalTrigger();
        }

        Builder externalTrigger() {
            externalTrigger = true;
            return this;
        }
    }
}
