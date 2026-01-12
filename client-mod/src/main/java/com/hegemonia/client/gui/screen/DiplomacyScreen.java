package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaPanel;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import net.minecraft.client.gui.DrawContext;

/**
 * Diplomacy management screen for nation relations
 */
public class DiplomacyScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel relationsPanel;
    private HegemoniaScrollPanel pendingPanel;
    private String selectedNation = null;

    public DiplomacyScreen() {
        super("Diplomatie");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(600, (int) (screenWidth * 0.85));
        contentHeight = Math.min(450, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        int panelWidth = (contentWidth - 45) / 2;

        // Current relations panel (left)
        relationsPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 60,
                panelWidth, 280
        ));
        relationsPanel.setPadding(5).setItemSpacing(3);
        addCurrentRelations();

        // Pending proposals panel (right)
        pendingPanel = addWidget(new HegemoniaScrollPanel(
                contentX + panelWidth + 30, contentY + 60,
                panelWidth, 280
        ));
        pendingPanel.setPadding(5).setItemSpacing(3);
        addPendingProposals();

        // Action buttons
        int buttonY = contentY + contentHeight - 55;
        int buttonWidth = 130;

        // Propose alliance button
        addWidget(new HegemoniaButton(
                contentX + 15, buttonY,
                buttonWidth, 35,
                "Proposer Alliance",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> openProposeDialog("ALLY")
        ));

        // Declare enemy button
        addWidget(new HegemoniaButton(
                contentX + 20 + buttonWidth, buttonY,
                buttonWidth, 35,
                "Declarer Ennemi",
                HegemoniaButton.ButtonStyle.DANGER,
                btn -> openProposeDialog("ENEMY")
        ));

        // Trade partner button
        addWidget(new HegemoniaButton(
                contentX + 25 + buttonWidth * 2, buttonY,
                buttonWidth, 35,
                "Partenaire",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> openProposeDialog("TRADE_PARTNER")
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 115, buttonY,
                100, 35,
                "Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void addCurrentRelations() {
        // Placeholder relations - real data from server
        String[][] relations = {
                {"Empire Romain", "ALLY", "Allie"},
                {"Royaume de France", "TRADE_PARTNER", "Partenaire"},
                {"Horde Mongole", "ENEMY", "Ennemi"},
                {"Republique de Venise", "NEUTRAL", "Neutre"},
                {"Empire Byzantin", "ALLY", "Allie"},
        };

        int itemWidth = relationsPanel.getContentWidth() - 10;

        for (String[] data : relations) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 32,
                    data[0]
            );
            int relationColor = switch (data[1]) {
                case "ALLY" -> HegemoniaColors.SUCCESS;
                case "ENEMY" -> HegemoniaColors.ERROR;
                case "TRADE_PARTNER" -> HegemoniaColors.GOLD;
                default -> HegemoniaColors.TEXT_MUTED;
            };
            String icon = switch (data[1]) {
                case "ALLY" -> "🤝 ";
                case "ENEMY" -> "⚔ ";
                case "TRADE_PARTNER" -> "💰 ";
                default -> "○ ";
            };
            item.setRightText(icon + data[2], relationColor);
            item.setSelectable(true);
            item.setOnClick(() -> selectNation(data[0]));
            relationsPanel.addChild(item);
        }
    }

    private void addPendingProposals() {
        // Placeholder pending - real data from server
        String[][] pending = {
                {"Empire Ottoman", "ALLY", "Recue"},
                {"Royaume d'Angleterre", "TRADE_PARTNER", "Envoyee"},
        };

        int itemWidth = pendingPanel.getContentWidth() - 10;

        if (pending.length == 0) {
            HegemoniaListItem emptyItem = new HegemoniaListItem(
                    0, 0, itemWidth, 30,
                    "Aucune proposition"
            );
            emptyItem.setSelectable(false);
            pendingPanel.addChild(emptyItem);
            return;
        }

        for (String[] data : pending) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 45,
                    data[0]
            );
            String statusIcon = data[2].equals("Recue") ? "📩" : "📤";
            item.setRightText(statusIcon + " " + data[2], HegemoniaColors.WARNING);
            item.setSelectable(true);

            if (data[2].equals("Recue")) {
                // Add accept/decline functionality for received proposals
                item.setOnClick(() -> openResponseDialog(data[0], data[1]));
            }

            pendingPanel.addChild(item);
        }
    }

    private void selectNation(String nationName) {
        selectedNation = nationName;
    }

    private void openProposeDialog(String relationType) {
        // Open nation selection dialog, then send proposal
        hegemonia.getNetworkHandler().requestNationList();
        // For now, use chat command as fallback
        if (client != null && client.player != null) {
            client.player.networkHandler.sendChatCommand("nation relation propose " + relationType);
        }
    }

    private void openResponseDialog(String nationName, String relationType) {
        // Show accept/decline dialog for received proposal
        // For now, accept directly
        if (client != null && client.player != null) {
            client.player.networkHandler.sendChatCommand("nation relation accept " + nationName);
        }
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = (contentWidth - 45) / 2;

        // Section headers
        context.drawText(textRenderer, "Relations actuelles",
                contentX + 20, contentY + 48, HegemoniaColors.TEXT_PRIMARY, false);

        context.drawText(textRenderer, "Propositions en attente",
                contentX + panelWidth + 35, contentY + 48, HegemoniaColors.TEXT_PRIMARY, false);

        // Legend at bottom
        int legendY = contentY + 350;
        int legendX = contentX + 20;
        int spacing = 100;

        context.drawText(textRenderer, "🤝 Allie", legendX, legendY, HegemoniaColors.SUCCESS, false);
        context.drawText(textRenderer, "💰 Partenaire", legendX + spacing, legendY, HegemoniaColors.GOLD, false);
        context.drawText(textRenderer, "⚔ Ennemi", legendX + spacing * 2, legendY, HegemoniaColors.ERROR, false);
        context.drawText(textRenderer, "○ Neutre", legendX + spacing * 3, legendY, HegemoniaColors.TEXT_MUTED, false);
    }
}
