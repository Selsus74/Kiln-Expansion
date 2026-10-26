package de.selsus74.kilnexpansion.compat.ponder;

import de.selsus74.kilnexpansion.kiln.charcoal_pile.CharcoalPileControllerBlock;
import de.selsus74.kilnexpansion.kiln.charcoal_pile.CharcoalPilePhase;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CharcoalPileScenes {

    private static final Set<String> SHELL_BLOCKS = Set.of(
            "minecraft:packed_mud",
            "kilnexpansion:charcoal_pile_controller");

    private static final Set<String> CORE_BLOCKS = Set.of(
            "minecraft:oak_log",
            "kilnexpansion:charcoal_pile_controller");

    public static void build(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("charcoal_pile_build", String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_build.header")));
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(10);

        // core = everything that is not shell, grouped by layer
        Map<Integer, List<BlockPos>> core = SchematicSelectionsHelper.blocksByLayer("charcoal_pile/build", SHELL_BLOCKS);
        // shell = everything that is not core, grouped by layer
        Map<Integer, List<BlockPos>> shell = SchematicSelectionsHelper.blocksByLayer("charcoal_pile/build", CORE_BLOCKS);

        BlockPos controller = util.grid().at(3, 1, 0);

        for (List<BlockPos> layer : core.values()) {
            scene.world().showSection(SchematicSelectionsHelper.of(util, layer), Direction.DOWN);
            scene.idle(15);
        }
        scene.addKeyframe();
        scene.overlay().showText(150).text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_build.text_1")))
                .pointAt(util.vector().centerOf(core.values().iterator().next().get(0)))
                .placeNearTarget();
        scene.idle(120);

        for (List<BlockPos> layer : shell.values()) {
            scene.world().showSection(SchematicSelectionsHelper.of(util, layer), Direction.DOWN);
            scene.idle(15);
        }
        scene.addKeyframe();
        scene.overlay().showText(70).text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_build.text_2")))
                .pointAt(util.vector().topOf(3, shell.keySet().stream().max(Integer::compare).get(), 3))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(50).text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_build.text_3")))
                .pointAt(util.vector().topOf(controller))
                .placeNearTarget();
        scene.world().showSection(util.select().position(controller), Direction.SOUTH);
        scene.idle(30);
    }

    public static void burn(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("charcoal_pile_burn", "Lighting the Pile");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(20);

        //---click with flint---
        BlockPos controller = util.grid().at(3, 1, 0);
        scene.overlay().showText(70).text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_burn.text_1")))
                .pointAt(util.vector().topOf(3, 1, 0))
                .attachKeyFrame()
                .placeNearTarget();
        scene.overlay().showControls(
                        util.vector().blockSurface(controller, Direction.NORTH),
                        Pointing.RIGHT,
                        40)
                .rightClick()
                .withItem(new ItemStack(Items.FLINT_AND_STEEL));
        scene.world().modifyBlock(controller, state -> state.setValue(CharcoalPileControllerBlock.PHASE, CharcoalPilePhase.BURNING), false);
        scene.idle(80);

        //---half the thing---
        BlockPos size = SchematicSelectionsHelper.size("charcoal_pile/build"); // x, y, z of the structure
        int midZ = size.getZ() / 2;
        Selection frontHalf = util.select().fromTo(
                0, 1, 0,
                size.getX() - 1, size.getY() - 1, midZ - 1);
        scene.world().hideSection(frontHalf, Direction.UP);

        scene.addKeyframe();

        //---text wood conversion---
        scene.overlay().showText(80)
                .text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_burn.text_2")))
                .pointAt(util.vector().centerOf(3, 2, 3))
                .placeNearTarget();
        scene.idle(90);

        //---show conversion---
        // core = everything that is not shell, grouped by layer
        Map<Integer, List<BlockPos>> core = SchematicSelectionsHelper.blocksByLayer("charcoal_pile/build", SHELL_BLOCKS);

        BlockState charredLog = ModBlocks.CHARRED_LOG.get().defaultBlockState();
        for (List<BlockPos> layer : core.values()) {
            for (BlockPos pos : layer) {
                scene.world().replaceBlocks(util.select().position(pos), charredLog, false);
                scene.idle(2);
            }
        }
        scene.idle(90);
        scene.addKeyframe();

        //---add back the half---
        scene.world().showSection(frontHalf, Direction.DOWN);

        //---when done---
        // shell = everything that is not core, grouped by layer
        Map<Integer, List<BlockPos>> shell = SchematicSelectionsHelper.blocksByLayer("charcoal_pile/build", CORE_BLOCKS);

        scene.overlay().showText(70)
                .text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_burn.text_3")))
                .pointAt(util.vector().topOf(controller))
                .placeNearTarget();
        scene.idle(50);

        scene.world().modifyBlock(controller, state -> state.setValue(CharcoalPileControllerBlock.PHASE, CharcoalPilePhase.DONE), false);
        scene.addKeyframe();
        scene.idle(90);

        for (List<BlockPos> layer : shell.values()) {
            scene.world().hideSection(SchematicSelectionsHelper.of(util, layer), Direction.UP);
        }

        scene.idle(50);

        scene.overlay().showText(70)
                .text(String.valueOf(Component.translatable("kilnexpansion.ponder.charcoal_pile_burn.text_4")))
                .pointAt(util.vector().topOf(3, 2, 1))
                .placeNearTarget();
        scene.idle(50);
    }
}