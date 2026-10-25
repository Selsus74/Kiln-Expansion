package de.selsus74.kilnexpansion.compat.ponder;

import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SchematicSelectionsHelper {

    //---helper for shell or core - can filter structure by excludes---
    //---Layer (y) -> positions of all blocks whose registry name is NOT in `excluded`---
    public static Map<Integer, List<BlockPos>> blocksByLayer(String schematicPath, @Nullable Set<String> excluded) {
        Map<Integer, List<BlockPos>> byLayer = new TreeMap<>();
        ResourceLocation file = new ResourceLocation("kilnexpansion", "ponder/" + schematicPath + ".nbt");
        try (InputStream in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(file).open()) {
            CompoundTag tag = NbtIo.readCompressed(in);
            ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
            ListTag blocks = tag.getList("blocks", Tag.TAG_COMPOUND);

            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag b = blocks.getCompound(i);
                String name = palette.getCompound(b.getInt("state")).getString("Name");
                ListTag pos = b.getList("pos", Tag.TAG_INT);
                int y = pos.getInt(1);

                if (y == 0) continue; // baseplate
                if (name.equals("minecraft:air")) continue;
                if (excluded != null && excluded.contains(name)) continue;

                byLayer.computeIfAbsent(y, k -> new ArrayList<>())
                        .add(new BlockPos(pos.getInt(0), y, pos.getInt(2)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read ponder schematic " + schematicPath, e);
        }
        return byLayer;
    }

    public static Selection of(SceneBuildingUtil util, List<BlockPos> positions) {
        Selection sel = util.select().position(positions.get(0));
        for (int i = 1; i < positions.size(); i++) {
            sel = sel.add(util.select().position(positions.get(i)));
        }
        return sel;
    }

    public static BlockPos size(String schematicPath) {
        ResourceLocation file = new ResourceLocation("kilnexpansion", "ponder/" + schematicPath + ".nbt");
        try (InputStream in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(file).open()) {
            CompoundTag tag = NbtIo.readCompressed(in);
            // open the file like in blocksByLayer, then:
            ListTag s = tag.getList("size", Tag.TAG_INT);
            return new BlockPos(s.getInt(0), s.getInt(1), s.getInt(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
