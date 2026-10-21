package de.selsus74.kilnexpansion.registry;

import de.selsus74.kilnexpansion.KilnExpansion;
import de.selsus74.kilnexpansion.kiln.BloomeryControllerBlock;
import de.selsus74.kilnexpansion.kiln.DraftKilnControllerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.selsus74.kilnexpansion.registry.ModItems.ITEMS;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, KilnExpansion.MOD_ID);

    //---list of stone like blocks (e.g. bricks) and their corresponding wall---
    public static final List<String> STONE_TYPE_BLOCKS = List.of(
            "draft_kiln_bricks",
            "bloomery_bricks"
    );

    //---map name to registry objects---
    public static final Map<String, RegistryObject<Block>> BLOCK_MAP = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> ITEM_MAP = new LinkedHashMap<>();

    static {
        for (String name : STONE_TYPE_BLOCKS) {
            //---registers blocks in this class---
            RegistryObject<Block> block = BLOCKS.register(name,
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .sound(SoundType.STONE)
                            .pushReaction(PushReaction.BLOCK)
                            .requiresCorrectToolForDrops()));

            //---registers items in ModItems class---
            RegistryObject<Item> item = ITEMS.register(name,
                    () -> new BlockItem(block.get(), new Item.Properties()));

            //---slab variant of the same block---
            String slabName = name + "_slab";
            RegistryObject<Block> slab = BLOCKS.register(slabName,
                    () -> new SlabBlock(BlockBehaviour.Properties.copy(block.get())));

            RegistryObject<Item> slabItem = ITEMS.register(slabName,
                    () -> new BlockItem(slab.get(), new Item.Properties()));

            //---wall variant of the same block---
            String wallName = name + "_wall";
            RegistryObject<Block> wall = BLOCKS.register(wallName,
                    () -> new WallBlock(BlockBehaviour.Properties.copy(block.get())));
            RegistryObject<Item> wallItem = ITEMS.register(wallName,
                    () -> new BlockItem(wall.get(), new Item.Properties()));

            BLOCK_MAP.put(name, block);
            ITEM_MAP.put(name, item);
            BLOCK_MAP.put(slabName, slab);
            ITEM_MAP.put(slabName, slabItem);
            BLOCK_MAP.put(wallName, wall);
            ITEM_MAP.put(wallName, wallItem);
        }
    }

    //---draft kiln controller block + block item---
    public static final RegistryObject<Block> DRAFT_KILN_CONTROLLER = BLOCKS.register(
            "draft_kiln_controller",
            () -> new DraftKilnControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 9 : 0)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> DRAFT_KILN_CONTROLLER_ITEM = ITEMS.register(
            "draft_kiln_controller",
            () -> new BlockItem(DRAFT_KILN_CONTROLLER.get(), new Item.Properties()));

    //---bloomery controller block + block item---
    public static final RegistryObject<Block> BLOOMERY_CONTROLLER = BLOCKS.register(
            "bloomery_controller",
            () -> new BloomeryControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> BLOOMERY_CONTROLLER_ITEM = ITEMS.register(
            "bloomery_controller",
            () -> new BlockItem(BLOOMERY_CONTROLLER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}