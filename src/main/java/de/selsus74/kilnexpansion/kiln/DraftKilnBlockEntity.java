package de.selsus74.kilnexpansion.kiln;

import de.selsus74.kilnexpansion.multiblock.DraftKilnStructure;
import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class DraftKilnBlockEntity extends BlockEntity {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> items);

    private int burnTime;
    private int burnTimeTotal;
    private int progress;
    private int progressTotal;

    //---data for gui sync---
    public final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> burnTime;
                case 1 -> burnTimeTotal;
                case 2 -> progress;
                case 3 -> progressTotal;
                default -> 0;
            };
        }
        @Override public void set(int i, int value) {
            switch (i) {
                case 0 -> burnTime = value;
                case 1 -> burnTimeTotal = value;
                case 2 -> progress = value;
                case 3 -> progressTotal = value;
            }
        }
        @Override public int getCount() { return 4; }
    };

    public DraftKilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRAFT_KILN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DraftKilnBlockEntity be) {
        if (level.getGameTime() % 20 == 0) {
            be.updateFormedState();
        }
        state = be.getBlockState();

        boolean changed = false;

        if (!state.getValue(DraftKilnControllerBlock.FORMED)) {
            if (be.progress != 0 || be.burnTime != 0) {
                be.progress = 0;
                be.burnTime = Math.max(0, be.burnTime - 2); //cools down
                changed = true;
            }
        } else {
            if (be.burnTime > 0) {
                be.burnTime--;
                changed = true;
            }
            // TODO  Rezept suchen, Fuel verbrauchen, progress hochzählen, Output erzeugen
        }

        if (changed) be.setChanged();
    }

    public void updateFormedState() {
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        boolean valid = DraftKilnStructure.isValid(level, worldPosition, state.getValue(DraftKilnControllerBlock.FACING));
        if (valid != state.getValue(DraftKilnControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(DraftKilnControllerBlock.FORMED, valid), Block.UPDATE_ALL);
        }
    }

    //---fuel helper - use in recipe handler---
    boolean tryConsumeFuel() {
        ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
        int time = ForgeHooks.getBurnTime(fuel, null);
        if (time <= 0) return false;
        burnTime = burnTimeTotal = time;
        fuel.shrink(1); // TODO ITEMS WIE LAVA MÜSSEN SEPERAT BEHANDLET WERDEN !
        return true;
    }

    public void dropContents() {
        SimpleContainer c = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) c.setItem(i, items.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, c);
    }

    //---save / load---
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putInt("Progress", progress);
        tag.putInt("ProgressTotal", progressTotal);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Items"));
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        progress = tag.getInt("Progress");
        progressTotal = tag.getInt("ProgressTotal");
    }

    //---Capability(Hopper etc.)---
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
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