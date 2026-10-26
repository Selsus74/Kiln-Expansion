package de.selsus74.kilnexpansion.compat.ponder;

import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class KilnExpansionPonderPlugin implements PonderPlugin {
    @Override public String getModId() { return "kilnexpansion"; }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {

        helper.forComponents(ModBlocks.DRAFT_KILN_CONTROLLER.getId())
                .addStoryBoard("draft_kiln/build", DraftKilnScenes::build, KilnExpansionPonderTags.KILNS);

        helper.forComponents(ModBlocks.BLOOMERY_CONTROLLER.getId())
                .addStoryBoard("bloomery/build", BloomeryScenes::build, KilnExpansionPonderTags.KILNS);

        helper.forComponents(ModBlocks.CHARCOAL_PILE_CONTROLLER.getId())
                .addStoryBoard("charcoal_pile/build", CharcoalPileScenes::build, KilnExpansionPonderTags.KILNS)
                .addStoryBoard("charcoal_pile/build", CharcoalPileScenes::burn);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(KilnExpansionPonderTags.KILNS)
                .addToIndex()
                .item(ModBlocks.DRAFT_KILN_CONTROLLER_ITEM.get())
                .title(String.valueOf(Component.translatable("kilnexpansion.ponder.tag.kilns")))
                .description(String.valueOf(Component.translatable("kilnexpansion.ponder.tag.kilns.description")))
                .register();

        helper.addToTag(KilnExpansionPonderTags.KILNS)
                .add(ModBlocks.DRAFT_KILN_CONTROLLER.getId())
                .add(ModBlocks.BLOOMERY_CONTROLLER.getId())
                .add(ModBlocks.CHARCOAL_PILE_CONTROLLER.getId());
    }
}