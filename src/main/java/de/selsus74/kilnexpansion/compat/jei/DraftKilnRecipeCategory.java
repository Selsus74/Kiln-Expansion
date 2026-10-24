package de.selsus74.kilnexpansion.compat.jei;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.recipe.DraftKilnRecipe;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import mezz.jei.api.recipe.RecipeType;

public class DraftKilnRecipeCategory implements IRecipeCategory<DraftKilnRecipe> {
    public static final RecipeType<DraftKilnRecipe> TYPE =
            RecipeType.create(KilnExpansion.MOD_ID, "draft_kiln", DraftKilnRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable flame;          // animated
    private final IDrawableAnimated arrow;  // animated
    private final IDrawable slot;

    public DraftKilnRecipeCategory(IGuiHelper gui) {
        this.background = gui.createBlankDrawable(82, 54);
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.DRAFT_KILN_CONTROLLER.get()));
        this.slot = gui.getSlotDrawable();
        this.flame = gui.createAnimatedRecipeFlame(300);   // burn time animation in ticks
        this.arrow = gui.createAnimatedRecipeArrow(200);   // cook time animation in ticks
    }

    @Override
    public void draw(DraftKilnRecipe recipe, IRecipeSlotsView view, GuiGraphics g, double mx, double my) {
        slot.draw(g, 0, 0);    // input
        slot.draw(g, 0, 36);   // fuel
        slot.draw(g, 56, 14);  // output (vanilla uses a bigger 26x26 output slot, see below)
        flame.draw(g, 1, 20);
        arrow.draw(g, 24, 18);

        //---gets the JSON defined processing time of the recipe and renders it---
        Font font = Minecraft.getInstance().font;
        String text = (recipe.getProcessingTime()) + "T" + " / " + (recipe.getProcessingTime() / 20) + "s";
        float scale = 0.75f;
        float x = 20;
        float y = 5;

        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, 0, 0, 0xFF808080, false);
        g.pose().popPose();
    }

    @Override public RecipeType<DraftKilnRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("block.kilnexpansion.draft_kiln_controller"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DraftKilnRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 57, 15).addItemStack(recipe.getResultItem(null));
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 37)
                .addItemStacks(JeiItemRenderHelper.getFuels())
                .addTooltipCallback((slotView, tooltip) ->
                        slotView.getDisplayedIngredient(VanillaTypes.ITEM_STACK).ifPresent(stack ->
                                tooltip.add(Component.literal("Burn time: " + JeiItemRenderHelper.getBurnTime(stack) + " ticks ("
                                                + JeiItemRenderHelper.getBurnTime(stack) / 20f + "s)")
                                        .withStyle(ChatFormatting.GRAY))));
    }
}