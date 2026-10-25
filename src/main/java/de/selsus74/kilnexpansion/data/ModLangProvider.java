package de.selsus74.kilnexpansion.data;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.registry.ModBlocks;
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
        add("container.kilnexpansion.bloomery", "Bloomery");

        add("message.kilnexpansion.draft_kiln.not_formed", "Structure incomplete");
        add("message.kilnexpansion.bloomery.not_formed", "Structure incomplete");
        add("message.kilnexpansion.charcoal_pile.not_formed", "Structure incomplete");

        add("message.kilnexpansion.charcoal_pile.no_igniter", "You can't ignite the Pile with this");
        add("message.kilnexpansion.charcoal_pile.not_ready", "You can't ignite the Pile right now");
        add("message.kilnexpansion.charcoal_pile.no_wood", "The Pile needs at least one Block to process");

        ModBlocks.BLOCKS.getEntries().forEach(block -> {
            if (block == ModBlocks.DRAFT_KILN_CONTROLLER) {
                add(block.get(), "Draft Kiln");
            } else if (block == ModBlocks.BLOOMERY_CONTROLLER) {
                add(block.get(), "Bloomery");
            } else if (block == ModBlocks.CHARCOAL_PILE_CONTROLLER) {
                add(block.get(), "Charcoal Pile");
            } else  {
                add(block.get(), toTitle(block.getId().getPath()));
            }
        });

        //ModItems.ITEMS.getEntries().forEach(item -> add(item.get(), toTitle(item.get().toString())));

        //---generates for every block in block map---
        //for (var entry : ModBlocks.BLOCK_MAP.entrySet()) {
        //    add(entry.getValue().get(), toTitle(entry.getKey()));
        //}

        //---for ponder scenes---
        add("kilnexpansion.ponder.tag.kilns", "Kilns");
        add("kilnexpansion.ponder.tag.kilns.description", "Multiblock furnaces for firing clay, smelting early metals and making charcoal");
        //---add entries for ponder charcoal pile: scene 1---
        add("kilnexpansion.ponder.charcoal_pile_build.header", "Building a Charcoal Pile");
        add("kilnexpansion.ponder.charcoal_pile_build.text_1",
                "Stack logs and wood in the middle"
                        + "\n" + "You can use every Wood or Log tagged with charable_log/charable_wood");
        add("kilnexpansion.ponder.charcoal_pile_build.text_2", "Cover everything in Packed Mud");
        add("kilnexpansion.ponder.charcoal_pile_build.text_3", "Place the Controller to finish the Charcoal Pile");
        //---add entries for ponder charcoal pile: scene 2---
        add("kilnexpansion.ponder.charcoal_pile_burn.header", "Lighting the Charcoal Pile");
        add("kilnexpansion.ponder.charcoal_pile_burn.text_1", "You can use any Item tagged with: pile_igniters");
        add("kilnexpansion.ponder.charcoal_pile_burn.text_2", "Over several days the wood slowly turns into charred blocks, one by one at random");
        add("kilnexpansion.ponder.charcoal_pile_burn.text_3", "When the Particle and Animations stop, the pile is finished and can be opened");
        add("kilnexpansion.ponder.charcoal_pile_burn.text_4", "Breaking the charred wood drops some charcoal");
    }

    private static String toTitle(String id) {
        return Arrays.stream(id.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}