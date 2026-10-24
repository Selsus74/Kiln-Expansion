package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlockTags;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup,
                               ExistingFileHelper efh) {
        super(out, lookup, KilnExpansion.MOD_ID, efh);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //---standard wall, slab and mineable tags---
        for (String name : ModBlocks.STONE_TYPE_BLOCKS) {
            Block block = ModBlocks.BLOCK_MAP.get(name).get();
            Block wall = ModBlocks.BLOCK_MAP.get(name + "_wall").get();
            Block slab = ModBlocks.BLOCK_MAP.get(name + "_slab").get();

            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block, wall, slab);
            tag(BlockTags.WALLS).add(wall);
            tag(BlockTags.SLABS).add(slab);
        }
        //---custom tags for structure declaration---
        tag(ModBlockTags.DRAFT_KILN_BLOCKS).add(ModBlocks.BLOCK_MAP.get("draft_kiln_bricks").get());
        tag(ModBlockTags.DRAFT_KILN_WALLS).add(ModBlocks.BLOCK_MAP.get("draft_kiln_bricks_wall").get());

        tag(ModBlockTags.BLOOMERY_BLOCKS).add(ModBlocks.BLOCK_MAP.get("bloomery_bricks").get());
        tag(ModBlockTags.BLOOMERY_WALLS).add(ModBlocks.BLOCK_MAP.get("bloomery_bricks_wall").get());
        tag(ModBlockTags.BLOOMERY_SLABS).add(ModBlocks.BLOCK_MAP.get("bloomery_bricks_slab").get());

        //---pile logs and woods---
        for (Block block : ForgeRegistries.BLOCKS) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            String path = id.getPath();

            if (id.getNamespace().equals(KilnExpansion.MOD_ID)) continue;   // exclude own blocks (charred_log etc.)
            if (path.contains("mushroom")) continue;

            if (path.endsWith("_log")) {
                tag(ModBlockTags.CHARCOAL_PILE_LOG).add(block);
            } else if (path.endsWith("_wood")) {
                tag(ModBlockTags.CHARCOAL_PILE_WOOD).add(block);
            }
        }
    }
}