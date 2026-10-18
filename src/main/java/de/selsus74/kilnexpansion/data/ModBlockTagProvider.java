package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup,
                               ExistingFileHelper efh) {
        super(out, lookup, KilnExpansion.MOD_ID, efh);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (String name : ModBlocks.STONE_TYPE_BLOCKS) {
            Block block = ModBlocks.BLOCK_MAP.get(name).get();
            Block wall = ModBlocks.BLOCK_MAP.get(name + "_wall").get();

            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block, wall);
            tag(BlockTags.WALLS).add(wall);
        }
    }
}