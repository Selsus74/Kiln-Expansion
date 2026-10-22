package de.selsus74.kilnexpansion.kiln.charcoal_pile;

import de.selsus74.kilnexpansion.kiln.KilnParticleRenderer;
import de.selsus74.kilnexpansion.multiblock.CharcoalPileStructure;
import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class CharcoalPileBlockEntity extends BlockEntity {
    private boolean updating;
    private static final int MIN_TICKS = 200, SPREAD_TICKS = 600;
    private final Map<BlockPos, Long> pending = new HashMap<>(); // pos -> finish gameTime

    //---constructor---
    public CharcoalPileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARCOAL_PILE.get(), pos, state);
    }

    public boolean tryStart(ServerLevel level) {
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CharcoalPileBlockEntity be) {

    }

    public void updateFormedState() {
        if (updating || level == null || level.isClientSide) return;
        updating = true;
        try {
            BlockState state = getBlockState();
            boolean valid = CharcoalPileStructure.isValid(level, worldPosition,
                    state.getValue(CharcoalPileControllerBlock.FACING));
            if (valid == state.getValue(CharcoalPileControllerBlock.FORMED)) return;

            BlockState newState = state
                    .setValue(CharcoalPileControllerBlock.FORMED, valid)
                    .setValue(CharcoalPileControllerBlock.PHASE, valid ? CharcoalPilePhase.IDLE : CharcoalPilePhase.NONE);
            level.setBlock(worldPosition, newState, Block.UPDATE_ALL);

            if (valid) KilnParticleRenderer.spawnFormedParticle(level, worldPosition, newState);
        } finally {
            updating = false;
        }
    }

    private void setPhase(Level level, CharcoalPilePhase phase) {
        BlockState current = getBlockState();
        if (current.getValue(CharcoalPileControllerBlock.PHASE) == phase) return;
        level.setBlock(worldPosition, current.setValue(CharcoalPileControllerBlock.PHASE, phase), Block.UPDATE_ALL);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        pending.forEach((p, t) -> {
            CompoundTag c = new CompoundTag();
            c.putLong("pos", p.asLong());
            c.putLong("at", t);
            list.add(c);
        });
        tag.put("pending", list);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        pending.clear();
        for (Tag t : tag.getList("pending", Tag.TAG_COMPOUND)) {
            CompoundTag c = (CompoundTag) t;
            pending.put(BlockPos.of(c.getLong("pos")), c.getLong("at"));
        }
    }

    //---Client-Sync---
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}