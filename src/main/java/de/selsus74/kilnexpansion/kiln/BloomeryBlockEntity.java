package de.selsus74.kilnexpansion.kiln;

import de.selsus74.kilnexpansion.menu.BloomeryMenu;
import de.selsus74.kilnexpansion.multiblock.BloomeryStructure;
import de.selsus74.kilnexpansion.recipe.BloomeryRecipe;
import de.selsus74.kilnexpansion.registry.ModBlockEntities;
import de.selsus74.kilnexpansion.registry.ModItemTags;
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

import static de.selsus74.kilnexpansion.kiln.KilnParticleRenderer.spawnFormedParticle;

public class BloomeryBlockEntity extends BlockEntity implements MenuProvider {

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
        public int getCount() {
            return 8;
        }
    };

    //---constructor---
    public BloomeryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLOOMERY.get(), pos, state);
    }


    public static void serverTick(Level level, BlockPos pos, BloomeryBlockEntity be) {
        //---every 20 game ticks check the structure---
        if (level.getGameTime() % 20 == 0) {
            be.updateFormedState();
        }
        BlockState state = be.getBlockState();
        boolean changed = false;

        //---check formed, if not reset progress---
        if (!state.getValue(BloomeryControllerBlock.FORMED)) {
            if (be.progress != 0 || be.burnTime != 0) {
                be.progress = 0;
                be.burnTime = Math.max(0, be.burnTime - 2);
                changed = true;
            }
            //---also set lit block property false---
            level.setBlock(pos, state.setValue(BloomeryControllerBlock.LIT, false), Block.UPDATE_ALL);
        } else {
            //---get recipes---
            Optional<BloomeryRecipe> recipe = be.findRecipe();
            //---make boolean to determine if the kiln can work---
            boolean canWork = recipe.isPresent() && be.canOutput(recipe.get());

            //---recipe is valid but burnTime is 0, try to consume fuel---
            if (canWork && be.burnTime <= 0) {
                changed |= be.tryConsumeFuel();
            }

            //---burning when burnTime greater than 0---
            boolean burning = be.burnTime > 0;
            //---if burning true---
            if (burning) {
                be.burnTime--;
                changed = true;
                //---if not lit true, then set now---
                if (!state.getValue(BloomeryControllerBlock.LIT)) {
                    level.setBlock(pos, state.setValue(BloomeryControllerBlock.LIT, true), Block.UPDATE_ALL);
                }
            }
            else {
                //---if not burning but lit still true, set lit to false---
                if (state.getValue(BloomeryControllerBlock.LIT)) {
                    level.setBlock(pos, state.setValue(BloomeryControllerBlock.LIT, false), Block.UPDATE_ALL);
                }
            }

            //---work if valid recipe and active burnTime---
            if (canWork && burning) {
                //---progressTotal = processingTime form JSON recipe---
                be.progressTotal = recipe.get().getProcessingTime();
                //---increment current progress---
                be.progress++;
                changed = true;
                //---when current progress = total progress do craft---
                if (be.progress >= be.progressTotal) {
                    be.craft(recipe.get());
                    be.progress = 0;
                }
            } else if (be.progress > 0) {
                //---progress drops if no recipe or burnTime is there---
                be.progress = Math.max(0, be.progress - 2);
                changed = true;
            }
        }

        if (changed) be.setChanged();
    }

    public void updateFormedState() {
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        boolean valid = BloomeryStructure.isValid(level, worldPosition, state.getValue(BloomeryControllerBlock.FACING));
        if (valid != state.getValue(BloomeryControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(BloomeryControllerBlock.FORMED, valid), Block.UPDATE_ALL);
            if (valid) {
                spawnFormedParticle(level, worldPosition, state);
            }
        }
    }

    //---fuel helper - use in recipe handler---
    boolean tryConsumeFuel() {
        //---get item in fuel stack---
        ItemStack fuel = items.getStackInSlot(SLOT_FUEL);

        //---get burnTime of item---
        int time = ForgeHooks.getBurnTime(fuel, null);
        if (time <= 0) return false;

        //---only accept tag: bloomery_fuel---
        if (!fuel.is(ModItemTags.BLOOMERY_FUEL)) {return false;}

        burnTime = burnTimeTotal = time;

        //---read the remainder BEFORE changing the stack; it can depend on the stack---
        ItemStack remainder = fuel.getCraftingRemainingItem(); // EMPTY if the item has none

        if (fuel.getCount() == 1) {
            //---last item: put the remainder (e.g. empty bucket) in its place, or clear the slot (set empty stack)---
            items.setStackInSlot(SLOT_FUEL, remainder);
        } else {
            //---shrink normal fuel stacks---
            fuel.shrink(1);
            if (!remainder.isEmpty() && level != null) {
                //---stackable item with a remainder: can't go in the slot, so dispensed above the block---
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                        worldPosition.getZ() + 0.5, remainder);
            }
        }
        return true;
    }


    private Optional<BloomeryRecipe> findRecipe() {
        SimpleContainer container = new SimpleContainer(items.getStackInSlot(SLOT_INPUT));
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.BLOOMERY.get(), container, level);
    }

    private boolean canOutput(BloomeryRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return items.insertItem(SLOT_OUTPUT, result, true).isEmpty();
    }

    private void craft(BloomeryRecipe recipe) {
        items.extractItem(SLOT_INPUT, 1, false);
        items.insertItem(SLOT_OUTPUT, recipe.getResultItem(level.registryAccess()).copy(), false);
    }

    public void dropContents() {
        SimpleContainer c = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) c.setItem(i, items.getStackInSlot(i));
        Containers.dropContents(level, worldPosition, c);
    }

    //---for menu - is translatable key---
    @Override
    public Component getDisplayName() {
        return Component.translatable("container.kilnexpansion.bloomery");
    }

    //---attach menu---
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new BloomeryMenu(id, inventory, items, data, ContainerLevelAccess.create(level, worldPosition));
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