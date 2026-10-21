package de.selsus74.kilnexpansion.client;

import de.selsus74.kilnexpansion.menu.BloomeryMenu;
import de.selsus74.kilnexpansion.menu.DraftKilnMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BloomeryScreen extends AbstractContainerScreen<BloomeryMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/gui/container/furnace.png");

    public BloomeryScreen(BloomeryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.isBurning()) {
            int k = menu.getBurnProgress();
            g.blit(TEXTURE, leftPos + 56, topPos + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        int l = menu.getScaledProgress();
        g.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, l + 1, 16);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}