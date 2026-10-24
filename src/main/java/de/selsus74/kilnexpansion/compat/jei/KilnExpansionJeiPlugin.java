package de.selsus74.kilnexpansion.compat.jei;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import de.selsus74.kilnexpansion.registry.ModItemTags;
import de.selsus74.kilnexpansion.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class KilnExpansionJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(KilnExpansion.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new DraftKilnRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(
                new BloomeryRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(
                new CharcoalPileRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        RecipeManager rm = Minecraft.getInstance().level.getRecipeManager();
        reg.addRecipes(DraftKilnRecipeCategory.TYPE,
                rm.getAllRecipesFor(ModRecipeTypes.DRAFT_KILN.get()));

        reg.addRecipes(BloomeryRecipeCategory.TYPE,
                rm.getAllRecipesFor(ModRecipeTypes.BLOOMERY.get()));

        //---these are static records for hardcoded block conversion behavior---
        List<CharcoalPileJeiRecipe> CharcoalPileJeiRecipes = new ArrayList<>();

        //---Record 1: all Logs -> Charred Log
        CharcoalPileJeiRecipes.add(new CharcoalPileJeiRecipe(
                Arrays.asList(Ingredient.of(ModItemTags.CHARCOAL_PILE_LOG).getItems()),
                new ItemStack(ModBlocks.CHARRED_LOG.get())));

        //---Record 2: all Wood -> Charred Wood
        CharcoalPileJeiRecipes.add(new CharcoalPileJeiRecipe(
                Arrays.asList(Ingredient.of(ModItemTags.CHARCOAL_PILE_WOOD).getItems()),
                new ItemStack(ModBlocks.CHARRED_WOOD.get())));

        //---add records---
        reg.addRecipes(CharcoalPileRecipeCategory.TYPE, CharcoalPileJeiRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DRAFT_KILN_CONTROLLER.get()), DraftKilnRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOOMERY_CONTROLLER.get()), BloomeryRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHARCOAL_PILE_CONTROLLER.get()), CharcoalPileRecipeCategory.TYPE);
    }
}