package net.nuclearteam.createnuclear.content.multiblock.output;

import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;

import java.util.List;

public class ReactorOutputEntity extends GeneratingKineticBlockEntity {
    public int speed = 0;
    public float heat = 0;

    protected ReactorPattern pattern =  new ReactorPattern();

    protected float generatedSpeed;

    public ReactorOutputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);

    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        determineSpeed();
    }

    public void determineSpeed() {
        int deterSpeed = this.speed;
        setSpeedAndUpdate(deterSpeed);
    }

    public void setSpeedAndUpdate(int speed) {
        if (generatedSpeed == speed) return;

        generatedSpeed = (float) speed;

        updateGeneratedRotation();
		setChanged();
    }

    // Tracks the output's linked block position for persistence across reloads.
    private BlockPos outputPos;

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);

        // Restore the generated rotation speed
        generatedSpeed = view.getFloatOr("generatedSpeed", 0f);

        // Restore the output position, if present in the tag
        view.getLong("outputPos").ifPresent(pos -> this.outputPos = BlockPos.of(pos));
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);

        // Persist the generated rotation speed
        view.putFloat("generatedSpeed", generatedSpeed);

        // Persist the output position, if set
        if (this.outputPos != null) {
            view.putLong("outputPos", this.outputPos.asLong());
        }
    }

     /**
      * Goggle tooltip body. 26.2 reads goggle tooltips off a client {@code TooltipBehaviour}, not
      * off the block entity, so this is called from the one {@code client.CNBlockEntityBehaviours}
      * registers for this type.
      */
     public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

         float stressBase = calculateAddedStressCapacity();

         CreateLang.translate("gui.goggles.generator_stats")
                 .forGoggles(tooltip);
         CreateLang.translate("tooltip.capacityProvided")
                 .style(ChatFormatting.GRAY)
                 .forGoggles(tooltip);

         float speed = getTheoreticalSpeed();
         speed = Math.abs(speed);

         float stressTotal = stressBase * speed;

         CreateLang.number(stressTotal)
                 .translate("generic.unit.stress")
                 .style(ChatFormatting.AQUA)
                 .space()
                 .add(CreateLang.translate("gui.goggles.at_current_speed")
                         .style(ChatFormatting.DARK_GRAY))
                 .forGoggles(tooltip, 1);
         return true;
     }

    @Override
    public void initialize() {
        super.initialize();

        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
        {
            assert level != null;
            pattern.findController(getBlockPos(), level, true);
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return Mth.clamp(generatedSpeed, 0, 1500000);
    }

}
