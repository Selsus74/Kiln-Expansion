package de.selsus74.kilnexpansion.kiln;

import de.selsus74.kilnexpansion.menu.DraftKilnMenu;
import de.selsus74.kilnexpansion.multiblock.DraftKilnStructure;
import de.selsus74.kilnexpansion.recipe.DraftKilnRecipe;
import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import de.selsus74.kilnexpansion.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
import java.util.Optional;

public class DraftKilnBlockEntity extends BlockEntity implements MenuProvider {

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
        @Override
        public int get(int i) {
            int value = switch (i / 2) {
                case 0 -> burnTime;
                case 1 -> burnTimeTotal;
                case 2 -> progress;
                case 3 -> progressTotal;
                default -> 0;
            };
            return (i % 2 == 0) ? (value & 0xFFFF) : (value >>> 16);
        }
        @Override
        public void set(int i, int value) {
            //never called - Client uses SimpleContainerData
        }
        @Override
        public int getCount() { return 8; }
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
                be.burnTime = Math.max(0, be.burnTime - 2);
                changed = true;
            }
        } else {
            Optional<DraftKilnRecipe> recipe = be.findRecipe();
            boolean canWork = recipe.isPresent() && be.canOutput(recipe.get());

            if (canWork && be.burnTime <= 0) {
                changed |= be.tryConsumeFuel();
            }

            boolean burning = be.burnTime > 0;
            if (burning) {
                be.burnTime--;
                changed = true;
            }

            if (canWork && burning) {
                be.progressTotal = recipe.get().getProcessingTime();
                be.progress++;
                changed = true;
                if (be.progress >= be.progressTotal) {
                    be.craft(recipe.get());
                    be.progress = 0;
                }
            } else if (be.progress > 0) {
                be.progress = Math.max(0, be.progress - 2);
                changed = true;
            }
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

    private Optional<DraftKilnRecipe> findRecipe() {
        SimpleContainer container = new SimpleContainer(items.getStackInSlot(SLOT_INPUT));
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.DRAFT_KILN.get(), container, level);
    }

    private boolean canOutput(DraftKilnRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return items.insertItem(SLOT_OUTPUT, result, true).isEmpty();
    }

    private void craft(DraftKilnRecipe recipe) {
        items.extractItem(SLOT_INPUT, 1, false);
        items.insertItem(SLOT_OUTPUT, recipe.getResultItem(level.registryAccess()).copy(), false);
    }

    public void dropContents() {
        SimpleContainer c = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) c.setItem(i, items.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, c);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.kilnexpansion.draft_kiln");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DraftKilnMenu(id, inventory, items, data, ContainerLevelAccess.create(level, worldPosition));
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