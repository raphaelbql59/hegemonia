package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import com.hegemonia.client.gui.widget.HegemoniaTextInput;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Money transfer screen
 */
public class TransferScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private HegemoniaTextInput recipientInput;
    private HegemoniaTextInput amountInput;
    private HegemoniaScrollPanel recentPanel;
    private String selectedRecipient = null;

    public TransferScreen() {
        super("Transfert d'argent");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(450, (int) (screenWidth * 0.75));
        contentHeight = Math.min(380, (int) (screenHeight * 0.75));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Recipient input
        recipientInput = addWidget(new HegemoniaTextInput(
                contentX + 15, contentY + 70,
                contentWidth - 30, 30,
                "Nom du destinataire..."
        ));

        // Amount input
        amountInput = addWidget(new HegemoniaTextInput(
                contentX + 15, contentY + 115,
                200, 30,
                "Montant..."
        ));
        amountInput.setNumericOnly(true);

        // Quick amount buttons
        int[] quickAmounts = {100, 500, 1000, 5000};
        int quickX = contentX + 225;
        for (int i = 0; i < quickAmounts.length; i++) {
            final int amount = quickAmounts[i];
            addWidget(new HegemoniaButton(
                    quickX + (i % 2) * 55, contentY + 115 + (i / 2) * 32,
                    50, 28,
                    String.valueOf(amount),
                    HegemoniaButton.ButtonStyle.GHOST,
                    btn -> amountInput.setText(String.valueOf(amount))
            ));
        }

        // Recent transfers panel
        recentPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 185,
                contentWidth - 30, 110
        ));
        recentPanel.setPadding(5).setItemSpacing(2);
        loadRecentTransfers();

        // Transfer button
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 130, contentY + contentHeight - 55,
                115, 40,
                "Envoyer",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> executeTransfer()
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 55,
                100, 40,
                "Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void loadRecentTransfers() {
        recentPanel.clearChildren();

        // Placeholder recent recipients
        String[][] recent = {
                {"PlayerOne", "Il y a 2h"},
                {"PlayerTwo", "Il y a 1j"},
                {"PlayerThree", "Il y a 3j"},
        };

        int itemWidth = recentPanel.getContentWidth() - 10;

        for (String[] data : recent) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 28,
                    data[0]
            );
            item.setRightText(data[1], HegemoniaDesign.TEXT_MUTED);
            item.setSelectable(true);
            item.setOnClick(() -> {
                recipientInput.setText(data[0]);
                selectedRecipient = data[0];
            });
            recentPanel.addChild(item);
        }
    }

    private void executeTransfer() {
        String recipient = recipientInput.getText().trim();
        String amountStr = amountInput.getText().trim();

        if (recipient.isEmpty() || amountStr.isEmpty()) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

            if (amount <= 0) {
                return;
            }

            if (amount > data.balance) {
                // Not enough funds
                return;
            }

            // Confirm transfer
            if (client != null) {
                client.setScreen(ConfirmationDialog.confirm(
                        this,
                        "Confirmer le transfert",
                        "Envoyer " + MONEY_FORMAT.format(amount) + " H\na " + recipient + "?",
                        "Envoyer",
                        () -> {
                            hegemonia.getNetworkHandler().requestTransfer(recipient, amount);
                            goBack();
                        }
                ));
            }
        } catch (NumberFormatException e) {
            // Invalid amount
        }
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Labels
        context.drawText(textRenderer, "Destinataire:",
                contentX + 20, contentY + 58, HegemoniaDesign.TEXT_MUTED, false);

        context.drawText(textRenderer, "Montant:",
                contentX + 20, contentY + 103, HegemoniaDesign.TEXT_MUTED, false);

        // Balance display
        String balanceText = "Solde: " + MONEY_FORMAT.format(data.balance) + " H";
        context.drawText(textRenderer, balanceText,
                contentX + 20, contentY + 155, HegemoniaDesign.GOLD, false);

        // Recent section header
        context.drawText(textRenderer, "Transferts recents",
                contentX + 20, contentY + 173, HegemoniaDesign.TEXT_MUTED, false);
    }
}
