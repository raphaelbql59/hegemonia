package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaPanel;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Economy overview screen
 */
public class EconomyScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    public EconomyScreen() {
        super("Économie");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(500, (int) (screenWidth * 0.8));
        contentHeight = Math.min(450, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int panelWidth = (contentWidth - 50) / 2;
        int panelHeight = 100;

        // Balance panel
        HegemoniaPanel balancePanel = addWidget(new HegemoniaPanel(
                contentX + 15, contentY + 50,
                panelWidth, panelHeight,
                "Portefeuille"
        ));

        // Bank panel
        HegemoniaPanel bankPanel = addWidget(new HegemoniaPanel(
                contentX + 25 + panelWidth, contentY + 50,
                panelWidth, panelHeight,
                "Banque"
        ));

        // Action buttons
        int buttonY = contentY + 170;
        int buttonWidth = panelWidth - 20;
        int buttonHeight = 35;

        // Bank button
        addWidget(new HegemoniaButton(
                contentX + 25, buttonY,
                buttonWidth, buttonHeight,
                "🏦  Accéder à la Banque",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> hegemonia.getScreenManager().openBankMenu()
        ));

        // Market button
        addWidget(new HegemoniaButton(
                contentX + 35 + panelWidth, buttonY,
                buttonWidth, buttonHeight,
                "🛒  Marché",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> hegemonia.getScreenManager().openMarketMenu()
        ));

        // Quick actions
        int quickY = buttonY + buttonHeight + 40;

        addWidget(new HegemoniaButton(
                contentX + 25, quickY,
                buttonWidth, 30,
                "Transferer",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> navigateTo(new TransferScreen())
        ));

        addWidget(new HegemoniaButton(
                contentX + 35 + panelWidth, quickY,
                buttonWidth, 30,
                "Historique",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> navigateTo(new TransactionHistoryScreen())
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 50,
                100, 30,
                "← Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int panelWidth = (contentWidth - 50) / 2;

        // Wallet amount
        String walletAmount = MONEY_FORMAT.format(data.balance) + " H";
        context.drawText(textRenderer, "§6§l" + walletAmount,
                contentX + 25, contentY + 95, HegemoniaColors.MONEY_NEUTRAL, true);

        // Bank amount
        String bankAmount = MONEY_FORMAT.format(data.bankBalance) + " H";
        context.drawText(textRenderer, "§e§l" + bankAmount,
                contentX + 35 + panelWidth, contentY + 95, HegemoniaColors.MONEY_NEUTRAL, true);

        // Total wealth
        context.fill(contentX + 15, contentY + 135, contentX + contentWidth - 15, contentY + 160, HegemoniaColors.BACKGROUND_LIGHT);
        String totalText = "Fortune totale: §6" + MONEY_FORMAT.format(data.getTotalBalance()) + " §7Hegemonia";
        int totalWidth = textRenderer.getWidth(totalText.replaceAll("§.", ""));
        context.drawText(textRenderer, totalText,
                contentX + (contentWidth - totalWidth) / 2, contentY + 143, HegemoniaColors.TEXT_PRIMARY, true);
    }
}
