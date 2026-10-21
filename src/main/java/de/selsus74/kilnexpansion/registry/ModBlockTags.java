package de.selsus74.kilnexpansion.registry;

import de.selsus74.kilnexpansion.KilnExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    //---block tags for draft kiln blocks---
    public static final TagKey<Block> DRAFT_KILN_BLOCKS =
            TagKey.create(Registries.BLOCK, new ResourceLocation(KilnExpansion.MOD_ID, "is_draft_kiln_block"));

    public static final TagKey<Block> DRAFT_KILN_WALLS =
            TagKey.create(Registries.BLOCK, new ResourceLocation(KilnExpansion.MOD_ID, "is_draft_kiln_wall"));

    //---block tags for bloomery blocks---
    public static final TagKey<Block> BLOOMERY_BLOCKS =
            TagKey.create(Registries.BLOCK, new ResourceLocation(KilnExpansion.MOD_ID, "is_bloomery_block"));

    public static final TagKey<Block> BLOOMERY_WALLS =
            TagKey.create(Registries.BLOCK, new ResourceLocation(KilnExpansion.MOD_ID, "is_bloomery_wall"));

    public static final TagKey<Block> BLOOMERY_SLABS =
            TagKey.create(Registries.BLOCK, new ResourceLocation(KilnExpansion.MOD_ID, "is_bloomery_slab"));
}