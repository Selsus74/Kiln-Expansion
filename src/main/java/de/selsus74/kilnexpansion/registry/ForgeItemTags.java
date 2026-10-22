package de.selsus74.kilnexpansion.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ForgeItemTags {
    //---register generic forge tags---
    //---these are optional tags for compat on bloomery fuel
    public static final TagKey<Item> STORAGE_BLOCKS_CHARCOAL =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/charcoal"));

    public static final TagKey<Item> COAL_COKE =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "coal_coke"));

    public static final TagKey<Item> STORAGE_BLOCKS_COAL_COKE =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/coal_coke"));

    public static final TagKey<Item> COAL_PETCOKE =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "coal_petcoke"));

    public static final TagKey<Item> STORAGE_BLOCKS_COAL_PETCOKE =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/coal_petcoke"));

    public static final TagKey<Item> STORAGE_BLOCKS_PETCOKE =
            TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/petcoke"));
}