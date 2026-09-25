package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.client.AllFluidConfigs;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.nuclearteam.createnuclear.CNFluids;
import net.nuclearteam.createnuclear.content.fluids.NuclearFluidEntry;

/**
 * Fluid models and fog, replacing the client half of upstream's {@code SolidRenderedPlaceableFluidity}
 * fluid type. Create Fly keeps both in {@link AllFluidConfigs}, keyed by fluid, and its own mixins
 * read them, so registering into those maps is all it takes.
 * <p>
 * The textures live under {@code textures/fluid/}, which is not in the block atlas by default;
 * {@code assets/minecraft/atlases/blocks.json} adds that directory.
 * <p>
 * Upstream also tinted the in-world fluid with its fog colour. The textures are already coloured,
 * and Create Fly resolves world tint through Fabric's fluid variant rendering rather than the
 * model's tint source, so the tint is left off.
 */
public final class CNFluidRenders {
    private CNFluidRenders() {
    }

    public static void register() {
        model(CNFluids.URANIUM);
        model(CNFluids.THORIUM);
        model(CNFluids.LIQUID_NITROGEN);

        // Fog colour and distance are upstream's: 96 blocks scaled by the fluidity factor.
        AllFluidConfigs.fog(CNFluids.URANIUM.get(), 0x38FF08, () -> 96.0f / 32.0f);
        AllFluidConfigs.fog(CNFluids.THORIUM.get(), 0x38F9FF, () -> 96.0f / 32.0f);
        AllFluidConfigs.fog(CNFluids.LIQUID_NITROGEN.get(), 0x23ECD5, () -> 96.0f / 16.0f);
    }

    private static void model(NuclearFluidEntry entry) {
        String name = entry.id.getPath();
        Material still = new Material(entry.id.withPath("fluid/" + name + "_still"));
        Material flow = new Material(entry.id.withPath("fluid/" + name + "_flow"));
        FluidModel.Unbaked model = new FluidModel.Unbaked(still, flow, null, null);
        AllFluidConfigs.MODEL.put(entry.still, model);
        AllFluidConfigs.MODEL.put(entry.flowing, model);
    }
}
