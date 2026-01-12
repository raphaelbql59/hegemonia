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
 * HEGEMONIA CUSTOM INVENTORY v3.0
 *
 * Clean implementation with proper positioning:
 * - Sidebars positioned outside inventory bounds
 * - Info bar below inventory
 * - No overlapping elements
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider {

    @Unique private static final Category[] LEFT_CATEGORIES = {
        Category.ECONOMY, Category.BANK, Category.NATION, Category.WAR
    };

    @Unique private static final Category[] RIGHT_CATEGORIES = {
        Category.MARKET, Category.DIPLOMACY, Category.TERRITORY, Category.LEADERBOARD
    };

    @Unique private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    @Unique private static final int BTN_SIZE = 36;      // Button size
    @Unique private static final int BTN_GAP = 6;        // Gap between buttons
    @Unique private static final int SIDEBAR_PAD = 8;    // Padding inside sidebar
    @Unique private static final int SIDEBAR_MARGIN = 8; // Margin from inventory

    @Unique private final float[] hoverAnims = new float[8];
    @Unique private int hoveredCategory = -1;
    @Unique private float openAnim = 0f;

    // Cached positions (calculated once per frame)
    @Unique private int leftSidebarX, rightSidebarX, sidebarY, sidebarHeight;
    @Unique private int infoBarY;

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void hegemonia$init(CallbackInfo ci) {
        openAnim = 0f;
        for (int i = 0; i < 8; i++) hoverAnims[i] = 0f;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hegemonia$render(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia) return;

        // Calculate positions
        hegemonia$calculatePositions();

        // Update animations
        hegemonia$updateAnimations(mouseX, mouseY);

        float ease = HegemoniaDesign.easeOut(openAnim);
        if (ease < 0.01f) return;

        // Render with proper z-ordering
        hegemonia$renderSidebars(ctx, mouseX, mouseY, ease);
        hegemonia$renderInfoBar(ctx, ease);
        hegemonia$renderTooltip(ctx, mouseX, mouseY);
    }

    @Unique
    private void hegemonia$calculatePositions() {
        int invX = this.x;
        int invY = this.y;
        int invWidth = this.backgroundWidth;
        int invHeight = this.backgroundHeight;

        int sidebarWidth = BTN_SIZE + SIDEBAR_PAD * 2;
        int totalBtnHeight = 4 * BTN_SIZE + 3 * BTN_GAP;

        // Left sidebar: to the left of inventory
        leftSidebarX = invX - sidebarWidth - SIDEBAR_MARGIN;

        // Right sidebar: to the right of inventory
        rightSidebarX = invX + invWidth + SIDEBAR_MARGIN;

        // Vertical centering
        sidebarY = invY + (invHeight - totalBtnHeight - SIDEBAR_PAD * 2) / 2;
        sidebarHeight = totalBtnHeight + SIDEBAR_PAD * 2;

        // Info bar: below inventory
        infoBarY = invY + invHeight + 6;
    }

    @Unique
    private void hegemonia$updateAnimations(int mouseX, int mouseY) {
        openAnim = Math.min(1f, openAnim + HegemoniaDesign.ANIM_NORMAL);
        hoveredCategory = hegemonia$getHoveredCategory(mouseX, mouseY);

        for (int i = 0; i < 8; i++) {
            float target = (i == hoveredCategory) ? 1f : 0f;
            hoverAnims[i] += (target - hoverAnims[i]) * HegemoniaDesign.ANIM_FAST;
        }
    }

    @Unique
    private int hegemonia$getHoveredCategory(int mouseX, int mouseY) {
        int sidebarWidth = BTN_SIZE + SIDEBAR_PAD * 2;

        // Check left sidebar buttons
        for (int i = 0; i < 4; i++) {
            int btnX = leftSidebarX + SIDEBAR_PAD;
            int btnY = sidebarY + SIDEBAR_PAD + i * (BTN_SIZE + BTN_GAP);
            if (mouseX >= btnX && mouseX < btnX + BTN_SIZE &&
                mouseY >= btnY && mouseY < btnY + BTN_SIZE) {
                return i;
            }
        }

        // Check right sidebar buttons
        for (int i = 0; i < 4; i++) {
            int btnX = rightSidebarX + SIDEBAR_PAD;
            int btnY = sidebarY + SIDEBAR_PAD + i * (BTN_SIZE + BTN_GAP);
            if (mouseX >= btnX && mouseX < btnX + BTN_SIZE &&
                mouseY >= btnY && mouseY < btnY + BTN_SIZE) {
                return 4 + i;
            }
        }

        return -1;
    }

    @Unique
    private void hegemonia$renderSidebars(DrawContext ctx, int mouseX, int mouseY, float ease) {
        int alpha = (int)(255 * ease);
        int sidebarWidth = BTN_SIZE + SIDEBAR_PAD * 2;
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();

        // === LEFT SIDEBAR ===
        int leftX = leftSidebarX + (int)((1f - ease) * -40); // Slide in from left

        // Background
        ctx.fill(leftX, sidebarY, leftX + sidebarWidth, sidebarY + sidebarHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        // Gold accent on right edge
        ctx.fill(leftX + sidebarWidth - 2, sidebarY, leftX + sidebarWidth, sidebarY + sidebarHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        HegemoniaDesign.drawBorder(ctx, leftX, sidebarY, sidebarWidth, sidebarHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        // Left buttons
        for (int i = 0; i < 4; i++) {
            Category cat = LEFT_CATEGORIES[i];
            int btnX = leftX + SIDEBAR_PAD;
            int btnY = sidebarY + SIDEBAR_PAD + i * (BTN_SIZE + BTN_GAP);
            boolean hovered = (hoveredCategory == i);
            boolean enabled = hegemonia$isCategoryEnabled(cat, data);
            hegemonia$drawButton(ctx, btnX, btnY, cat, hovered, hoverAnims[i], enabled);
        }

        // === RIGHT SIDEBAR ===
        int rightX = rightSidebarX + (int)((1f - ease) * 40); // Slide in from right

        // Background
        ctx.fill(rightX, sidebarY, rightX + sidebarWidth, sidebarY + sidebarHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        // Gold accent on left edge
        ctx.fill(rightX, sidebarY, rightX + 2, sidebarY + sidebarHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        HegemoniaDesign.drawBorder(ctx, rightX, sidebarY, sidebarWidth, sidebarHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        // Right buttons
        for (int i = 0; i < 4; i++) {
            Category cat = RIGHT_CATEGORIES[i];
            int btnX = rightX + SIDEBAR_PAD;
            int btnY = sidebarY + SIDEBAR_PAD + i * (BTN_SIZE + BTN_GAP);
            boolean hovered = (hoveredCategory == 4 + i);
            boolean enabled = hegemonia$isCategoryEnabled(cat, data);
            hegemonia$drawButton(ctx, btnX, btnY, cat, hovered, hoverAnims[4 + i], enabled);
        }
    }

    @Unique
    private void hegemonia$drawButton(DrawContext ctx, int x, int y, Category cat,
                                       boolean hovered, float hoverAnim, boolean enabled) {
        // Background
        int bgColor = !enabled ? HegemoniaDesign.BG_PRIMARY :
                      hovered ? HegemoniaDesign.lerp(HegemoniaDesign.BG_TERTIARY, HegemoniaDesign.BG_HOVER, hoverAnim) :
                      HegemoniaDesign.BG_TERTIARY;
        ctx.fill(x, y, x + BTN_SIZE, y + BTN_SIZE, bgColor);

        // Border
        int borderColor = !enabled ? HegemoniaDesign.BORDER_SUBTLE :
                          hovered ? HegemoniaDesign.lerp(HegemoniaDesign.BORDER_DEFAULT, cat.color, hoverAnim * 0.7f) :
                          HegemoniaDesign.BORDER_DEFAULT;
        HegemoniaDesign.drawBorder(ctx, x, y, BTN_SIZE, BTN_SIZE, borderColor);

        // Accent bar on hover
        if (hovered && enabled) {
            int accentAlpha = (int)(255 * hoverAnim);
            ctx.fill(x, y + 4, x + 2, y + BTN_SIZE - 4, HegemoniaDesign.withAlpha(cat.color, accentAlpha));
        }

        // Glow on hover
        if (hoverAnim > 0.1f && enabled) {
            HegemoniaDesign.drawGlow(ctx, x, y, BTN_SIZE, BTN_SIZE, cat.color, 2);
        }

        // Icon (centered, 20x20 inside 36x36 button)
        int iconColor = !enabled ? HegemoniaDesign.TEXT_DISABLED :
                        hovered ? HegemoniaDesign.lerp(HegemoniaDesign.TEXT_SECONDARY, cat.color, hoverAnim) :
                        HegemoniaDesign.TEXT_SECONDARY;
        int iconX = x + (BTN_SIZE - 20) / 2;
        int iconY = y + (BTN_SIZE - 20) / 2;
        hegemonia$drawIcon(ctx, iconX, iconY, cat, iconColor);
    }

    @Unique
    private void hegemonia$drawIcon(DrawContext ctx, int x, int y, Category cat, int color) {
        // Draw 20x20 icons (scaled up from 16x16)
        HegemoniaDesign.drawCategoryIcon(ctx, x + 2, y + 2, cat, color);
    }

    @Unique
    private void hegemonia$renderInfoBar(DrawContext ctx, float ease) {
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();
        int invX = this.x;
        int invWidth = this.backgroundWidth;
        int barHeight = 26;

        // Animate from bottom
        int barY = infoBarY + (int)((1f - ease) * 20);
        int alpha = (int)(255 * ease);

        // Background
        ctx.fill(invX, barY, invX + invWidth, barY + barHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        ctx.fill(invX, barY, invX + invWidth, barY + 2,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        HegemoniaDesign.drawBorder(ctx, invX, barY, invWidth, barHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        // Content - compact layout
        int cy = barY + (barHeight - 8) / 2;
        int cx = invX + 8;

        // Balance: $X H
        ctx.drawText(textRenderer, "$", cx, cy, HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), false);
        cx += 8;
        String bal = MONEY_FORMAT.format(data.balance) + " H";
        ctx.drawText(textRenderer, bal, cx, cy, HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_PRIMARY, alpha), false);
        cx += textRenderer.getWidth(bal) + 6;

        // Separator
        ctx.fill(cx, barY + 5, cx + 1, barY + barHeight - 5,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
        cx += 7;

        // Bank: B X H
        ctx.drawText(textRenderer, "B", cx, cy, HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
        cx += 8;
        String bank = MONEY_FORMAT.format(data.bankBalance) + " H";
        ctx.drawText(textRenderer, bank, cx, cy, HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_PRIMARY, alpha), false);
        cx += textRenderer.getWidth(bank) + 6;

        // Nation info (if space available)
        if (data.hasNation() && cx < invX + invWidth - 80) {
            ctx.fill(cx, barY + 5, cx + 1, barY + barHeight - 5,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
            cx += 7;

            String tag = "[" + data.nationTag + "]";
            ctx.drawText(textRenderer, tag, cx, cy, HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
            cx += textRenderer.getWidth(tag) + 4;

            // War status
            if (data.atWar && cx < invX + invWidth - 50) {
                ctx.drawText(textRenderer, "GUERRE", cx, cy,
                        HegemoniaDesign.withAlpha(HegemoniaDesign.ERROR, alpha), false);
            }
        }
    }

    @Unique
    private void hegemonia$renderTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (hoveredCategory < 0) return;

        Category cat = hoveredCategory < 4 ? LEFT_CATEGORIES[hoveredCategory] : RIGHT_CATEGORIES[hoveredCategory - 4];

        // Calculate tooltip size
        int titleWidth = textRenderer.getWidth(cat.name);
        int descWidth = textRenderer.getWidth(cat.description);
        int tooltipWidth = Math.max(titleWidth, descWidth) + 16;
        int tooltipHeight = 32;

        // Position tooltip
        int tooltipX, tooltipY;
        if (hoveredCategory < 4) {
            // Left side - show tooltip to the right of cursor
            tooltipX = mouseX + 12;
        } else {
            // Right side - show tooltip to the left of cursor
            tooltipX = mouseX - tooltipWidth - 12;
        }
        tooltipY = mouseY - 8;

        // Keep on screen
        if (tooltipX < 4) tooltipX = 4;
        if (tooltipX + tooltipWidth > width - 4) tooltipX = width - tooltipWidth - 4;
        if (tooltipY < 4) tooltipY = 4;
        if (tooltipY + tooltipHeight > height - 4) tooltipY = height - tooltipHeight - 4;

        // Background
        ctx.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, HegemoniaDesign.BG_PRIMARY);
        ctx.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + 2, cat.color);
        HegemoniaDesign.drawBorder(ctx, tooltipX, tooltipY, tooltipWidth, tooltipHeight, HegemoniaDesign.BORDER_DEFAULT);

        // Text
        ctx.drawText(textRenderer, cat.name, tooltipX + 8, tooltipY + 6, cat.color, false);
        ctx.drawText(textRenderer, cat.description, tooltipX + 8, tooltipY + 18, HegemoniaDesign.TEXT_MUTED, false);
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

        Category cat = clicked < 4 ? LEFT_CATEGORIES[clicked] : RIGHT_CATEGORIES[clicked - 4];

        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();
        if (!hegemonia$isCategoryEnabled(cat, data)) return;

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
                if (client != null) client.setScreen(new com.hegemonia.client.gui.screen.DiplomacyScreen());
            }
            case TERRITORY -> {
                if (client != null) client.setScreen(new com.hegemonia.client.gui.screen.TerritoryScreen());
            }
            case LEADERBOARD -> {
                if (client != null) client.setScreen(new com.hegemonia.client.gui.screen.LeaderboardScreen());
            }
        }
    }
}
