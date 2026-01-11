package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Main Hegemonia menu - hub for all features
 */
public class MainMenuScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public MainMenuScreen() {
        super("HEGEMONIA");
    }

    @Override
    protected void calculateContentArea() {
        // Wider menu for main screen
        contentWidth = Math.min(450, (int) (screenWidth * 0.8));
        contentHeight = Math.min(400, (int) (screenHeight * 0.75));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        int buttonWidth = contentWidth - 60;
        int buttonHeight = 40;
        int spacing = 10;
        int startY = contentY + 80;

        // Economy button
        addWidget(new HegemoniaButton(
                contentX + 30, startY,
                buttonWidth, buttonHeight,
                "💰  Économie",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> hegemonia.getScreenManager().openEconomyMenu()
        ));

        // Nation button
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        String nationText = data.hasNation() ?
                "🏛  " + data.nationName + " [" + data.nationTag + "]" :
                "🏛  Nations";

        addWidget(new HegemoniaButton(
                contentX + 30, startY + buttonHeight + spacing,
                buttonWidth, buttonHeight,
                nationText,
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> hegemonia.getScreenManager().openNationMenu()
        ));

        // War button
        HegemoniaButton warButton = addWidget(new HegemoniaButton(
                contentX + 30, startY + (buttonHeight + spacing) * 2,
                buttonWidth, buttonHeight,
                data.atWar ? "⚔  Guerre - " + data.warTarget : "⚔  Guerre",
                data.atWar ? HegemoniaButton.ButtonStyle.DANGER : HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> hegemonia.getScreenManager().openWarMenu()
        ));

        // Only enable war if player has a nation
        warButton.setEnabled(data.hasNation());

        // Settings button (smaller, at bottom)
        addWidget(new HegemoniaButton(
                contentX + 30, contentY + contentHeight - 60,
                buttonWidth, 30,
                "⚙  Paramètres",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openSettingsMenu()
        ));
    }

    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY, float delta) {
        // Custom header for main menu
        int headerHeight = 60;
        context.fill(contentX, contentY, contentX + contentWidth, contentY + headerHeight, HegemoniaColors.PANEL_HEADER);

        // Logo/Title
        String title = "HEGEMONIA";
        int titleWidth = textRenderer.getWidth(title) * 2; // Approximate scaled width
        context.drawText(textRenderer, "§6§l" + title, contentX + 20, contentY + 15, HegemoniaColors.ACCENT_GOLD, true);

        // Subtitle
        context.drawText(textRenderer, "§7Simulation Géopolitique", contentX + 20, contentY + 30, HegemoniaColors.TEXT_SECONDARY, true);

        // Player balance on right side
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        String balance = "§6" + MONEY_FORMAT.format(data.getTotalBalance()) + " §7H";
        int balanceWidth = textRenderer.getWidth(balance.replaceAll("§.", ""));
        context.drawText(textRenderer, balance, contentX + contentWidth - balanceWidth - 40, contentY + 22, 0xFFFFFF, true);

        // Header bottom border
        context.fill(contentX, contentY + headerHeight - 1, contentX + contentWidth, contentY + headerHeight, HegemoniaColors.ACCENT_GOLD);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Footer with version
        String version = "v1.0.0";
        int versionWidth = textRenderer.getWidth(version);
        context.drawText(textRenderer, version,
                contentX + contentWidth - versionWidth - 10,
                contentY + contentHeight - 15,
                HegemoniaColors.TEXT_MUTED, false);
    }
}
