package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaTextInput;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Bank screen for deposits and withdrawals
 */
public class BankScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private HegemoniaTextInput amountInput;
    private String errorMessage = null;

    public BankScreen() {
        super("Banque");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(400, (int) (screenWidth * 0.7));
        contentHeight = Math.min(350, (int) (screenHeight * 0.7));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        int centerX = contentX + contentWidth / 2;
        int inputWidth = contentWidth - 80;

        // Amount input
        amountInput = addWidget(new HegemoniaTextInput(
                centerX - inputWidth / 2, contentY + 140,
                inputWidth, 30,
                "Montant..."
        ));
        amountInput.setValidator(text -> {
            if (text.isEmpty()) return true;
            try {
                double val = Double.parseDouble(text.replace(",", "."));
                return val >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        // Quick amount buttons
        int quickY = contentY + 180;
        int quickWidth = (inputWidth - 30) / 4;

        addWidget(new HegemoniaButton(centerX - inputWidth / 2, quickY, quickWidth, 25, "100",
                HegemoniaButton.ButtonStyle.GHOST, btn -> amountInput.setText("100")));
        addWidget(new HegemoniaButton(centerX - inputWidth / 2 + quickWidth + 10, quickY, quickWidth, 25, "500",
                HegemoniaButton.ButtonStyle.GHOST, btn -> amountInput.setText("500")));
        addWidget(new HegemoniaButton(centerX - inputWidth / 2 + (quickWidth + 10) * 2, quickY, quickWidth, 25, "1000",
                HegemoniaButton.ButtonStyle.GHOST, btn -> amountInput.setText("1000")));
        addWidget(new HegemoniaButton(centerX - inputWidth / 2 + (quickWidth + 10) * 3, quickY, quickWidth, 25, "MAX",
                HegemoniaButton.ButtonStyle.GHOST, btn -> setMaxAmount()));

        // Action buttons
        int buttonY = contentY + 220;
        int buttonWidth = (inputWidth - 20) / 2;

        addWidget(new HegemoniaButton(
                centerX - inputWidth / 2, buttonY,
                buttonWidth, 40,
                "📥 Déposer",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> deposit()
        ));

        addWidget(new HegemoniaButton(
                centerX + 10, buttonY,
                buttonWidth, 40,
                "📤 Retirer",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> withdraw()
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 50,
                100, 30,
                "← Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openEconomyMenu()
        ));
    }

    private void setMaxAmount() {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        amountInput.setText(MONEY_FORMAT.format(data.balance).replace(",", ""));
    }

    private double getAmount() {
        try {
            return Double.parseDouble(amountInput.getText().replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void deposit() {
        double amount = getAmount();
        if (amount <= 0) {
            errorMessage = "Montant invalide";
            return;
        }

        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        if (amount > data.balance) {
            errorMessage = "Fonds insuffisants";
            return;
        }

        errorMessage = null;
        hegemonia.getNetworkHandler().requestBankDeposit(amount);
        amountInput.setText("");
    }

    private void withdraw() {
        double amount = getAmount();
        if (amount <= 0) {
            errorMessage = "Montant invalide";
            return;
        }

        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        if (amount > data.bankBalance) {
            errorMessage = "Fonds en banque insuffisants";
            return;
        }

        errorMessage = null;
        hegemonia.getNetworkHandler().requestBankWithdraw(amount);
        amountInput.setText("");
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int centerX = contentX + contentWidth / 2;

        // Current balances
        context.fill(contentX + 20, contentY + 50, contentX + contentWidth - 20, contentY + 120, HegemoniaDesign.BACKGROUND_LIGHT);

        // Wallet
        String walletLabel = "Portefeuille:";
        String walletAmount = MONEY_FORMAT.format(data.balance) + " H";
        context.drawText(textRenderer, walletLabel, contentX + 35, contentY + 60, HegemoniaDesign.TEXT_SECONDARY, true);
        context.drawText(textRenderer, "§6" + walletAmount, contentX + 35, contentY + 75, HegemoniaDesign.MONEY_NEUTRAL, true);

        // Bank
        String bankLabel = "En banque:";
        String bankAmount = MONEY_FORMAT.format(data.bankBalance) + " H";
        int bankX = centerX + 20;
        context.drawText(textRenderer, bankLabel, bankX, contentY + 60, HegemoniaDesign.TEXT_SECONDARY, true);
        context.drawText(textRenderer, "§e" + bankAmount, bankX, contentY + 75, HegemoniaDesign.MONEY_NEUTRAL, true);

        // Interest rate info
        String interestText = "§7Intérêts: §a+0.5% §7par jour";
        int interestWidth = textRenderer.getWidth(interestText.replaceAll("§.", ""));
        context.drawText(textRenderer, interestText, centerX - interestWidth / 2, contentY + 100, 0xFFFFFF, true);

        // Error message
        if (errorMessage != null) {
            int errorWidth = textRenderer.getWidth(errorMessage);
            context.drawText(textRenderer, "§c" + errorMessage, centerX - errorWidth / 2, contentY + 280, HegemoniaDesign.ERROR, true);
        }
    }
}
