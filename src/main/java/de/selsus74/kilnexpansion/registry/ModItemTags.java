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

    //---register charcoal pile igniter tag---
    public static final TagKey<Item> PILE_IGNITERS =
            TagKey.create(Registries.ITEM, new ResourceLocation(KilnExpansion.MOD_ID, "pile_igniters"));

    //---register tag for charcoal pile logs and wood blocks---
    public static final TagKey<Item> CHARCOAL_PILE_LOG =
            TagKey.create(Registries.ITEM, new ResourceLocation(KilnExpansion.MOD_ID, "charable_logs"));
    public static final TagKey<Item> CHARCOAL_PILE_WOOD =
            TagKey.create(Registries.ITEM, new ResourceLocation(KilnExpansion.MOD_ID, "charable_woods"));
}