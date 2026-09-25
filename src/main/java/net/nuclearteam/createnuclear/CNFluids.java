package net.nuclearteam.createnuclear;

import com.zurrtum.create.infrastructure.fluids.FluidInteractionRegistry;
import com.zurrtum.create.infrastructure.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.nuclearteam.createnuclear.content.decoration.palettes.CNPaletteStoneTypes;
import net.nuclearteam.createnuclear.content.fluids.NuclearFluidEntry;
import net.nuclearteam.createnuclear.content.radiation.RadiationBucketItem;
import net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;

import java.util.Optional;
import java.util.function.Supplier;

public class CNFluids {
    private static final double URANIUM_FLUID_DOSE = 5.0D;

    // Flow values are upstream's fluidProperties: levelDecreasePerBlock, tickRate, slopeFindDistance.
    // Fog colour and distance are client-side, in client.CNFluidRenders.

    public static final NuclearFluidEntry URANIUM = NuclearFluidEntry.register(
        CreateNuclear.asResource("uranium"), 2, 15, 6,
        Optional.of(SoundEvents.BUCKET_FILL_LAVA),
        p -> p,
        (fluid, p) -> new RadiationBucketItem(fluid, p, 20));

    public static final NuclearFluidEntry THORIUM = NuclearFluidEntry.register(
        CreateNuclear.asResource("thorium"), 2, 15, 6,
        Optional.of(SoundEvents.BUCKET_FILL_LAVA),
        p -> p,
        BucketItem::new);

    public static final NuclearFluidEntry LIQUID_NITROGEN = NuclearFluidEntry.register(
        CreateNuclear.asResource("nitrogen"), 5, 10, 6,
        Optional.of(SoundEvents.BUCKET_FILL_AXOLOTL),
        p -> p,
        BucketItem::new);

    public static void register() {
        registerFluidDispenseBehavior(URANIUM.getBucket());
        registerFluidDispenseBehavior(THORIUM.getBucket());
        registerFluidDispenseBehavior(LIQUID_NITROGEN.getBucket());
    }

    /**
     * Was a NeoForge {@code LivingVisibilityEvent} listener, used as a per-tick hook on both sides.
     * Now called from {@code LivingEntityMixin} at the end of {@code baseTick}.
     */
    public static void handleFluidEffect(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) return;

        Level level = entity.level();

        // 1. Uranium: applies radiation contagion
        if (isInFluid(entity, URANIUM)) {
            if (entity.tickCount % 20 == 0) {
                RadiationCapability.applyContagion(entity, URANIUM_FLUID_DOSE, 100);
            }
        }

        // 2. Liquid nitrogen: freezes the entity like powder snow
        else if (isInFluid(entity, LIQUID_NITROGEN)) {
            // Extinguish fire immediately on contact
            if (entity.isOnFire()) {
                entity.clearFire();
            }

            if (entity instanceof Player player && player.isSwimming()) {
                CNAdvancement.CRYOGENIC_BAPTISM.awardTo(player);
            }

            int currentTicks = entity.getTicksFrozen();
            int maxTicks = entity.getTicksRequiredToFreeze();
            int freezeSpeed = 3;

            if (level.isClientSide()) {
                // Target maxTicks + 1 so vanilla's client-side -1 decay settles exactly at maxTicks
                entity.setTicksFrozen(Math.min(maxTicks + 1, currentTicks + freezeSpeed + 1));
            } else {
                // Target maxTicks + 2 so vanilla's server-side -2 decay settles exactly at maxTicks
                entity.setTicksFrozen(Math.min(maxTicks + 2, currentTicks + freezeSpeed + 2));

                // Check the freeze state after applying the compensation above
                if (entity.getTicksFrozen() >= maxTicks && entity.tickCount % 10 == 0) {
                    entity.hurt(entity.damageSources().freeze(), 2.0F);
                }

                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, false));
            }
        }

        // 3. Thorium: burns the entity like lava
        else if (isInFluid(entity, THORIUM)) {
            entity.lavaHurt();
        }
    }

    /**
     * Stands in for NeoForge's {@code Entity.isInFluidType(FluidType)}: vanilla only tracks fluid
     * contact per fluid tag, and these fluids are deliberately in neither water nor lava.
     */
    private static boolean isInFluid(LivingEntity entity, NuclearFluidEntry fluid) {
        AABB box = entity.getBoundingBox().deflate(0.001);
        Level level = entity.level();
        for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(box.minX, box.minY, box.minZ),
            BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (fluid.is(level.getFluidState(pos).getType()))
                return true;
        }
        return false;
    }

    /** Lava or water touching uranium turns into autunite, as upstream's FluidInteractionRegistry did. */
    public static void registerFluidInteractions() {
        Supplier<BlockState> autuniteState = () -> CNPaletteStoneTypes.AUTUNITE.getBaseBlock().get().defaultBlockState();
        Fluid uranium = URANIUM.get();
        for (Fluid source : new Fluid[]{Fluids.LAVA, Fluids.WATER}) {
            FluidInteractionRegistry.addInteraction(source, new InteractionInformation(uranium, fs -> autuniteState.get()));
        }
    }

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
    private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
            DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
            BlockPos pos = pSource.pos().relative(pSource.state().getValue(DispenserBlock.FACING));
            Level level = pSource.level();
            if (dispensibleContainerItem.emptyContents(null, level, pos, null)) {
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT.dispense(pSource, pStack);
        }
    };

    private static void registerFluidDispenseBehavior(BucketItem bucket) {
        DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
    }
}
