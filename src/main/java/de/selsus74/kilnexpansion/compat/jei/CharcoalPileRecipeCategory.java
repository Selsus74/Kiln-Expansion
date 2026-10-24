package de.selsus74.kilnexpansion.compat.jei;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CharcoalPileRecipeCategory implements IRecipeCategory<CharcoalPileJeiRecipe> {
    public static final RecipeType<CharcoalPileJeiRecipe> TYPE =
            RecipeType.create(KilnExpansion.MOD_ID, "charcoal_pile", CharcoalPileJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public CharcoalPileRecipeCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(100, 26);
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.CHARCOAL_PILE_CONTROLLER.get()));
        this.arrow = gui.getRecipeArrow();
    }

    @Override public RecipeType<CharcoalPileJeiRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.kilnexpansion.charcoal_pile_controller"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CharcoalPileJeiRecipe r, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5).addItemStacks(r.inputs());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 77, 5).addItemStack(r.output());
    }

    @Override
    public void draw(CharcoalPileJeiRecipe r, IRecipeSlotsView v, GuiGraphics g, double mx, double my) {
        arrow.draw(g, 36, 5);
    }
}