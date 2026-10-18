package de.selsus74.kilnexpansion.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public class ModLootTables {
    public static LootTableProvider create(PackOutput out) {
        return new LootTableProvider(out, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)));
    }
}
