package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class ModBlockLoot extends BlockLootSubProvider {
    protected ModBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        Set<Block> custom = Set.of(ModBlocks.CHARRED_LOG.get(), ModBlocks.CHARRED_WOOD.get());

        //---drop self if not charred wood---
        getKnownBlocks().forEach(block -> {
            if (!custom.contains(block)) dropSelf(block);
        });

        //---charred wood/log 6-9 charcoal, with silk touch drops self---
        add(ModBlocks.CHARRED_LOG.get(), this::charcoalOrSelf);
        add(ModBlocks.CHARRED_WOOD.get(), this::charcoalOrSelf);
    }

    private LootTable.Builder charcoalOrSelf(Block block) {
        return createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(Items.CHARCOAL)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(6, 9)))
                        .apply(ApplyExplosionDecay.explosionDecay()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}