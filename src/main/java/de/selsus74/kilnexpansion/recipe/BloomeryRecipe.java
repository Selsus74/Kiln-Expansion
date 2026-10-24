package de.selsus74.kilnexpansion.recipe;

import com.google.gson.JsonObject;
import de.selsus74.kilnexpansion.registry.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class BloomeryRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final int processingTime;

    public BloomeryRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int processingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.processingTime = processingTime;
    }

    public int getProcessingTime() { return processingTime; }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return ingredient.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(RegistryAccess access) { return result; }

    public Ingredient getIngredient() { return ingredient; }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ResourceLocation getId() { return id; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipeTypes.BLOOMERY_SERIALIZER.get(); }

    @Override
    public RecipeType<?> getType() { return ModRecipeTypes.BLOOMERY.get(); }

    public static class Serializer implements RecipeSerializer<BloomeryRecipe> {

        @Override
        public BloomeryRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(json, "ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "processing_time", 200);
            return new BloomeryRecipe(id, ingredient, result, time);
        }

        @Override
        public BloomeryRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            int time = buf.readVarInt();
            return new BloomeryRecipe(id, ingredient, result, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, BloomeryRecipe recipe) {
            recipe.ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
            buf.writeVarInt(recipe.processingTime);
        }
    }
}