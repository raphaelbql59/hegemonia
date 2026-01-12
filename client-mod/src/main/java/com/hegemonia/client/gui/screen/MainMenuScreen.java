package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

/**
 * HEGEMONIA MAIN MENU
 * Ultra-modern, Nations Glory inspired design
 * Professional card-based layout with smooth animations
 */
public class MainMenuScreen extends Screen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private final HegemoniaClient hegemonia;

    // Layout dimensions
    private int panelX, panelY, panelWidth, panelHeight;
    private int contentX, contentY;

    // Animation state
    private float openAnim = 0f;
    private float[] cardAnim = new float[6];
    private int hoveredCard = -1;

    // Card data
    private static final String[] CARD_TITLES = {"ECONOMIE", "NATION", "GUERRE", "TERRITOIRES", "MARCHE", "BANQUE"};
    private static final String[] CARD_SUBTITLES = {"Gerez votre fortune", "Votre empire", "Conflits actifs", "Carte du monde", "Achat & Vente", "Epargne & Prets"};
    private static final String[] CARD_ICONS = {"economy", "nation", "war", "territory", "market", "bank"};
    private static final int[] CARD_COLORS = {
            HegemoniaDesign.GOLD,
            HegemoniaDesign.BLUE,
            HegemoniaDesign.ERROR,
            HegemoniaDesign.SUCCESS,
            HegemoniaDesign.WARNING,
            HegemoniaDesign.INFO
    };

    public MainMenuScreen() {
        super(Text.literal("Hegemonia"));
        this.hegemonia = HegemoniaClient.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        // Calculate panel dimensions (centered, max 600x420)
        panelWidth = Math.min(600, (int)(width * 0.85));
        panelHeight = Math.min(420, (int)(height * 0.85));
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        contentX = panelX + HegemoniaDesign.SPACE_LG;
        contentY = panelY + HegemoniaDesign.HEADER_HEIGHT + HegemoniaDesign.SPACE_MD;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Update animations
        openAnim = Math.min(1f, openAnim + HegemoniaDesign.ANIM_SPEED_FAST);
        for (int i = 0; i < cardAnim.length; i++) {
            float targetDelay = i * 0.08f;
            if (openAnim > targetDelay) {
                cardAnim[i] = Math.min(1f, cardAnim[i] + HegemoniaDesign.ANIM_SPEED_FAST);
            }
        }

        float eased = HegemoniaDesign.easeOut(openAnim);

        // ═══════════════════════════════════════════════════════════════
        // BACKGROUND OVERLAY
        // ═══════════════════════════════════════════════════════════════
        int overlayAlpha = (int)(230 * eased);
        ctx.fill(0, 0, width, height, HegemoniaDesign.withAlpha(0x000000, overlayAlpha));

        // ═══════════════════════════════════════════════════════════════
        // MAIN PANEL
        // ═══════════════════════════════════════════════════════════════
        int panelAlpha = (int)(255 * eased);

        // Panel background
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PANEL, panelAlpha));

        // Panel border with gold accent
        HegemoniaDesign.drawPanelBorder(ctx, panelX, panelY, panelWidth, panelHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, panelAlpha),
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, panelAlpha));

        // ═══════════════════════════════════════════════════════════════
        // HEADER
        // ═══════════════════════════════════════════════════════════════
        renderHeader(ctx, mouseX, mouseY, eased);

        // ═══════════════════════════════════════════════════════════════
        // CARDS GRID (2x3)
        // ═══════════════════════════════════════════════════════════════
        renderCards(ctx, mouseX, mouseY, delta);

        // ═══════════════════════════════════════════════════════════════
        // FOOTER
        // ═══════════════════════════════════════════════════════════════
        renderFooter(ctx, mouseX, mouseY, eased);

        // ═══════════════════════════════════════════════════════════════
        // CLOSE BUTTON
        // ═══════════════════════════════════════════════════════════════
        renderCloseButton(ctx, mouseX, mouseY);
    }

    private void renderHeader(DrawContext ctx, int mouseX, int mouseY, float anim) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int alpha = (int)(255 * anim);

        // Header background
        ctx.fill(panelX + 1, panelY + 2, panelX + panelWidth - 1, panelY + HegemoniaDesign.HEADER_HEIGHT,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PANEL_HEADER, alpha));

        // Header bottom divider
        ctx.fill(panelX + 1, panelY + HegemoniaDesign.HEADER_HEIGHT - 1,
                panelX + panelWidth - 1, panelY + HegemoniaDesign.HEADER_HEIGHT,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_SUBTLE, alpha));

        // Logo
        int logoSize = 32;
        int logoX = panelX + HegemoniaDesign.SPACE_MD;
        int logoY = panelY + (HegemoniaDesign.HEADER_HEIGHT - logoSize) / 2 + 1;

        // Draw logo texture
        ctx.drawTexture(HegemoniaDesign.LOGO, logoX, logoY, 0, 0, logoSize, logoSize, logoSize, logoSize);

        // Title
        int titleX = logoX + logoSize + HegemoniaDesign.SPACE_SM;
        ctx.drawText(textRenderer, "HEGEMONIA",
                titleX, logoY + 4, HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), true);
        ctx.drawText(textRenderer, "Simulation Geopolitique",
                titleX, logoY + 16, HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);

        // Player info (right side)
        String playerName = client != null && client.player != null ? client.player.getName().getString() : "Joueur";
        String balance = MONEY_FORMAT.format(data.getTotalBalance()) + " H";

        int rightX = panelX + panelWidth - HegemoniaDesign.SPACE_MD;

        // Balance
        int balanceWidth = textRenderer.getWidth(balance);
        ctx.drawText(textRenderer, balance,
                rightX - balanceWidth - 24, logoY + 4,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), true);

        // Player name
        int nameWidth = textRenderer.getWidth(playerName);
        ctx.drawText(textRenderer, playerName,
                rightX - nameWidth - 24, logoY + 16,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_SECONDARY, alpha), false);

        // Nation tag
        if (data.hasNation()) {
            String tag = "[" + data.nationTag + "]";
            int tagWidth = textRenderer.getWidth(tag);
            ctx.drawText(textRenderer, tag,
                    rightX - tagWidth - 24, logoY + 28,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
        }
    }

    private void renderCards(DrawContext ctx, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Card grid layout
        int cardWidth = (panelWidth - HegemoniaDesign.SPACE_LG * 2 - HegemoniaDesign.SPACE_MD * 2) / 3;
        int cardHeight = HegemoniaDesign.CARD_HEIGHT;
        int cardSpacingX = HegemoniaDesign.SPACE_MD;
        int cardSpacingY = HegemoniaDesign.SPACE_MD;

        int startX = contentX;
        int startY = contentY;

        hoveredCard = -1;

        for (int i = 0; i < 6; i++) {
            int col = i % 3;
            int row = i / 3;

            int cardX = startX + col * (cardWidth + cardSpacingX);
            int cardY = startY + row * (cardHeight + cardSpacingY);

            float anim = HegemoniaDesign.easeOut(cardAnim[i]);
            if (anim < 0.01f) continue;

            boolean hovered = mouseX >= cardX && mouseX < cardX + cardWidth &&
                    mouseY >= cardY && mouseY < cardY + cardHeight;

            if (hovered) hoveredCard = i;

            // Check if card is enabled
            boolean enabled = true;
            if (i == 2 && !data.hasNation()) enabled = false; // War requires nation

            int alpha = (int)(255 * anim);

            // Card background
            int bgColor = hovered && enabled ? HegemoniaDesign.BG_CARD_HOVER : HegemoniaDesign.BG_CARD;
            if (!enabled) bgColor = HegemoniaDesign.withAlpha(HegemoniaDesign.BG_CARD, 128);

            ctx.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight,
                    HegemoniaDesign.withAlpha(bgColor, alpha));

            // Card border
            int borderColor = hovered && enabled ? CARD_COLORS[i] : HegemoniaDesign.BORDER_DEFAULT;
            if (!enabled) borderColor = HegemoniaDesign.TEXT_DISABLED;
            HegemoniaDesign.drawBorder(ctx, cardX, cardY, cardWidth, cardHeight,
                    HegemoniaDesign.withAlpha(borderColor, alpha));

            // Accent bar (left)
            int accentColor = enabled ? CARD_COLORS[i] : HegemoniaDesign.TEXT_DISABLED;
            ctx.fill(cardX, cardY + 8, cardX + 3, cardY + cardHeight - 8,
                    HegemoniaDesign.withAlpha(accentColor, alpha));

            // Hover glow
            if (hovered && enabled) {
                HegemoniaDesign.drawGlow(ctx, cardX, cardY, cardWidth, cardHeight, CARD_COLORS[i]);
            }

            // Icon
            int iconSize = 24;
            int iconX = cardX + HegemoniaDesign.SPACE_SM + 4;
            int iconY = cardY + HegemoniaDesign.SPACE_SM;
            int iconColor = enabled ? CARD_COLORS[i] : HegemoniaDesign.TEXT_DISABLED;
            HegemoniaDesign.drawIcon(ctx, CARD_ICONS[i], iconX, iconY, iconSize, HegemoniaDesign.withAlpha(iconColor, alpha));

            // Title
            int textX = iconX + iconSize + HegemoniaDesign.SPACE_SM;
            int textColor = enabled ? HegemoniaDesign.TEXT_PRIMARY : HegemoniaDesign.TEXT_DISABLED;
            ctx.drawText(textRenderer, CARD_TITLES[i],
                    textX, iconY + 2, HegemoniaDesign.withAlpha(textColor, alpha), false);

            // Subtitle
            ctx.drawText(textRenderer, CARD_SUBTITLES[i],
                    textX, iconY + 14, HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);

            // Value display (bottom of card)
            String value = getCardValue(i, data);
            int valueColor = enabled ? CARD_COLORS[i] : HegemoniaDesign.TEXT_DISABLED;
            ctx.drawText(textRenderer, value,
                    cardX + HegemoniaDesign.SPACE_SM + 4, cardY + cardHeight - 18,
                    HegemoniaDesign.withAlpha(valueColor, alpha), true);

            // Arrow indicator
            if (enabled) {
                int arrowX = cardX + cardWidth - 16;
                int arrowY = cardY + cardHeight / 2 - 4;
                ctx.drawText(textRenderer, ">",
                        arrowX, arrowY, HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, (int)(alpha * 0.6f)), false);
            }
        }
    }

    private String getCardValue(int cardIndex, HegemoniaClient.PlayerData data) {
        return switch (cardIndex) {
            case 0 -> MONEY_FORMAT.format(data.balance) + " H";
            case 1 -> data.hasNation() ? data.nationName : "Sans nation";
            case 2 -> data.atWar ? "EN GUERRE" : "En paix";
            case 3 -> "Explorer";
            case 4 -> "Voir offres";
            case 5 -> MONEY_FORMAT.format(data.bankBalance) + " H";
            default -> "";
        };
    }

    private void renderFooter(DrawContext ctx, int mouseX, int mouseY, float anim) {
        int alpha = (int)(255 * anim);
        int footerY = panelY + panelHeight - HegemoniaDesign.FOOTER_HEIGHT;

        // Footer divider
        ctx.fill(panelX + 1, footerY, panelX + panelWidth - 1, footerY + 1,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_SUBTLE, alpha));

        // Settings button
        int btnY = footerY + (HegemoniaDesign.FOOTER_HEIGHT - HegemoniaDesign.BUTTON_HEIGHT_SM) / 2;
        int settingsBtnX = panelX + HegemoniaDesign.SPACE_MD;
        int settingsBtnW = 100;

        boolean settingsHovered = mouseX >= settingsBtnX && mouseX < settingsBtnX + settingsBtnW &&
                mouseY >= btnY && mouseY < btnY + HegemoniaDesign.BUTTON_HEIGHT_SM;

        int settingsBg = settingsHovered ? HegemoniaDesign.BG_BUTTON_HOVER : HegemoniaDesign.BG_BUTTON;
        int settingsBorder = settingsHovered ? HegemoniaDesign.TEXT_SECONDARY : HegemoniaDesign.BORDER_DEFAULT;

        ctx.fill(settingsBtnX, btnY, settingsBtnX + settingsBtnW, btnY + HegemoniaDesign.BUTTON_HEIGHT_SM,
                HegemoniaDesign.withAlpha(settingsBg, alpha));
        HegemoniaDesign.drawBorder(ctx, settingsBtnX, btnY, settingsBtnW, HegemoniaDesign.BUTTON_HEIGHT_SM,
                HegemoniaDesign.withAlpha(settingsBorder, alpha));

        String settingsText = "Reglages";
        int settingsTextW = textRenderer.getWidth(settingsText);
        ctx.drawText(textRenderer, settingsText,
                settingsBtnX + (settingsBtnW - settingsTextW) / 2, btnY + 6,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_SECONDARY, alpha), false);

        // Version
        String version = "v1.0.0";
        ctx.drawText(textRenderer, version,
                panelX + panelWidth - textRenderer.getWidth(version) - HegemoniaDesign.SPACE_MD,
                btnY + 6, HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);
    }

    private void renderCloseButton(DrawContext ctx, int mouseX, int mouseY) {
        int btnSize = 24;
        int btnX = panelX + panelWidth - btnSize - 8;
        int btnY = panelY + 8;

        boolean hovered = mouseX >= btnX && mouseX < btnX + btnSize &&
                mouseY >= btnY && mouseY < btnY + btnSize;

        int bgColor = hovered ? HegemoniaDesign.withAlpha(HegemoniaDesign.ERROR, 60) : 0;
        int textColor = hovered ? HegemoniaDesign.ERROR_LIGHT : HegemoniaDesign.TEXT_MUTED;

        if (bgColor != 0) {
            ctx.fill(btnX, btnY, btnX + btnSize, btnY + btnSize, bgColor);
        }

        ctx.drawCenteredTextWithShadow(textRenderer, "X", btnX + btnSize / 2, btnY + 8, textColor);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Close button
        int closeBtnSize = 24;
        int closeBtnX = panelX + panelWidth - closeBtnSize - 8;
        int closeBtnY = panelY + 8;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + closeBtnSize &&
                mouseY >= closeBtnY && mouseY < closeBtnY + closeBtnSize) {
            close();
            return true;
        }

        // Settings button
        int btnY = panelY + panelHeight - HegemoniaDesign.FOOTER_HEIGHT +
                (HegemoniaDesign.FOOTER_HEIGHT - HegemoniaDesign.BUTTON_HEIGHT_SM) / 2;
        int settingsBtnX = panelX + HegemoniaDesign.SPACE_MD;
        int settingsBtnW = 100;
        if (mouseX >= settingsBtnX && mouseX < settingsBtnX + settingsBtnW &&
                mouseY >= btnY && mouseY < btnY + HegemoniaDesign.BUTTON_HEIGHT_SM) {
            hegemonia.getScreenManager().openSettingsMenu();
            return true;
        }

        // Card clicks
        if (hoveredCard >= 0) {
            HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

            switch (hoveredCard) {
                case 0 -> hegemonia.getScreenManager().openEconomyMenu();
                case 1 -> hegemonia.getScreenManager().openNationMenu();
                case 2 -> {
                    if (data.hasNation()) hegemonia.getScreenManager().openWarMenu();
                }
                case 3 -> {
                    if (client != null) client.setScreen(new TerritoryScreen());
                }
                case 4 -> hegemonia.getScreenManager().openMarketMenu();
                case 5 -> hegemonia.getScreenManager().openBankMenu();
            }
            return true;
        }

        // Click outside panel closes
        if (mouseX < panelX || mouseX >= panelX + panelWidth ||
                mouseY < panelY || mouseY >= panelY + panelHeight) {
            close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
