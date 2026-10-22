package de.selsus74.kilnexpansion.kiln.charcoal_pile;

import de.selsus74.kilnexpansion.kiln.bloomery.BloomeryBlockEntity;
import de.selsus74.kilnexpansion.kiln.draft_kiln.DraftKilnBlockEntity;
import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import de.selsus74.kilnexpansion.registry.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
        if (!held.is(ModItemTags.PILE_IGNITERS)) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof CharcoalPileBlockEntity be && be.tryStart((ServerLevel) level)) {
            if (!player.getAbilities().instabuild) held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand)); // or held.shrink(1)
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState s, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.CHARCOAL_PILE.get()
                ? (lvl, pos, st, be) -> CharcoalPileBlockEntity.serverTick(lvl, pos, st, (CharcoalPileBlockEntity) be)
                : null;
    }
}