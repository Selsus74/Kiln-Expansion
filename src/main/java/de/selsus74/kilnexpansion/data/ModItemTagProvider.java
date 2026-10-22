package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ForgeItemTags;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import de.selsus74.kilnexpansion.registry.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {

    public ModItemTagProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> tagLookupBlock, ExistingFileHelper existingFileHelper) {
        super(out, lookupProvider, tagLookupBlock, KilnExpansion.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //---bloomery fuel tag added to following existing or optional tags---
        tag(ModItemTags.BLOOMERY_FUEL)
                .addTag(ItemTags.COALS)
                .addTag(Tags.Items.STORAGE_BLOCKS_COAL)
                .addOptionalTag(ForgeItemTags.STORAGE_BLOCKS_CHARCOAL)
                .addOptionalTag(ForgeItemTags.COAL_COKE)
                .addOptionalTag(ForgeItemTags.STORAGE_BLOCKS_COAL_COKE)
                .addOptionalTag(ForgeItemTags.COAL_PETCOKE)
                .addOptionalTag(ForgeItemTags.STORAGE_BLOCKS_COAL_PETCOKE)
                .addOptionalTag(ForgeItemTags.STORAGE_BLOCKS_PETCOKE);
    }
}
