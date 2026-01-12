package com.hegemonia.client.mixin;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaTheme;
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
 * Mixin to add Hegemonia button to inventory screen
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider {

    @Unique
    private ButtonWidget hegemoniaButton;

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void hegemonia$addButton(CallbackInfo ci) {
        // Position to the left of the inventory
        int buttonX = this.x - 26;
        int buttonY = this.y + 8;

        hegemoniaButton = ButtonWidget.builder(Text.of("H"), button -> {
            HegemoniaClient.getInstance().getScreenManager().openMainMenu();
        }).dimensions(buttonX, buttonY, 20, 20).build();

        this.addDrawableChild(hegemoniaButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hegemonia$renderButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Custom render for the button to match Hegemonia style
        if (hegemoniaButton != null) {
            int x = hegemoniaButton.getX();
            int y = hegemoniaButton.getY();
            int w = hegemoniaButton.getWidth();
            int h = hegemoniaButton.getHeight();

            boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;

            // Background
            int bgColor = hovered ? HegemoniaTheme.BG_HOVER : HegemoniaTheme.BG_SECONDARY;
            context.fill(x, y, x + w, y + h, bgColor);

            // Border
            int borderColor = hovered ? HegemoniaTheme.ACCENT_GOLD : HegemoniaTheme.BORDER_DEFAULT;
            context.fill(x, y, x + w, y + 1, borderColor);
            context.fill(x, y + h - 1, x + w, y + h, borderColor);
            context.fill(x, y, x + 1, y + h, borderColor);
            context.fill(x + w - 1, y, x + w, y + h, borderColor);

            // Gold "H" letter
            int textColor = hovered ? HegemoniaTheme.ACCENT_GOLD_LIGHT : HegemoniaTheme.ACCENT_GOLD;
            context.drawCenteredTextWithShadow(textRenderer, "H", x + w / 2, y + (h - 8) / 2, textColor);
        }
    }
}
