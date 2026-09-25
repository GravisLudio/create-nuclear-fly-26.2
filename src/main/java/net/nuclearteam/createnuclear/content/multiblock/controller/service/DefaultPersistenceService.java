package net.nuclearteam.createnuclear.content.multiblock.controller.service;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;

public class DefaultPersistenceService implements IPersistenceService {
    @Override
    public void readBasicState(ReactorControllerBlockEntity owner, CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        owner.setMultiblockSize(compound.getIntOr("reactorSize", 0));
        owner.setMultiblockFacing(Direction.byName(compound.getStringOr("reactorFacing", "")));

        owner.setMultiblockStructure(compound.contains("reactorPose")
            ? BoundingBox.CODEC.parse(NbtOps.INSTANCE, compound.get("reactorPose")).result().orElse(null)
            : null
        );

        if (!clientPacket) {
            owner.deserializeInventory(registries, compound.getCompoundOrEmpty("pattern"));
        } else {
            owner.setDisplayState(compound.contains("displayState")
                    ? ReactorDisplayState.deserializeNBT(registries, compound.getCompoundOrEmpty("displayState"))
                    : ReactorDisplayState.EMPTY
            );
        }
        // ItemStack.parse/saveOptional are gone in 26.2; the optional codec is the route now.
        owner.setConfiguredPattern(compound.get("items") == null ? ItemStack.EMPTY
            : ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), compound.get("items"))
                .result().orElse(ItemStack.EMPTY));

    }

    @Override
    public void writeBasicState(ReactorControllerBlockEntity owner, CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putInt("reactorSize", owner.getMultiblockSize());
        compound.putString("reactorFacing", owner.getMultiblockFacing() != null ? owner.getMultiblockFacing().getSerializedName() : "");
        if (owner.getMultiblockPos() != null) {
            compound.put("reactorPose", BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, owner.getMultiblockPos()).getOrThrow());
        }

        // Stacks need registry access to encode; a write without a level (none observed, but
        // SmartBlockEntity allows it) keeps the plain fields and skips them.
        if (registries == null)
            return;

        if (!clientPacket) {
            compound.put("pattern", owner.serializeInventory(registries));
        } else {
            compound.put("displayState", owner.getDisplayState().serializeNBT(registries));
        }
        compound.put("items", ItemStack.OPTIONAL_CODEC
            .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), owner.getConfiguredPattern())
            .getOrThrow());

    }
}
