package de.selsus74.kilnexpansion.kiln.charcoal_pile;

import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import de.selsus74.kilnexpansion.registry.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class CharcoalPileControllerBlock extends HorizontalDirectionalBlock implements EntityBlock {

    //---new boolean properties---
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public static final EnumProperty<CharcoalPilePhase> PHASE = EnumProperty.create("phase", CharcoalPilePhase.class);

    //---set the properties---
    public CharcoalPileControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false)
                .setValue(PHASE, CharcoalPilePhase.NONE));
    }

    //---block state definition---
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING,  FORMED, PHASE);
    }

    //---state when placed---
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(FORMED, false)
                .setValue(PHASE, CharcoalPilePhase.NONE);
    }

    //---attach block entity---
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CharcoalPileBlockEntity(pos, state);
    }

    //---called on block direct neighbor changed---
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CharcoalPileBlockEntity be) {
            be.updateFormedState();
        }
    }

    //---called when placed---
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CharcoalPileBlockEntity be) {
            be.updateFormedState();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack held = player.getItemInHand(hand);
        if (!state.getValue(FORMED)) {
            player.displayClientMessage(Component.translatable("message.kilnexpansion.charcoal_pile.not_formed"), true);
            return InteractionResult.CONSUME;
        }
        if (!held.is(ModItemTags.PILE_IGNITERS)) {
            player.displayClientMessage(Component.translatable("message.kilnexpansion.charcoal_pile.no_igniter"), true);
            return InteractionResult.CONSUME;
        }
        if (state.getValue(PHASE) != CharcoalPilePhase.IDLE) {
            player.displayClientMessage(Component.translatable("message.kilnexpansion.charcoal_pile.not_ready"), true);
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof CharcoalPileBlockEntity be && be.tryStart((ServerLevel) level)) {
            if (!player.getAbilities().instabuild) held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand)); // or held.shrink(1)
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.kilnexpansion.charcoal_pile.no_wood"), true);
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState s, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.CHARCOAL_PILE.get()
                ? (lvl, pos, st, be) -> CharcoalPileBlockEntity.serverTick(lvl, pos, st, (CharcoalPileBlockEntity) be)
                : null;
    }

    //---particle renderer (client side, needs no packets like KilnParticleRenderer---
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PHASE) != CharcoalPilePhase.BURNING) {return;}

        //---positions for smoke---
        BlockPos pos1 = pos.relative(state.getValue(FACING).getOpposite()).above();
        double x1 = pos1.getX() + 0.5;
        double y1 = pos1.getY() + 3.05;
        double z1 = pos1.getZ() + 0.5;

        double x2 = pos1.getX() + 0.5 - 1;
        double y2 = pos1.getY() + 0.05;
        double z2 = pos1.getZ() + 0.5 + 3;

        double x3 = pos1.getX() + 0.5 - 2;
        double y3 = pos1.getY() + 1.05;
        double z3 = pos1.getZ() + 0.5 - 3;

        double x4 = pos1.getX() + 0.5 - 3;
        double y4 = pos1.getY() + 4.05;
        double z4 = pos1.getZ() + 0.5 + 1;

        double x5 = pos1.getX() + 0.5 - 5;
        double y5 = pos1.getY() + 0.05;
        double z5 = pos1.getZ() + 0.5 - 1;

        //---generates smoke on the pile---
        if (random.nextInt(1) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x1, y1, z1, 0.0, 0.07, 0.0);
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x4, y4, z4, 0.0, 0.07, 0.0);
        }
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x2, y2, z2, 0.0, 0.07, 0.0);
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x3, y3, z3, 0.0, 0.07, 0.0);
            level.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x5, y5, z5, 0.0, 0.07, 0.0);
        }

        //---plays furnace fire sounds---
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false);
        }
    }
}