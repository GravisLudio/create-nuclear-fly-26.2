package net.nuclearteam.createnuclear.content.contraptions.irradiated;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public interface IrradiatedAnimal {
    Map<EntityType<?>, EntityType<? extends Animal>> VANILLA_TO_IRRADIATED = new HashMap<>();

    EntityType<? extends Animal> getNormalVariant();

    default void readFromVanilla(Animal animal) {}

    default void writeToVanilla(Animal animal) {}

    boolean isConverting();

    void setConverting();

    void setConversionTime(int conversionTime);

    int getConversionTime();

    default void startConverting(int conversionTime) {
        Animal animal = (Animal) this;

        setConversionTime(conversionTime);
        setConverting();
        animal.removeEffect(MobEffects.WEAKNESS);
        animal.addEffect(new MobEffectInstance(MobEffects.STRENGTH, conversionTime, Math.min(animal.level().getDifficulty().getId() - 1, 0)));
        animal.level().broadcastEntityEvent(animal, EntityEvent.ZOMBIE_CONVERTING);
    }

    @SuppressWarnings("unchecked")
    default void finishConversion(ServerLevel level) {
        Animal irradiatedAnimal = (Animal) this;
        // Fabric fires ServerLivingEntityEvents.MOB_CONVERSION from convertTo itself (was EventHooks.onLivingConvert).
        // keepEquipment=false and preserveCanPickUpLoot=false match the old convertTo(type, false).
        irradiatedAnimal.convertTo((EntityType<Animal>) getNormalVariant(), ConversionParams.single(irradiatedAnimal, false, false), vanillaAnimal -> {
            vanillaAnimal.finalizeSpawn(level, level.getCurrentDifficultyAt(vanillaAnimal.blockPosition()), EntitySpawnReason.CONVERSION, null);
            writeToVanilla(vanillaAnimal);
            vanillaAnimal.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));

            if (!irradiatedAnimal.isSilent()) {
                level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_CONVERTED, irradiatedAnimal.blockPosition(), 0);
            }
        });
    }

    default int getConversionProgress() {
        int progress = 1;
        Animal animal = (Animal) this;

        if (animal.getRandom().nextFloat() < 0.01F) {
            int buffCount = 0;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int x = (int) animal.getX() - 4; x < animal.getX() + 4 && buffCount < 14; ++x) {
                for (int y = (int) animal.getY() - 4; y < animal.getY() + 4 && buffCount < 14; ++y) {
                    for (int z = (int) animal.getZ() - 4; z < animal.getZ() + 4 && buffCount < 14; ++z) {
                        BlockState state = animal.level().getBlockState(pos.set(x, y, z));

                        if (state.is(BlockTags.WOODEN_FENCES) || state.is(Blocks.HAY_BLOCK)) {
                            if (animal.getRandom().nextFloat() < 0.3F)
                                ++progress;

                            ++buffCount;
                        }
                    }
                }
            }
        }

        return progress;
    }
}
