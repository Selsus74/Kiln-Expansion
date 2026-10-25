package de.selsus74.kilnexpansion.client;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.compat.ponder.KilnExpansionPonderPlugin;
import de.selsus74.kilnexpansion.registry.ModMenuTypes;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = KilnExpansion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.DRAFT_KILN_MENU.get(), DraftKilnScreen::new);
            MenuScreens.register(ModMenuTypes.BLOOMERY_MENU.get(), BloomeryScreen::new);

            //---registers Ponder Scenes---
            PonderIndex.addPlugin(new KilnExpansionPonderPlugin());
        });
    }
}