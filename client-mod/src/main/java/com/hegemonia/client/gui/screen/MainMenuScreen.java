package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Main Hegemonia menu - Modern hub for all features
 * Redesigned with card-based layout and smooth animations
 */
public class MainMenuScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat COMPACT_FORMAT = new DecimalFormat("#,##0");

    // Animation
    private float animationProgress = 0f;
    private float[] cardAnimations = new float[4];

    public MainMenuScreen() {
        super("HEGEMONIA");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(520, (int) (screenWidth * 0.85));
        contentHeight = Math.min(380, (int) (screenHeight * 0.78));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        int cardWidth = (contentWidth - 70) / 2;
        int cardHeight = 85;
        int cardSpacing = 15;
        int startY = contentY + 95;
        int leftX = contentX + 20;
        int rightX = contentX + cardWidth + 35;

        // ═══════════════════════════════════════════════════════════════
        // ECONOMY CARD (Top Left)
        // ═══════════════════════════════════════════════════════════════
        addWidget(new HegemoniaButton(
                leftX, startY,
                cardWidth, cardHeight,
                "",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> hegemonia.getScreenManager().openEconomyMenu()
        ));

        // ═══════════════════════════════════════════════════════════════
        // NATION CARD (Top Right)
        // ═══════════════════════════════════════════════════════════════
        addWidget(new HegemoniaButton(
                rightX, startY,
                cardWidth, cardHeight,
                "",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> hegemonia.getScreenManager().openNationMenu()
        ));

        // ═══════════════════════════════════════════════════════════════
        // WAR CARD (Bottom Left)
        // ═══════════════════════════════════════════════════════════════
        HegemoniaButton warBtn = addWidget(new HegemoniaButton(
                leftX, startY + cardHeight + cardSpacing,
                cardWidth, cardHeight,
                "",
                data.atWar ? HegemoniaButton.ButtonStyle.DANGER : HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> hegemonia.getScreenManager().openWarMenu()
        ));
        warBtn.setEnabled(data.hasNation());

        // ═══════════════════════════════════════════════════════════════
        // MAP/TERRITORY CARD (Bottom Right)
        // ═══════════════════════════════════════════════════════════════
        addWidget(new HegemoniaButton(
                rightX, startY + cardHeight + cardSpacing,
                cardWidth, cardHeight,
                "",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> navigateTo(new TerritoryScreen())
        ));

        // ═══════════════════════════════════════════════════════════════
        // BOTTOM ACTION BAR
        // ═══════════════════════════════════════════════════════════════
        int bottomY = contentY + contentHeight - 50;
        int smallBtnWidth = (contentWidth - 60) / 3;

        addWidget(new HegemoniaButton(
                leftX, bottomY,
                smallBtnWidth, 32,
                "Marche",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openMarketMenu()
        ));

        addWidget(new HegemoniaButton(
                leftX + smallBtnWidth + 10, bottomY,
                smallBtnWidth, 32,
                "Banque",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openBankMenu()
        ));

        addWidget(new HegemoniaButton(
                leftX + (smallBtnWidth + 10) * 2, bottomY,
                smallBtnWidth, 32,
                "Reglages",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openSettingsMenu()
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animations
        animationProgress = Math.min(1f, animationProgress + 0.08f);
        for (int i = 0; i < cardAnimations.length; i++) {
            float targetDelay = i * 0.15f;
            if (animationProgress > targetDelay) {
                cardAnimations[i] = Math.min(1f, cardAnimations[i] + 0.12f);
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Header background with gradient effect
        int headerHeight = 75;
        context.fill(contentX, contentY, contentX + contentWidth, contentY + headerHeight,
                HegemoniaColors.withAlpha(HegemoniaColors.PANEL_HEADER, 250));

        // Accent line at top
        context.fill(contentX, contentY, contentX + contentWidth, contentY + 3, HegemoniaColors.ACCENT_GOLD);

        // Logo and title
        int logoX = contentX + 20;
        int logoY = contentY + 18;

        // Draw crown icon
        drawCrownIcon(context, logoX, logoY + 2, HegemoniaColors.ACCENT_GOLD);

        // Title
        context.drawText(textRenderer, "HEGEMONIA",
                logoX + 28, logoY, HegemoniaColors.ACCENT_GOLD, true);
        context.drawText(textRenderer, "Simulation Geopolitique",
                logoX + 28, logoY + 14, HegemoniaColors.TEXT_MUTED, false);

        // Player welcome message
        String playerName = client != null && client.player != null ? client.player.getName().getString() : "Joueur";
        context.drawText(textRenderer, "Bienvenue, " + playerName,
                logoX, logoY + 38, HegemoniaColors.TEXT_SECONDARY, false);

        // Balance display (right side)
        String balanceLabel = "Fortune totale";
        String balanceValue = MONEY_FORMAT.format(data.getTotalBalance()) + " H";
        int balanceLabelWidth = textRenderer.getWidth(balanceLabel);
        int balanceValueWidth = textRenderer.getWidth(balanceValue);
        int balanceX = contentX + contentWidth - Math.max(balanceLabelWidth, balanceValueWidth) - 25;

        context.drawText(textRenderer, balanceLabel,
                balanceX, logoY + 5, HegemoniaColors.TEXT_MUTED, false);
        context.drawText(textRenderer, balanceValue,
                balanceX, logoY + 18, HegemoniaColors.ACCENT_GOLD, true);

        // Nation indicator
        if (data.hasNation()) {
            String nationTag = "[" + data.nationTag + "]";
            context.drawText(textRenderer, nationTag,
                    balanceX, logoY + 38, HegemoniaColors.ACCENT_BLUE, false);
        }

        // Header bottom border
        context.fill(contentX, contentY + headerHeight - 1, contentX + contentWidth, contentY + headerHeight,
                HegemoniaColors.PANEL_BORDER);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        int cardWidth = (contentWidth - 70) / 2;
        int cardHeight = 85;
        int cardSpacing = 15;
        int startY = contentY + 95;
        int leftX = contentX + 20;
        int rightX = contentX + cardWidth + 35;

        // ═══════════════════════════════════════════════════════════════
        // RENDER CARD CONTENTS
        // ═══════════════════════════════════════════════════════════════

        // Economy Card Content
        renderCardContent(context, leftX, startY, cardWidth, cardHeight,
                "Economie", "Gerez votre fortune",
                MONEY_FORMAT.format(data.balance) + " H",
                HegemoniaColors.ACCENT_GOLD, 0);

        // Nation Card Content
        String nationStatus = data.hasNation() ? data.nationName : "Sans nation";
        String nationDesc = data.hasNation() ? "Role: " + formatRole(data.nationRole) : "Rejoignez ou creez";
        renderCardContent(context, rightX, startY, cardWidth, cardHeight,
                "Nation", nationDesc,
                nationStatus,
                HegemoniaColors.ACCENT_BLUE, 1);

        // War Card Content
        String warStatus = data.atWar ? "EN GUERRE" : "En paix";
        String warDesc = data.atWar ? "vs " + data.warTarget : "Aucun conflit actif";
        int warColor = data.atWar ? HegemoniaColors.ERROR : HegemoniaColors.SUCCESS;
        renderCardContent(context, leftX, startY + cardHeight + cardSpacing, cardWidth, cardHeight,
                "Guerre", warDesc,
                warStatus,
                warColor, 2);

        // Territory Card Content
        renderCardContent(context, rightX, startY + cardHeight + cardSpacing, cardWidth, cardHeight,
                "Territoires", "Carte et regions",
                "Explorer",
                HegemoniaColors.SUCCESS, 3);

        // Version footer
        String version = "v1.0.0";
        context.drawText(textRenderer, version,
                contentX + contentWidth - textRenderer.getWidth(version) - 10,
                contentY + contentHeight - 15,
                HegemoniaColors.TEXT_MUTED, false);
    }

    private void renderCardContent(DrawContext context, int x, int y, int width, int height,
                                   String title, String subtitle, String value, int accentColor, int cardIndex) {
        float anim = cardAnimations[cardIndex];
        if (anim < 0.01f) return;

        int alpha = (int) (255 * anim);

        // Card accent bar on left
        context.fill(x + 2, y + 8, x + 5, y + height - 8,
                HegemoniaColors.withAlpha(accentColor, alpha));

        // Icon area background
        context.fill(x + 10, y + 10, x + 35, y + 35,
                HegemoniaColors.withAlpha(accentColor, (int) (30 * anim)));

        // Title
        context.drawText(textRenderer, title,
                x + 42, y + 12, HegemoniaColors.withAlpha(HegemoniaColors.TEXT_PRIMARY, alpha), false);

        // Subtitle
        context.drawText(textRenderer, subtitle,
                x + 42, y + 26, HegemoniaColors.withAlpha(HegemoniaColors.TEXT_MUTED, alpha), false);

        // Value (large)
        String displayValue = value.length() > 18 ? value.substring(0, 16) + "..." : value;
        context.drawText(textRenderer, displayValue,
                x + 12, y + height - 25, HegemoniaColors.withAlpha(accentColor, alpha), true);

        // Arrow indicator
        context.drawText(textRenderer, ">",
                x + width - 18, y + height / 2 - 4,
                HegemoniaColors.withAlpha(HegemoniaColors.TEXT_MUTED, (int) (alpha * 0.5f)), false);
    }

    private void drawCrownIcon(DrawContext context, int x, int y, int color) {
        // Simple crown shape
        // Base
        context.fill(x + 2, y + 12, x + 18, y + 16, color);
        // Left peak
        context.fill(x + 2, y + 4, x + 6, y + 12, color);
        // Center peak
        context.fill(x + 8, y, x + 12, y + 12, color);
        // Right peak
        context.fill(x + 14, y + 4, x + 18, y + 12, color);
    }

    private String formatRole(String role) {
        if (role == null) return "Membre";
        return switch (role.toUpperCase()) {
            case "LEADER" -> "Chef";
            case "OFFICER" -> "Officier";
            case "MEMBER" -> "Membre";
            case "RECRUIT" -> "Recrue";
            default -> role;
        };
    }
}
