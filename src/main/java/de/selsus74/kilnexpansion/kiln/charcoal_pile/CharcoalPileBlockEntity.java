package de.selsus74.kilnexpansion.kiln.charcoal_pile;

import de.selsus74.kilnexpansion.kiln.KilnParticleRenderer;
import de.selsus74.kilnexpansion.multiblock.CharcoalPileStructure;
import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import de.selsus74.kilnexpansion.registry.ModBlockTags;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class CharcoalPileBlockEntity extends BlockEntity {
    private static final int DAY = 240;
    private static final int MIN_TOTAL_TICKS = 5 * DAY;
    private static final int MAX_TOTAL_TICKS = 7 * DAY;
    private static final int CHECK_INTERVAL = 20; // server ticker only checks every 20 ticks

    private final Map<BlockPos, Long> pending = new HashMap<>(); // pos -> gameTime of conversion
    private boolean updating;

    //---constructor---
    public CharcoalPileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHARCOAL_PILE.get(), pos, state);
    }

    //---called on use at block - get wood block pos from structure and append them as pending in the map---
    public boolean tryStart(ServerLevel level) {
        BlockState state = getBlockState();
        if (state.getValue(CharcoalPileControllerBlock.PHASE) != CharcoalPilePhase.IDLE) return false;

        Optional<List<BlockPos>> wood = CharcoalPileStructure.findWoodPositions(
                level, worldPosition, state.getValue(CharcoalPileControllerBlock.FACING));
        if (wood.isEmpty()) return false;

        List<BlockPos> logs = wood.get().stream()
                .filter(p ->
                        level.getBlockState(p).is(ModBlockTags.CHARCOAL_PILE_LOG)
                                || level.getBlockState(p).is(ModBlockTags.CHARCOAL_PILE_WOOD))
                .toList();
        if (logs.isEmpty()) return false;

        RandomSource rnd = level.random;
        long now = level.getGameTime();
        int total = MIN_TOTAL_TICKS + rnd.nextInt(MAX_TOTAL_TICKS - MIN_TOTAL_TICKS + 1);
        int last = rnd.nextInt(logs.size());

        pending.clear();
        for (int i = 0; i < logs.size(); i++) {
            int delay = (i == last) ? total : 1 + rnd.nextInt(total);
            pending.put(logs.get(i), now + delay);
        }

        setChanged();
        setPhase(level, CharcoalPilePhase.BURNING);
        return true;
    }

    //---server ticker - this comes from block get ticker---
    public static void serverTick(Level level, BlockPos pos, BlockState state, CharcoalPileBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (level.getGameTime() % CHECK_INTERVAL != 0) return;

        be.updateFormedState();

        if (state.getValue(CharcoalPileControllerBlock.PHASE) != CharcoalPilePhase.BURNING) return;

        be.convertDueBlocks(serverLevel);
    }

    //---Returns the charred block for a log/wood block, or null if it is not tagged with log---
    @Nullable
    private static Block charredFor(BlockState state) {
        if (state.is(ModBlockTags.CHARCOAL_PILE_LOG)) return ModBlocks.CHARRED_LOG.get();

        if (state.is(ModBlockTags.CHARCOAL_PILE_WOOD)) return ModBlocks.CHARRED_WOOD.get();

        return null;
    }

    //---this does the magic---
    private void convertDueBlocks(ServerLevel level) {
        long now = level.getGameTime();

        //---1) collect + remove due entries; nothing in the world is touched while iterating---
        List<BlockPos> due = new ArrayList<>();
        for (Map.Entry<BlockPos, Long> e : pending.entrySet()) {
            if (e.getValue() <= now) due.add(e.getKey());
        }
        if (!due.isEmpty()) {
            pending.keySet().removeAll(due);
            setChanged();
        }

        //---2) convert; setBlock may re-enter updateFormedState() and clear 'pending', which is now safe---
        for (BlockPos p : due) {
            if (getBlockState().getValue(CharcoalPileControllerBlock.PHASE) != CharcoalPilePhase.BURNING) {
                return; // structure broke during this tick -> process was canceled
            }

            BlockState old = level.getBlockState(p);
            Block charredBlock = charredFor(old);
            if (charredBlock == null) continue;

            BlockState charred = charredBlock.defaultBlockState();
            if (old.hasProperty(RotatedPillarBlock.AXIS) && charred.hasProperty(RotatedPillarBlock.AXIS)) {
                charred = charred.setValue(RotatedPillarBlock.AXIS, old.getValue(RotatedPillarBlock.AXIS));
            }
            level.setBlock(p, charred, Block.UPDATE_ALL);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5,
                    4, 0.2, 0.1, 0.2, 0.01);
        }

        //---3) finished only if we are still burning (an aborted pile also has an empty map!)---
        if (pending.isEmpty()
                && getBlockState().getValue(CharcoalPileControllerBlock.PHASE) == CharcoalPilePhase.BURNING) {
            setPhase(level, CharcoalPilePhase.DONE);
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 0.8f);
        }
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

            if (!valid) {
                pending.clear();
                setChanged();
            }

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

    @Override
    protected void saveAdditional(CompoundTag tag) {
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

    @Override
    public void load(CompoundTag tag) {
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