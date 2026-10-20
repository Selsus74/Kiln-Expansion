package de.selsus74.kilnexpansion.kiln;

import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class DraftKilnControllerBlock extends HorizontalDirectionalBlock implements EntityBlock {

    //---new boolean properties---
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    //---set the properties---
    public DraftKilnControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(FORMED, false));
    }

    //---block state definition---
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, FORMED);
    }

    //---state when placed---
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false)
                .setValue(FORMED, false);
    }

    //---attach block entity---
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DraftKilnBlockEntity(pos, state);
    }

    //---ticking behavior - important for serverTick in BlockEntity---
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.DRAFT_KILN.get()
                ? (lvl, pos, st, be) -> DraftKilnBlockEntity.serverTick(lvl, pos, (DraftKilnBlockEntity) be)
                : null;
    }

    //---particle renderer (client side, needs no packets like KilnParticleRenderer---
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        //--opening position: center of the top layer---
        BlockPos vent = pos.relative(state.getValue(FACING).getOpposite()).above();
        double x = vent.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
        double y = vent.getY() + 1.05;
        double z = vent.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3;

        //---generates smoke in the chimney---
        if (random.nextInt(1) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0, 0.07, 0.0);
        }

        //---plays campfire fire sounds---
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }
    }

    //---called on block removal---
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DraftKilnBlockEntity be) {
                be.dropContents();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    //---called on block direct neighbor changed---
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DraftKilnBlockEntity be) {
            be.updateFormedState();
        }
    }

    //---called when placed---
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DraftKilnBlockEntity be) {
            be.updateFormedState();
        }
    }

    //---called when interacted with---
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (!state.getValue(FORMED)) {
            player.displayClientMessage(Component.translatable("message.kilnexpansion.draft_kiln.not_formed"), true);
            return InteractionResult.CONSUME;
        }

        if (level.getBlockEntity(pos) instanceof DraftKilnBlockEntity be) {
            NetworkHooks.openScreen((ServerPlayer) player, be, pos);
        }
        return InteractionResult.CONSUME;
    }
}