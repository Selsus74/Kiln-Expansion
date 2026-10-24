package de.selsus74.kilnexpansion.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

//---this basically does nothing except for providing a way to create records for charcoal pile "recipes"---
//---recipes are not really recipes but intrinsic block functionality---
//---meaning they are hardcoded and need a way to create jei records---
//---this does exactly that and nothing more---
public record CharcoalPileJeiRecipe(List<ItemStack> inputs, ItemStack output) {}