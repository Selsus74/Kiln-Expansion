package de.selsus74.kilnexpansion;

import com.mojang.logging.LogUtils;
import de.selsus74.kilnexpansion.registry.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

//---mod start point declaration---
@Mod(KilnExpansion.MOD_ID)
public class KilnExpansion {
    public static final String MOD_ID = "kilnexpansion";
    public static final Logger LOGGER = LogUtils.getLogger();

    //---register common resources---
    public KilnExpansion(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
}