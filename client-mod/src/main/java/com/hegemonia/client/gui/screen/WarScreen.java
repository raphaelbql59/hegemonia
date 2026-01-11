package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaPanel;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import net.minecraft.client.gui.DrawContext;

/**
 * War management screen
 */
public class WarScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel activeWarsPanel;
    private HegemoniaScrollPanel historyPanel;

    public WarScreen() {
        super("Guerre");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(550, (int) (screenWidth * 0.85));
        contentHeight = Math.min(450, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int panelWidth = (contentWidth - 45) / 2;

        // Active wars panel
        HegemoniaPanel activePanel = addWidget(new HegemoniaPanel(
                contentX + 15, contentY + 45,
                panelWidth, 200,
                "Guerres Actives"
        ));

        activeWarsPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 20, contentY + 75,
                panelWidth - 10, 160
        ));
        activeWarsPanel.setPadding(3).setItemSpacing(3);

        // War history panel
        HegemoniaPanel historyPanelWidget = addWidget(new HegemoniaPanel(
                contentX + panelWidth + 25, contentY + 45,
                panelWidth, 200,
                "Historique"
        ));

        historyPanel = addWidget(new HegemoniaScrollPanel(
                contentX + panelWidth + 30, contentY + 75,
                panelWidth - 10, 160
        ));
        historyPanel.setPadding(3).setItemSpacing(3);

        // Load data
        loadActiveWars();
        loadWarHistory();

        // Action buttons
        int buttonY = contentY + 260;

        // Declare war button
        HegemoniaButton declareButton = addWidget(new HegemoniaButton(
                contentX + 15, buttonY,
                panelWidth, 40,
                "⚔ Déclarer la guerre",
                HegemoniaButton.ButtonStyle.DANGER,
                btn -> {} // TODO: War declaration screen
        ));

        // Only leaders/officers can declare war
        declareButton.setEnabled(data.hasNation() &&
                (data.nationRole.equals("LEADER") || data.nationRole.equals("OFFICER")));

        // Surrender button (only if at war)
        if (data.atWar) {
            addWidget(new HegemoniaButton(
                    contentX + panelWidth + 25, buttonY,
                    panelWidth, 40,
                    "🏳 Capituler",
                    HegemoniaButton.ButtonStyle.DEFAULT,
                    btn -> {} // TODO: Surrender confirmation
            ));
        }

        // Statistics panel
        HegemoniaPanel statsPanel = addWidget(new HegemoniaPanel(
                contentX + 15, buttonY + 55,
                contentWidth - 30, 60,
                "Statistiques"
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 45,
                100, 30,
                "← Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void loadActiveWars() {
        activeWarsPanel.clearChildren();

        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int itemWidth = activeWarsPanel.getContentWidth() - 5;

        if (data.atWar && data.warTarget != null) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 40,
                    "§c⚔ vs " + data.warTarget,
                    "§7En cours depuis 2 jours"
            );
            item.setRightText("§cActif", HegemoniaColors.WAR_ACTIVE);
            item.setSelectable(false);
            activeWarsPanel.addChild(item);
        } else {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 35,
                    "§7Aucune guerre active"
            );
            item.setSelectable(false);
            activeWarsPanel.addChild(item);
        }
    }

    private void loadWarHistory() {
        historyPanel.clearChildren();

        // Placeholder history
        String[][] history = {
                {"vs Germany", "Victoire", "+15,000 H"},
                {"vs Spain", "Défaite", "-8,000 H"},
                {"vs Italy", "Trêve", "0 H"},
        };

        int itemWidth = historyPanel.getContentWidth() - 5;

        for (String[] data : history) {
            int resultColor = switch (data[1]) {
                case "Victoire" -> HegemoniaColors.SUCCESS;
                case "Défaite" -> HegemoniaColors.ERROR;
                default -> HegemoniaColors.WARNING;
            };

            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 35,
                    data[0], "§7" + data[2]
            );
            item.setRightText(data[1], resultColor);
            item.setSelectable(false);
            historyPanel.addChild(item);
        }
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int buttonY = contentY + 260;

        // Stats content
        int statsY = buttonY + 80;
        int statsX = contentX + 35;

        context.drawText(textRenderer, "§7Guerres gagnées: §a12", statsX, statsY, 0xFFFFFF, false);
        context.drawText(textRenderer, "§7Guerres perdues: §c4", statsX + 150, statsY, 0xFFFFFF, false);
        context.drawText(textRenderer, "§7Ratio: §e75%", statsX + 300, statsY, 0xFFFFFF, false);
    }
}
