package de.selsus74.kilnexpansion.compat.jei;

import de.selsus74.kilnexpansion.registry.ModItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class JeiItemRenderHelper {

    private static List<ItemStack> FUELS;
    private static List<ItemStack> BLOOMERY_FUELS;

    public static List<ItemStack> getFuels() {
        if (FUELS == null) {
            FUELS = ForgeRegistries.ITEMS.getValues().stream()
                    .map(ItemStack::new)
                    .filter(s -> !s.isEmpty() && getBurnTime(s) > 0)
                    .toList();
        }
        return FUELS;
    }

    public static List<ItemStack> getBloomeryFuels() {
        if (BLOOMERY_FUELS == null) {
            BLOOMERY_FUELS = ForgeRegistries.ITEMS.getValues().stream()
                    .map(ItemStack::new)
                    .filter(s -> !s.isEmpty() && s.is(ModItemTags.BLOOMERY_FUEL))
                    .toList();
        }
        return BLOOMERY_FUELS;
    }

    public static int getBurnTime(ItemStack s) {
        return ForgeHooks.getBurnTime(s, RecipeType.SMELTING);
    }
}
