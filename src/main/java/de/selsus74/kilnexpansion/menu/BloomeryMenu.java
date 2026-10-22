package de.selsus74.kilnexpansion.menu;

import de.selsus74.kilnexpansion.kiln.bloomery.BloomeryBlockEntity;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import de.selsus74.kilnexpansion.registry.ModItemTags;
import de.selsus74.kilnexpansion.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BloomeryMenu extends AbstractContainerMenu {

    private static final int BE_SLOTS = 3;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    //---Client-Constructor (called in IForgeMenuType)---
    public BloomeryMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, new ItemStackHandler(BE_SLOTS), new SimpleContainerData(8),
                ContainerLevelAccess.create(inv.player.level(), buf.readBlockPos()));
    }

    //---Server-Constructor (from BlockEntity)---
    public BloomeryMenu(int id, Inventory inv, IItemHandler items, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.BLOOMERY_MENU.get(), id);
        this.access = access;
        this.data = data;

        addSlot(new SlotItemHandler(items, BloomeryBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(new SlotItemHandler(items, BloomeryBlockEntity.SLOT_FUEL, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                //---only placeable if tag is bloomery_fuel---
                return ForgeHooks.getBurnTime(stack,null) > 0 && stack.is(ModItemTags.BLOOMERY_FUEL);
            }
        });
        addSlot(new SlotItemHandler(items, BloomeryBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        //---player inventory---
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        //---hotbar---
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    private int getInt(int index) {
        //---& 0xFFFF, the short comes with char to Client---
        return (data.get(index * 2) & 0xFFFF) | ((data.get(index * 2 + 1) & 0xFFFF) << 16);
    }

    public boolean isBurning() {
        return getInt(0) > 0;
    }

    //---Flame height, 0..13---
    public int getBurnProgress() {
        int total = getInt(1);
        if (total == 0) total = 200;
        return (int) ((long) getInt(0) * 13 / total);
    }

    //---Arrow width, 0..24---
    public int getScaledProgress() {
        int progress = getInt(2);
        int total = getInt(3);
        return total != 0 && progress != 0 ? (int) ((long) progress * 24 / total) : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < BE_SLOTS) {
            //---Kiln -> Player inventory---
            if (!moveItemStackTo(stack, BE_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            //---Player -> Kiln (Fuel in Fuel, otherwise in Input)---
            boolean moved;
            if (ForgeHooks.getBurnTime(stack, null) > 0) {
                moved = moveItemStackTo(stack, BloomeryBlockEntity.SLOT_FUEL, BloomeryBlockEntity.SLOT_FUEL + 1, false);
            } else {
                moved = moveItemStackTo(stack, BloomeryBlockEntity.SLOT_INPUT, BloomeryBlockEntity.SLOT_INPUT + 1, false);
            }
            //---otherwise move between Inventory and Hotbar---
            if (!moved) {
                if (index < BE_SLOTS + 27) {
                    if (!moveItemStackTo(stack, BE_SLOTS + 27, slots.size(), false)) return ItemStack.EMPTY;
                } else if (!moveItemStackTo(stack, BE_SLOTS, BE_SLOTS + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BLOOMERY_CONTROLLER.get());
    }
}