package de.selsus74.kilnexpansion.registry;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.recipe.BloomeryRecipe;
import de.selsus74.kilnexpansion.recipe.DraftKilnRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, KilnExpansion.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, KilnExpansion.MOD_ID);

    //---registers draft kiln recipe type and serializer---
    public static final RegistryObject<RecipeType<DraftKilnRecipe>> DRAFT_KILN =
            RECIPE_TYPES.register("draft_kiln",
                    () -> RecipeType.simple(new ResourceLocation(KilnExpansion.MOD_ID, "draft_kiln")));

    public static final RegistryObject<RecipeSerializer<DraftKilnRecipe>> DRAFT_KILN_SERIALIZER =
            SERIALIZERS.register("draft_kiln", DraftKilnRecipe.Serializer::new);

    //---registers bloomery recipe type and serializer---
    public static final RegistryObject<RecipeType<BloomeryRecipe>> BLOOMERY =
            RECIPE_TYPES.register("bloomery",
                    () -> RecipeType.simple(new ResourceLocation(KilnExpansion.MOD_ID, "bloomery")));

    public static final RegistryObject<RecipeSerializer<BloomeryRecipe>> BLOOMERY_SERIALIZER =
            SERIALIZERS.register("bloomery", BloomeryRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }
}