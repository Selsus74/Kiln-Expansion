package de.selsus74.kilnexpansion.registry;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.menu.DraftKilnMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, KilnExpansion.MOD_ID);

    public static final RegistryObject<MenuType<DraftKilnMenu>> DRAFT_KILN_MENU =
            MENUS.register("draft_kiln_menu", () -> IForgeMenuType.create(DraftKilnMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}