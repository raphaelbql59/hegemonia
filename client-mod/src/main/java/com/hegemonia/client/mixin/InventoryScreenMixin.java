package com.hegemonia.client.mixin;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * HEGEMONIA INVENTORY BUTTON
 * Adds a professional Hegemonia access button to the inventory screen
 * Features the logo with hover effects and gold accent styling
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider {

    @Unique
    private ButtonWidget hegemoniaButton;

    @Unique
    private float hoverProgress = 0f;

    @Unique
    private static final int BUTTON_SIZE = 24;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void hegemonia$addButton(CallbackInfo ci) {
        // Position to the left of the inventory panel
        int buttonX = this.x - BUTTON_SIZE - 4;
        int buttonY = this.y + 4;

        hegemoniaButton = ButtonWidget.builder(Text.empty(), button -> {
            HegemoniaClient.getInstance().getScreenManager().openMainMenu();
        }).dimensions(buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE).build();

        this.addDrawableChild(hegemoniaButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hegemonia$renderButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (hegemoniaButton == null) return;

        int x = hegemoniaButton.getX();
        int y = hegemoniaButton.getY();
        int size = BUTTON_SIZE;

        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        // Smooth hover animation
        if (hovered) {
            hoverProgress = Math.min(1f, hoverProgress + 0.15f);
        } else {
            hoverProgress = Math.max(0f, hoverProgress - 0.1f);
        }

        float ease = HegemoniaDesign.easeOut(hoverProgress);

        // ═══════════════════════════════════════════════════════════════
        // BUTTON BACKGROUND
        // ═══════════════════════════════════════════════════════════════
        int bgColor = HegemoniaDesign.lerp(HegemoniaDesign.BG_PANEL, HegemoniaDesign.BG_CARD_HOVER, ease);
        context.fill(x, y, x + size, y + size, bgColor);

        // ═══════════════════════════════════════════════════════════════
        // BORDER WITH GOLD ACCENT ON HOVER
        // ═══════════════════════════════════════════════════════════════
        int borderColor = HegemoniaDesign.lerp(HegemoniaDesign.BORDER_DEFAULT, HegemoniaDesign.GOLD, ease);

        // Top border (thicker when hovered)
        int topThickness = hovered ? 2 : 1;
        context.fill(x, y, x + size, y + topThickness, borderColor);
        // Other borders
        context.fill(x, y + size - 1, x + size, y + size, HegemoniaDesign.BORDER_DEFAULT);
        context.fill(x, y, x + 1, y + size, HegemoniaDesign.BORDER_DEFAULT);
        context.fill(x + size - 1, y, x + size, y + size, HegemoniaDesign.BORDER_DEFAULT);

        // ═══════════════════════════════════════════════════════════════
        // GLOW EFFECT ON HOVER
        // ═══════════════════════════════════════════════════════════════
        if (hoverProgress > 0.1f) {
            int glowAlpha = (int)(20 * ease);
            int glowColor = HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, glowAlpha);
            context.fill(x - 2, y - 2, x + size + 2, y, glowColor);
            context.fill(x - 2, y + size, x + size + 2, y + size + 2, glowColor);
            context.fill(x - 2, y, x, y + size, glowColor);
            context.fill(x + size, y, x + size + 2, y + size, glowColor);
        }

        // ═══════════════════════════════════════════════════════════════
        // LOGO (centered in button)
        // ═══════════════════════════════════════════════════════════════
        int logoSize = 16;
        int logoX = x + (size - logoSize) / 2;
        int logoY = y + (size - logoSize) / 2;

        // Draw logo texture
        context.drawTexture(HegemoniaDesign.LOGO, logoX, logoY, 0, 0, logoSize, logoSize, logoSize, logoSize);

        // ═══════════════════════════════════════════════════════════════
        // TOOLTIP ON HOVER
        // ═══════════════════════════════════════════════════════════════
        if (hovered) {
            // Draw tooltip background
            String tooltipText = "Hegemonia";
            int textWidth = textRenderer.getWidth(tooltipText);
            int tooltipX = x - textWidth - 8;
            int tooltipY = y + (size - 9) / 2;

            // Background
            context.fill(tooltipX - 4, tooltipY - 2, tooltipX + textWidth + 4, tooltipY + 11,
                    HegemoniaDesign.BG_PANEL);
            // Border
            context.fill(tooltipX - 4, tooltipY - 2, tooltipX + textWidth + 4, tooltipY - 1,
                    HegemoniaDesign.GOLD);
            HegemoniaDesign.drawBorder(context, tooltipX - 4, tooltipY - 2, textWidth + 8, 13,
                    HegemoniaDesign.BORDER_DEFAULT);

            // Text
            context.drawText(textRenderer, tooltipText, tooltipX, tooltipY,
                    HegemoniaDesign.GOLD, false);
        }
    }
}
