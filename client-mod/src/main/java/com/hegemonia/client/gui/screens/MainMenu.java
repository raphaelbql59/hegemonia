package com.hegemonia.client.gui.screens;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.components.BaseScreen;
import com.hegemonia.client.gui.components.FlatButton;
import com.hegemonia.client.gui.theme.HegemoniaTheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;

/**
 * Main Hegemonia Menu
 * Modern flat design with logo and quick access cards
 */
public class MainMenu extends BaseScreen {

    private static final Identifier LOGO = new Identifier("hegemonia", "textures/gui/logo.png");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    // Animation
    private float openAnim = 0f;

    public MainMenu() {
        super("HEGEMONIA");
    }

    @Override
    protected void calculateLayout() {
        panelWidth = Math.min(360, (int) (width * 0.85));
        panelHeight = Math.min(340, (int) (height * 0.8));
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    @Override
    protected void initComponents() {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        int contentY = getContentY() + 50; // After logo area
        int buttonWidth = panelWidth - HegemoniaTheme.SPACE_MD * 2;
        int buttonHeight = HegemoniaTheme.BUTTON_HEIGHT;
        int spacing = HegemoniaTheme.SPACE_SM;
        int leftX = panelX + HegemoniaTheme.SPACE_MD;

        // Main navigation buttons
        addButton(new FlatButton(
                leftX, contentY,
                buttonWidth, buttonHeight,
                "Economie",
                FlatButton.Style.PRIMARY,
                btn -> hegemonia.getScreenManager().openEconomyMenu()
        ));

        addButton(new FlatButton(
                leftX, contentY + buttonHeight + spacing,
                buttonWidth, buttonHeight,
                data.hasNation() ? data.nationName : "Nations",
                FlatButton.Style.SECONDARY,
                btn -> hegemonia.getScreenManager().openNationMenu()
        ));

        FlatButton warBtn = addButton(new FlatButton(
                leftX, contentY + (buttonHeight + spacing) * 2,
                buttonWidth, buttonHeight,
                data.atWar ? "Guerre - En conflit" : "Guerre",
                data.atWar ? FlatButton.Style.DANGER : FlatButton.Style.GHOST,
                btn -> hegemonia.getScreenManager().openWarMenu()
        ));
        warBtn.setEnabled(data.hasNation());

        addButton(new FlatButton(
                leftX, contentY + (buttonHeight + spacing) * 3,
                buttonWidth, buttonHeight,
                "Marche",
                FlatButton.Style.GHOST,
                btn -> hegemonia.getScreenManager().openMarketMenu()
        ));

        // Bottom buttons row
        int bottomY = panelY + panelHeight - HegemoniaTheme.SPACE_MD - HegemoniaTheme.BUTTON_HEIGHT_SM;
        int smallWidth = (buttonWidth - HegemoniaTheme.SPACE_SM) / 2;

        addButton(new FlatButton(
                leftX, bottomY,
                smallWidth, HegemoniaTheme.BUTTON_HEIGHT_SM,
                "Parametres",
                FlatButton.Style.GHOST,
                btn -> hegemonia.getScreenManager().openSettingsMenu()
        ));

        addButton(new FlatButton(
                leftX + smallWidth + HegemoniaTheme.SPACE_SM, bottomY,
                smallWidth, HegemoniaTheme.BUTTON_HEIGHT_SM,
                "Fermer",
                FlatButton.Style.GHOST,
                btn -> close()
        ));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Animate opening
        openAnim = Math.min(1f, openAnim + 0.1f);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    protected void renderHeader(DrawContext ctx) {
        int headerHeight = 80; // Taller header for logo

        // Header background
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + headerHeight, HegemoniaTheme.BG_SECONDARY);

        // Gold accent at top
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + 3, HegemoniaTheme.ACCENT_GOLD);

        // Header bottom border
        ctx.fill(panelX, panelY + headerHeight - 1, panelX + panelWidth, panelY + headerHeight, HegemoniaTheme.BORDER_DEFAULT);

        // Draw logo (32x32)
        int logoSize = 32;
        int logoX = panelX + HegemoniaTheme.SPACE_MD;
        int logoY = panelY + (headerHeight - logoSize) / 2;

        try {
            ctx.drawTexture(LOGO, logoX, logoY, 0, 0, logoSize, logoSize, logoSize, logoSize);
        } catch (Exception e) {
            // Fallback: draw a simple crown shape
            drawFallbackLogo(ctx, logoX, logoY, logoSize);
        }

        // Title and subtitle
        int textX = logoX + logoSize + HegemoniaTheme.SPACE_MD;
        ctx.drawText(textRenderer, "HEGEMONIA", textX, logoY + 4, HegemoniaTheme.ACCENT_GOLD, false);
        ctx.drawText(textRenderer, "Simulation Geopolitique", textX, logoY + 16, HegemoniaTheme.TEXT_MUTED, false);

        // Player balance on right
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        String balance = MONEY_FORMAT.format(data.getTotalBalance()) + " H";
        int balanceWidth = textRenderer.getWidth(balance);
        ctx.drawText(textRenderer, balance,
                panelX + panelWidth - balanceWidth - HegemoniaTheme.SPACE_MD,
                logoY + 10,
                HegemoniaTheme.ACCENT_GOLD, false);
    }

    private void drawFallbackLogo(DrawContext ctx, int x, int y, int size) {
        // Simple crown icon as fallback
        int color = HegemoniaTheme.ACCENT_GOLD;
        int s = size / 4;

        // Base
        ctx.fill(x + s, y + size - s * 2, x + size - s, y + size - s, color);

        // Peaks
        ctx.fill(x + s, y + s * 2, x + s + 4, y + size - s * 2, color);
        ctx.fill(x + size / 2 - 2, y + s, x + size / 2 + 2, y + size - s * 2, color);
        ctx.fill(x + size - s - 4, y + s * 2, x + size - s, y + size - s * 2, color);
    }

    @Override
    protected void renderContent(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Player status bar
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        int statusY = panelY + 82;
        int statusX = panelX + HegemoniaTheme.SPACE_MD;

        // Welcome message
        String playerName = client != null && client.player != null ? client.player.getName().getString() : "Joueur";
        ctx.drawText(textRenderer, "Bienvenue, " + playerName,
                statusX, statusY, HegemoniaTheme.TEXT_SECONDARY, false);

        // Status indicators
        int indicatorY = statusY + 14;

        // Nation status
        if (data.hasNation()) {
            ctx.drawText(textRenderer, "[" + data.nationTag + "]",
                    statusX, indicatorY, HegemoniaTheme.ACCENT_BLUE, false);
        } else {
            ctx.drawText(textRenderer, "Sans nation",
                    statusX, indicatorY, HegemoniaTheme.TEXT_MUTED, false);
        }

        // War status
        String warStatus = data.atWar ? "En guerre" : "En paix";
        int warColor = data.atWar ? HegemoniaTheme.ERROR : HegemoniaTheme.SUCCESS;
        int warX = panelX + panelWidth - textRenderer.getWidth(warStatus) - HegemoniaTheme.SPACE_MD;
        ctx.drawText(textRenderer, warStatus, warX, indicatorY, warColor, false);

        // Version at bottom
        String version = "v1.0.0";
        ctx.drawText(textRenderer, version,
                panelX + panelWidth - textRenderer.getWidth(version) - HegemoniaTheme.SPACE_SM,
                panelY + panelHeight - 12,
                HegemoniaTheme.TEXT_MUTED, false);
    }

    @Override
    protected int getContentY() {
        return panelY + 80; // After taller header
    }
}
