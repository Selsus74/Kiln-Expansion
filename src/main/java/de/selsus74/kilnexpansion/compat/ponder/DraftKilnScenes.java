package de.selsus74.kilnexpansion.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;

public class DraftKilnScenes {

    public static void build(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("draft_kiln_build", "kilnexpansion.ponder.charcoal_pile_build.header");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(50);

        Map<Integer, List<BlockPos>> kiln = SchematicSelectionsHelper.blocksByLayer("draft_kiln/build", null);

        for (List<BlockPos> layer : kiln.values()) {
            scene.world().showSection(SchematicSelectionsHelper.of(util, layer), Direction.DOWN);
            scene.addKeyframe();
            scene.idle(30);
        }
        scene.idle(100);
    }
}