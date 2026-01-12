package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaPanel;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import com.hegemonia.client.gui.widget.HegemoniaTextInput;
import net.minecraft.client.gui.DrawContext;

/**
 * Treasury management screen for nation finances
 */
public class TreasuryScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel transactionsPanel;
    private HegemoniaTextInput amountInput;
    private double nationBalance = 125000.0;
    private double dailyIncome = 2500.0;
    private double dailyExpenses = 1200.0;

    public TreasuryScreen() {
        super("Tresor National");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(550, (int) (screenWidth * 0.85));
        contentHeight = Math.min(420, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        // Balance panel
        HegemoniaPanel balancePanel = addWidget(new HegemoniaPanel(
                contentX + 15, contentY + 45,
                contentWidth - 30, 80,
                "Finances"
        ));

        // Amount input
        amountInput = addWidget(new HegemoniaTextInput(
                contentX + 15, contentY + 140,
                150, 30,
                "Montant..."
        ));
        amountInput.setNumericOnly(true);

        // Deposit button
        addWidget(new HegemoniaButton(
                contentX + 175, contentY + 140,
                100, 30,
                "Deposer",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> depositToTreasury()
        ));

        // Withdraw button
        addWidget(new HegemoniaButton(
                contentX + 285, contentY + 140,
                100, 30,
                "Retirer",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> withdrawFromTreasury()
        ));

        // Transaction history panel
        transactionsPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 190,
                contentWidth - 30, 160
        ));
        transactionsPanel.setPadding(5).setItemSpacing(2);
        addTransactionHistory();

        // Back button
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 115, contentY + contentHeight - 50,
                100, 35,
                "Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void addTransactionHistory() {
        // Placeholder transactions - real data from server
        String[][] transactions = {
                {"+5,000 H", "Taxe journaliere", "Il y a 2h", "income"},
                {"-1,200 H", "Entretien territoire", "Il y a 2h", "expense"},
                {"+10,000 H", "Depot par LeaderName", "Il y a 5h", "deposit"},
                {"-3,000 H", "Achat equipement", "Il y a 1j", "expense"},
                {"+2,500 H", "Tribut du vassal", "Il y a 1j", "income"},
                {"-500 H", "Retrait par Officer1", "Il y a 2j", "withdraw"},
        };

        int itemWidth = transactionsPanel.getContentWidth() - 10;

        for (String[] data : transactions) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 26,
                    data[0] + " - " + data[1]
            );

            int amountColor = data[3].equals("income") || data[3].equals("deposit")
                    ? HegemoniaDesign.SUCCESS
                    : HegemoniaDesign.ERROR;

            item.setRightText(data[2], HegemoniaDesign.TEXT_MUTED);
            item.setSelectable(false);
            transactionsPanel.addChild(item);
        }
    }

    private void depositToTreasury() {
        String amountStr = amountInput.getText();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    hegemonia.getNetworkHandler().requestTreasuryDeposit(amount);
                    amountInput.setText("");
                }
            } catch (NumberFormatException e) {
                // Invalid amount
            }
        }
    }

    private void withdrawFromTreasury() {
        String amountStr = amountInput.getText();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0 && amount <= nationBalance) {
                    hegemonia.getNetworkHandler().requestTreasuryWithdraw(amount);
                    amountInput.setText("");
                }
            } catch (NumberFormatException e) {
                // Invalid amount
            }
        }
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Balance display
        int balanceY = contentY + 65;
        int col1 = contentX + 30;
        int col2 = contentX + 200;
        int col3 = contentX + 370;

        // Current balance
        context.drawText(textRenderer, "Solde:", col1, balanceY, HegemoniaDesign.TEXT_MUTED, false);
        String balanceStr = String.format("%,.0f H", nationBalance);
        context.drawText(textRenderer, balanceStr, col1 + 45, balanceY, HegemoniaDesign.GOLD, true);

        // Daily income
        context.drawText(textRenderer, "Revenus/j:", col2, balanceY, HegemoniaDesign.TEXT_MUTED, false);
        String incomeStr = String.format("+%,.0f H", dailyIncome);
        context.drawText(textRenderer, incomeStr, col2 + 60, balanceY, HegemoniaDesign.SUCCESS, false);

        // Daily expenses
        context.drawText(textRenderer, "Depenses/j:", col3, balanceY, HegemoniaDesign.TEXT_MUTED, false);
        String expenseStr = String.format("-%,.0f H", dailyExpenses);
        context.drawText(textRenderer, expenseStr, col3 + 70, balanceY, HegemoniaDesign.ERROR, false);

        // Net income
        double netIncome = dailyIncome - dailyExpenses;
        int netY = balanceY + 20;
        context.drawText(textRenderer, "Net:", col1, netY, HegemoniaDesign.TEXT_MUTED, false);
        String netStr = String.format("%+,.0f H/jour", netIncome);
        int netColor = netIncome >= 0 ? HegemoniaDesign.SUCCESS : HegemoniaDesign.ERROR;
        context.drawText(textRenderer, netStr, col1 + 30, netY, netColor, false);

        // Section header for transactions
        context.drawText(textRenderer, "Historique des transactions",
                contentX + 20, contentY + 178, HegemoniaDesign.TEXT_MUTED, false);
    }
}
