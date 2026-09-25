package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.fluids.transfer.GenericItemEmptying;
import com.zurrtum.create.content.fluids.transfer.GenericItemFilling;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.foundation.fluid.FluidHelper.FluidExchange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

public class PlayerInteractReactorFluidInput {
    public static InteractionResult interact(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, boolean onClient, BlockHitResult ray) {
        FluidExchange exchange = null;
        ReactorFluidInputEntity be = (ReactorFluidInputEntity) level.getBlockEntity(pos);
        if (be == null) {
            return InteractionResult.FAIL;
        }

        // Was the fluid handler capability; Create Fly reaches the same tank through
        // ReactorFluidInput's FluidInventoryProvider, which FluidHelper uses below.
        ReactorFluidInputEntity.InputTank fluidInput = be.getTank();

        FluidStack prevFluidInInput = fluidInput.getFluid().copy();

        if (FluidHelper.tryEmptyItemIntoBE(level, player, hand, stack, be)) exchange = FluidExchange.ITEM_TO_TANK;
        if (FluidHelper.tryFillItemFromBE(level, player, hand, stack, be)) exchange = FluidExchange.TANK_TO_ITEM;

        if (exchange == null) {
            if (GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack)) return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }

        SoundEvent soundEvent = null;
        BlockState fluidState = null;
        FluidStack fluidInInput = fluidInput.getFluid();

        if (exchange == FluidExchange.ITEM_TO_TANK) {
            if (player.isCreative() && !onClient) {
                FluidStack fluidInItem = GenericItemEmptying.emptyItem(level, stack, true).getFirst();
//                if (!fluidInItem.isEmpty() && fluidInInput instanceof ReactorLiquidInput) {
//                    fluidInInput.set
//                }
            }

            Fluid fluid = fluidInInput.getFluid();
            fluidState = fluid.defaultFluidState().createLegacyBlock();
            soundEvent = FluidHelper.getEmptySound(fluidInInput);
        }

        if (exchange == FluidExchange.TANK_TO_ITEM) {
            if ( player.isCreative() && !onClient) {

            }

            Fluid fluid = prevFluidInInput.getFluid();
            fluidState = fluid.defaultFluidState().createLegacyBlock();
            soundEvent = FluidHelper.getFillSound(prevFluidInInput);
        }

        if (soundEvent != null && !onClient) {
            float pitch = Mth
                    .clamp(1 - (1f * fluidInInput.getAmount() / (FluidTankBlockEntity.getCapacityMultiplier() * 16)), 0, 1);
            pitch /= 1.5f;
            pitch += .5f;
            pitch += (level.getRandom().nextFloat() - .5f) / 4f;
            level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, .5f, pitch);
        }

        if (!FluidStack.areFluidsAndComponentsEqual(fluidInInput, prevFluidInInput) || fluidInInput.getAmount() != prevFluidInInput.getAmount()) {
            if (be instanceof ReactorFluidInputEntity) {
                if (fluidState != null && onClient) {
                    BlockParticleOption blockParticleData =
                            new BlockParticleOption(ParticleTypes.BLOCK, fluidState);
                    float flevel = (float) fluidInInput.getAmount() / fluidInput.getMaxAmountPerStack();

                    // Lighter-than-air fluids flipped the level here; Create Fly has no fluid-type API
                    // to ask, and stubs the same branch out in its own tank.

                    Vec3 vec = ray.getLocation();
                    vec = new Vec3(vec.x, be.getBlockPos()
                            .getY() + flevel * (1 - .5f) + .25f, vec.z);
                    Vec3 motion = player.position()
                            .subtract(vec)
                            .scale(1 / 20f);
                    vec = vec.add(motion);
                    level.addParticle(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.SUCCESS;

    }
}
