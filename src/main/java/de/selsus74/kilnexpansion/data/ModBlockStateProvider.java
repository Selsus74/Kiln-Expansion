package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput out, ExistingFileHelper efh) {
        super(out, KilnExpansion.MOD_ID, efh);
    }

    @Override
    protected void registerStatesAndModels() {
        for (String name : ModBlocks.STONE_TYPE_BLOCKS) {
            Block block = ModBlocks.BLOCK_MAP.get(name).get();
            ResourceLocation texture = modLoc("block/" + name);

            //---full block + its item model---
            simpleBlockWithItem(block, cubeAll(block));

            //---wall block state + models---
            String wallName = name + "_wall";
            WallBlock wall = (WallBlock) ModBlocks.BLOCK_MAP.get(wallName).get();
            wallBlock(wall, texture);

            //---wall item model (parent is the inventory model)---
            models().wallInventory(wallName + "_inventory", texture);
            itemModels().withExistingParent(wallName, modLoc("block/" + wallName + "_inventory"));
        }
    }
}