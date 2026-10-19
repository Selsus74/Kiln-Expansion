package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import de.selsus74.kilnexpansion.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

//---generates lang file entries---
public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(PackOutput out) {
        super(out, KilnExpansion.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        //---creative tab title---
        add("itemGroup.kilnexpansion", "Kiln Expansion");

        add("container.kilnexpansion.draft_kiln", "Draft Kiln");
        add("message.kilnexpansion.draft_kiln.not_formed", "Structure incomplete");

        ModBlocks.BLOCKS.getEntries().forEach(block -> {
            if (block == ModBlocks.DRAFT_KILN_CONTROLLER) {
                add(block.get(), "Draft Kiln");
            }
            else {
                add(block.get(), toTitle(block.getId().getPath()));
            }
        });

        //ModItems.ITEMS.getEntries().forEach(item -> add(item.get(), toTitle(item.get().toString())));

        //---generates for every block in block map---
        //for (var entry : ModBlocks.BLOCK_MAP.entrySet()) {
        //    add(entry.getValue().get(), toTitle(entry.getKey()));
        //}
    }

    private static String toTitle(String id) {
        return Arrays.stream(id.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}