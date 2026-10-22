package de.selsus74.kilnexpansion.registry;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.kiln.bloomery.BloomeryBlockEntity;
import de.selsus74.kilnexpansion.kiln.charcoal_pile.CharcoalPileBlockEntity;
import de.selsus74.kilnexpansion.kiln.draft_kiln.DraftKilnBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KilnExpansion.MOD_ID);

    //---register draft kiln block entity type---
    public static final RegistryObject<BlockEntityType<DraftKilnBlockEntity>> DRAFT_KILN =
            BLOCK_ENTITIES.register("draft_kiln", () ->
                    BlockEntityType.Builder.of(DraftKilnBlockEntity::new,
                            ModBlocks.DRAFT_KILN_CONTROLLER.get()).build(null));

    //---register bloomery block entity type---
    public static final RegistryObject<BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            BLOCK_ENTITIES.register("bloomery", () ->
                    BlockEntityType.Builder.of(BloomeryBlockEntity::new,
                            ModBlocks.BLOOMERY_CONTROLLER.get()).build(null));

    //---register charcoal pile block entity type---
    public static final RegistryObject<BlockEntityType<CharcoalPileBlockEntity>> CHARCOAL_PILE =
            BLOCK_ENTITIES.register("charcoal_pile", () ->
                    BlockEntityType.Builder.of(CharcoalPileBlockEntity::new,
                            ModBlocks.CHARCOAL_PILE_CONTROLLER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}