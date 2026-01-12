package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Transaction history screen
 */
public class TransactionHistoryScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private HegemoniaScrollPanel historyPanel;
    private String currentFilter = "ALL";

    public TransactionHistoryScreen() {
        super("Historique des transactions");
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
        // Filter buttons
        int filterY = contentY + 45;
        int filterWidth = 80;
        String[] filters = {"ALL", "IN", "OUT", "MARKET"};
        String[] filterLabels = {"Tout", "Entrees", "Sorties", "Marche"};

        for (int i = 0; i < filters.length; i++) {
            final String filter = filters[i];
            addWidget(new HegemoniaButton(
                    contentX + 15 + (i * (filterWidth + 5)), filterY,
                    filterWidth, 28,
                    filterLabels[i],
                    filter.equals(currentFilter) ? HegemoniaButton.ButtonStyle.PRIMARY : HegemoniaButton.ButtonStyle.GHOST,
                    btn -> {
                        currentFilter = filter;
                        clearAndInit();
                    }
            ));
        }

        // History panel
        historyPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 85,
                contentWidth - 30, 270
        ));
        historyPanel.setPadding(5).setItemSpacing(3);
        loadTransactions();

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 50,
                100, 35,
                "Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void loadTransactions() {
        historyPanel.clearChildren();

        // Placeholder transactions
        String[][] transactions = {
                {"+500.00", "Vente de diamants", "MARKET", "Il y a 30min"},
                {"-150.00", "Achat de fer", "MARKET", "Il y a 1h"},
                {"+1000.00", "Transfert de Player1", "IN", "Il y a 2h"},
                {"-200.00", "Transfert a Player2", "OUT", "Il y a 3h"},
                {"+50.00", "Interets bancaires", "IN", "Il y a 1j"},
                {"-500.00", "Taxe nationale", "OUT", "Il y a 1j"},
                {"+2500.00", "Salaire", "IN", "Il y a 2j"},
                {"-1200.00", "Achat d'equipement", "MARKET", "Il y a 2j"},
                {"+300.00", "Vente de bois", "MARKET", "Il y a 3j"},
                {"-100.00", "Don a la nation", "OUT", "Il y a 3j"},
        };

        int itemWidth = historyPanel.getContentWidth() - 10;

        for (String[] data : transactions) {
            // Apply filter
            if (!currentFilter.equals("ALL")) {
                if (currentFilter.equals("IN") && !data[0].startsWith("+")) continue;
                if (currentFilter.equals("OUT") && !data[0].startsWith("-")) continue;
                if (currentFilter.equals("MARKET") && !data[2].equals("MARKET")) continue;
            }

            boolean isIncome = data[0].startsWith("+");

            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 38,
                    data[1],
                    data[3]
            );

            int amountColor = isIncome ? HegemoniaDesign.SUCCESS : HegemoniaDesign.ERROR;
            item.setRightText(data[0] + " H", amountColor);
            item.setSelectable(false);
            historyPanel.addChild(item);
        }

        // Empty state
        if (historyPanel.getChildCount() == 0) {
            HegemoniaListItem emptyItem = new HegemoniaListItem(
                    0, 0, itemWidth, 35,
                    "Aucune transaction"
            );
            emptyItem.setSelectable(false);
            historyPanel.addChild(emptyItem);
        }
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Summary at bottom
        int summaryY = contentY + contentHeight - 45;

        // Could add summary statistics here
        context.drawText(textRenderer, "Total entrees: +5,350.00 H",
                contentX + 150, summaryY, HegemoniaDesign.SUCCESS, false);
        context.drawText(textRenderer, "Total sorties: -2,150.00 H",
                contentX + 350, summaryY, HegemoniaDesign.ERROR, false);
    }
}
