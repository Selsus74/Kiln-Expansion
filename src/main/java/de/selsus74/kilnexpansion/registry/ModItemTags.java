package de.selsus74.kilnexpansion.registry;

import de.selsus74.kilnexpansion.KilnExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    //---register bloomery fuel tag---
    public static final TagKey<Item> BLOOMERY_FUEL =
            TagKey.create(Registries.ITEM, new ResourceLocation(KilnExpansion.MOD_ID, "bloomery_fuel"));
}