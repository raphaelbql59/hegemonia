package com.hegemonia.client.mixin;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.theme.HegemoniaDesign.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;

/**
 * HEGEMONIA CUSTOM INVENTORY v2.0
 *
 * Adds category buttons on both sides of the inventory:
 * LEFT:  Economy, Bank, Nation, War
 * RIGHT: Market, Diplomacy, Territory, Leaderboard
 *
 * Bottom info bar shows: Balance, Bank, Nation, War status
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider {

    // Categories - LEFT side
    @Unique
    private static final Category[] LEFT_CATEGORIES = {
        Category.ECONOMY,
        Category.BANK,
        Category.NATION,
        Category.WAR
    };

    // Categories - RIGHT side
    @Unique
    private static final Category[] RIGHT_CATEGORIES = {
        Category.MARKET,
        Category.DIPLOMACY,
        Category.TERRITORY,
        Category.LEADERBOARD
    };

    @Unique
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    // Animation states for each button (8 total)
    @Unique
    private final float[] hoverAnims = new float[8];

    @Unique
    private int hoveredCategory = -1;

    @Unique
    private float openAnim = 0f;

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void hegemonia$init(CallbackInfo ci) {
        openAnim = 0f;
        for (int i = 0; i < 8; i++) {
            hoverAnims[i] = 0f;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hegemonia$render(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Don't render if not connected to Hegemonia
        if (!HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia) {
            return;
        }

        // Update animations
        hegemonia$updateAnimations(mouseX, mouseY);

        float ease = HegemoniaDesign.easeOut(openAnim);
        if (ease < 0.01f) return;

        // Get inventory panel position
        int invX = this.x;
        int invY = this.y;
        int invWidth = this.backgroundWidth;
        int invHeight = this.backgroundHeight;

        // Render components
        hegemonia$renderLeftCategories(ctx, invX, invY, invHeight, mouseX, mouseY, ease);
        hegemonia$renderRightCategories(ctx, invX + invWidth, invY, invHeight, mouseX, mouseY, ease);
        hegemonia$renderInfoBar(ctx, invX, invY + invHeight, invWidth, ease);
        hegemonia$renderTooltip(ctx, mouseX, mouseY);
    }

    @Unique
    private void hegemonia$updateAnimations(int mouseX, int mouseY) {
        // Open animation
        openAnim = Math.min(1f, openAnim + HegemoniaDesign.ANIM_NORMAL);

        // Find hovered category
        hoveredCategory = hegemonia$getHoveredCategory(mouseX, mouseY);

        // Update hover animations
        for (int i = 0; i < 8; i++) {
            float target = (i == hoveredCategory) ? 1f : 0f;
            hoverAnims[i] += (target - hoverAnims[i]) * HegemoniaDesign.ANIM_FAST;
        }
    }

    @Unique
    private int hegemonia$getHoveredCategory(int mouseX, int mouseY) {
        int invX = this.x;
        int invY = this.y;
        int invWidth = this.backgroundWidth;
        int invHeight = this.backgroundHeight;

        int btnSize = HegemoniaDesign.CATEGORY_BTN_SIZE;
        int gap = HegemoniaDesign.CATEGORY_BTN_GAP;
        int sidebarWidth = btnSize + 12;

        // LEFT categories
        int leftX = invX - sidebarWidth - 4;
        int totalHeight = 4 * btnSize + 3 * gap;
        int startY = invY + (invHeight - totalHeight) / 2;

        for (int i = 0; i < 4; i++) {
            int btnX = leftX + 6;
            int btnY = startY + i * (btnSize + gap);
            if (mouseX >= btnX && mouseX < btnX + btnSize &&
                mouseY >= btnY && mouseY < btnY + btnSize) {
                return i;
            }
        }

        // RIGHT categories
        int rightX = invX + invWidth + 4;

        for (int i = 0; i < 4; i++) {
            int btnX = rightX + 6;
            int btnY = startY + i * (btnSize + gap);
            if (mouseX >= btnX && mouseX < btnX + btnSize &&
                mouseY >= btnY && mouseY < btnY + btnSize) {
                return 4 + i;
            }
        }

        return -1;
    }

    @Unique
    private void hegemonia$renderLeftCategories(DrawContext ctx, int invX, int invY, int invHeight,
                                                 int mouseX, int mouseY, float ease) {
        int btnSize = HegemoniaDesign.CATEGORY_BTN_SIZE;
        int gap = HegemoniaDesign.CATEGORY_BTN_GAP;
        int sidebarWidth = btnSize + 12;

        // Position sidebar to the left of inventory
        int sidebarX = invX - sidebarWidth - 4;
        int totalHeight = 4 * btnSize + 3 * gap;
        int startY = invY + (invHeight - totalHeight) / 2;

        // Animate in from left
        int animOffset = (int)((1f - ease) * -50);
        sidebarX += animOffset;

        // Background panel
        int alpha = (int)(255 * ease);
        ctx.fill(sidebarX, startY - 8, sidebarX + sidebarWidth, startY + totalHeight + 8,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        ctx.fill(sidebarX + sidebarWidth - 2, startY - 8, sidebarX + sidebarWidth, startY + totalHeight + 8,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        HegemoniaDesign.drawBorder(ctx, sidebarX, startY - 8, sidebarWidth, totalHeight + 16,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        // Draw buttons
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();

        for (int i = 0; i < LEFT_CATEGORIES.length; i++) {
            Category cat = LEFT_CATEGORIES[i];
            int btnX = sidebarX + 6;
            int btnY = startY + i * (btnSize + gap);

            boolean hovered = (hoveredCategory == i);
            boolean enabled = hegemonia$isCategoryEnabled(cat, data);

            HegemoniaDesign.drawCategoryButton(ctx, btnX, btnY, cat, hovered, hoverAnims[i], enabled);
        }
    }

    @Unique
    private void hegemonia$renderRightCategories(DrawContext ctx, int invRight, int invY, int invHeight,
                                                  int mouseX, int mouseY, float ease) {
        int btnSize = HegemoniaDesign.CATEGORY_BTN_SIZE;
        int gap = HegemoniaDesign.CATEGORY_BTN_GAP;
        int sidebarWidth = btnSize + 12;

        // Position sidebar to the right of inventory
        int sidebarX = invRight + 4;
        int totalHeight = 4 * btnSize + 3 * gap;
        int startY = invY + (invHeight - totalHeight) / 2;

        // Animate in from right
        int animOffset = (int)((1f - ease) * 50);
        sidebarX += animOffset;

        // Background panel
        int alpha = (int)(255 * ease);
        ctx.fill(sidebarX, startY - 8, sidebarX + sidebarWidth, startY + totalHeight + 8,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        ctx.fill(sidebarX, startY - 8, sidebarX + 2, startY + totalHeight + 8,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        HegemoniaDesign.drawBorder(ctx, sidebarX, startY - 8, sidebarWidth, totalHeight + 16,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        // Draw buttons
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();

        for (int i = 0; i < RIGHT_CATEGORIES.length; i++) {
            Category cat = RIGHT_CATEGORIES[i];
            int btnX = sidebarX + 6;
            int btnY = startY + i * (btnSize + gap);

            boolean hovered = (hoveredCategory == 4 + i);
            boolean enabled = hegemonia$isCategoryEnabled(cat, data);

            HegemoniaDesign.drawCategoryButton(ctx, btnX, btnY, cat, hovered, hoverAnims[4 + i], enabled);
        }
    }

    @Unique
    private void hegemonia$renderInfoBar(DrawContext ctx, int invX, int invBottom, int invWidth, float ease) {
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();

        int barHeight = HegemoniaDesign.INFO_BAR_HEIGHT;
        int barY = invBottom + 4;

        // Animate in from bottom
        int animOffset = (int)((1f - ease) * 30);
        barY += animOffset;

        int alpha = (int)(255 * ease);

        // Background
        ctx.fill(invX, barY, invX + invWidth, barY + barHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        ctx.fill(invX, barY, invX + invWidth, barY + 2,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        HegemoniaDesign.drawBorder(ctx, invX, barY, invWidth, barHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        // Content
        int contentY = barY + 7;
        int x = invX + 8;
        int spacing = 8;

        // Balance
        String balanceLabel = "Solde:";
        String balanceValue = MONEY_FORMAT.format(data.balance) + " H";
        ctx.drawText(textRenderer, balanceLabel, x, contentY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);
        x += textRenderer.getWidth(balanceLabel) + 4;
        ctx.drawText(textRenderer, balanceValue, x, contentY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), false);
        x += textRenderer.getWidth(balanceValue) + spacing;

        // Separator
        ctx.fill(x, barY + 6, x + 1, barY + barHeight - 6,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
        x += spacing;

        // Bank
        String bankLabel = "Banque:";
        String bankValue = MONEY_FORMAT.format(data.bankBalance) + " H";
        ctx.drawText(textRenderer, bankLabel, x, contentY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);
        x += textRenderer.getWidth(bankLabel) + 4;
        ctx.drawText(textRenderer, bankValue, x, contentY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
        x += textRenderer.getWidth(bankValue) + spacing;

        // Separator
        ctx.fill(x, barY + 6, x + 1, barY + barHeight - 6,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
        x += spacing;

        // Nation
        if (data.hasNation()) {
            String nationText = "[" + data.nationTag + "] " + data.nationName;
            ctx.drawText(textRenderer, nationText, x, contentY,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
            x += textRenderer.getWidth(nationText) + spacing;

            // War status
            if (data.atWar) {
                ctx.fill(x, barY + 6, x + 1, barY + barHeight - 6,
                        HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
                x += spacing;
                String warText = "EN GUERRE";
                ctx.drawText(textRenderer, warText, x, contentY,
                        HegemoniaDesign.withAlpha(HegemoniaDesign.ERROR, alpha), false);
            }
        } else {
            String noNation = "Pas de nation";
            ctx.drawText(textRenderer, noNation, x, contentY,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);
        }
    }

    @Unique
    private void hegemonia$renderTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (hoveredCategory < 0) return;

        Category cat;
        if (hoveredCategory < 4) {
            cat = LEFT_CATEGORIES[hoveredCategory];
        } else {
            cat = RIGHT_CATEGORIES[hoveredCategory - 4];
        }

        // Position tooltip near the button
        int tooltipX, tooltipY;
        int invX = this.x;
        int invWidth = this.backgroundWidth;

        if (hoveredCategory < 4) {
            // Left side - tooltip on right of button
            tooltipX = invX - 4;
            tooltipY = mouseY - 10;
        } else {
            // Right side - tooltip on left of button
            int tooltipWidth = Math.max(textRenderer.getWidth(cat.name), textRenderer.getWidth(cat.description)) + 24;
            tooltipX = invX + invWidth + 4 - tooltipWidth;
            tooltipY = mouseY - 10;
        }

        HegemoniaDesign.drawTooltip(ctx, tooltipX, tooltipY, cat.name, cat.description);
    }

    @Unique
    private boolean hegemonia$isCategoryEnabled(Category cat, HegemoniaClient.PlayerData data) {
        return switch (cat) {
            case WAR, DIPLOMACY -> data.hasNation();
            default -> true;
        };
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hegemonia$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) return;
        if (!HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia) return;

        int clicked = hegemonia$getHoveredCategory((int) mouseX, (int) mouseY);
        if (clicked < 0) return;

        Category cat;
        if (clicked < 4) {
            cat = LEFT_CATEGORIES[clicked];
        } else {
            cat = RIGHT_CATEGORIES[clicked - 4];
        }

        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();
        if (!hegemonia$isCategoryEnabled(cat, data)) return;

        // Open the corresponding screen
        hegemonia$openCategory(cat);
        cir.setReturnValue(true);
    }

    @Unique
    private void hegemonia$openCategory(Category cat) {
        var screenManager = HegemoniaClient.getInstance().getScreenManager();

        switch (cat) {
            case ECONOMY -> screenManager.openEconomyMenu();
            case BANK -> screenManager.openBankMenu();
            case NATION -> screenManager.openNationMenu();
            case WAR -> screenManager.openWarMenu();
            case MARKET -> screenManager.openMarketMenu();
            case DIPLOMACY -> {
                if (client != null) {
                    client.setScreen(new com.hegemonia.client.gui.screen.DiplomacyScreen());
                }
            }
            case TERRITORY -> {
                if (client != null) {
                    client.setScreen(new com.hegemonia.client.gui.screen.TerritoryScreen());
                }
            }
            case LEADERBOARD -> {
                // TODO: Leaderboard screen
            }
        }
    }
}
