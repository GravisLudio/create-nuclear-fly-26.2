package net.nuclearteam.createnuclear.compat.jei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.FanRenderState;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bulk fan processing with several outputs, laid out like Create Fly's {@code FanHauntingCategory}.
 * Upstream extended Create's {@code ProcessingViaFanCategory.MultiOutput}, which Create Fly
 * replaced with one standalone class per fan type.
 */
public class NuclearFanCategory<R extends Recipe<?>> extends CreateCategory<RecipeHolder<R>> {
    private final IRecipeType<RecipeHolder<R>> type;
    private final Component title;
    private final Item iconSubItem;
    private final Supplier<BlockState> catalystBlock;
    private final Function<R, Ingredient> ingredient;
    private final Function<R, List<ProcessingOutput>> results;

    public NuclearFanCategory(IRecipeType<RecipeHolder<R>> type, Component title, Item iconSubItem, Supplier<BlockState> catalystBlock,
                              Function<R, Ingredient> ingredient, Function<R, List<ProcessingOutput>> results) {
        this.type = type;
        this.title = title;
        this.iconSubItem = iconSubItem;
        this.catalystBlock = catalystBlock;
        this.ingredient = ingredient;
        this.results = results;
    }

    @Override
    public IRecipeType<RecipeHolder<R>> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.PROPELLER, iconSubItem);
    }

    @Override
    public int getHeight() {
        return 72;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> entry, IFocusGroup focuses) {
        R recipe = entry.value();
        List<ProcessingOutput> outputs = results.apply(recipe);
        int outputSize = outputs.size();
        if (outputSize == 1) {
            builder.addInputSlot(21, 48).setBackground(SLOT, -1, -1).add(ingredient.apply(recipe));
            addChanceSlot(builder, 141, 48, outputs.getFirst());
        } else {
            int xOffsetAmount = 1 - Math.min(3, outputSize);
            builder.addInputSlot(21 + xOffsetAmount * 5, 48).setBackground(SLOT, -1, -1).add(ingredient.apply(recipe));
            for (int i = 0, left = 141 + xOffsetAmount * 9, top = outputSize <= 9 ? 48 : 57; i < outputSize; i++) {
                addChanceSlot(builder, left + i % 3 * 19, top + i / 3 * -19, outputs.get(i));
            }
        }
    }

    @Override
    public void draw(RecipeHolder<R> entry, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int xOffsetAmount = 1 - Math.min(3, results.apply(entry.value()).size());
        AllGuiTextures.JEI_SHADOW.render(graphics, 46, 27);
        AllGuiTextures.JEI_LIGHT.render(graphics, 65, 39);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 54 + 7 * xOffsetAmount, 51);
        graphics.guiRenderState.addPicturesInPictureState(new FanRenderState(
            new Matrix3x2f(graphics.pose()),
            56,
            4,
            catalystBlock.get()
        ));
    }
}
