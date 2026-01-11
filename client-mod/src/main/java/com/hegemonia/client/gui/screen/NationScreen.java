package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaPanel;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import net.minecraft.client.gui.DrawContext;

/**
 * Nation overview screen for players who are in a nation
 */
public class NationScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel membersPanel;

    public NationScreen() {
        super("Nation");
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

        // Info panel (left side)
        int infoPanelWidth = 200;
        HegemoniaPanel infoPanel = addWidget(new HegemoniaPanel(
                contentX + 15, contentY + 45,
                infoPanelWidth, 150,
                "Informations"
        ));

        // Members panel (right side)
        membersPanel = addWidget(new HegemoniaScrollPanel(
                contentX + infoPanelWidth + 25, contentY + 45,
                contentWidth - infoPanelWidth - 40, 200
        ));
        membersPanel.setPadding(5).setItemSpacing(2);

        // Add placeholder members
        addPlaceholderMembers();

        // Action buttons
        int buttonY = contentY + 260;
        int buttonWidth = 150;

        // Territoire button
        addWidget(new HegemoniaButton(
                contentX + 15, buttonY,
                buttonWidth, 35,
                "🗺 Territoire",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> {} // TODO: Territory screen
        ));

        // Diplomatie button
        addWidget(new HegemoniaButton(
                contentX + 20 + buttonWidth, buttonY,
                buttonWidth, 35,
                "🤝 Diplomatie",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> {} // TODO: Diplomacy screen
        ));

        // Trésor button
        addWidget(new HegemoniaButton(
                contentX + 25 + buttonWidth * 2, buttonY,
                buttonWidth, 35,
                "💰 Trésor",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> {} // TODO: Treasury screen
        ));

        // Leave nation button (bottom, danger)
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 165, contentY + contentHeight - 50,
                150, 30,
                "🚪 Quitter la nation",
                HegemoniaButton.ButtonStyle.DANGER,
                btn -> {
                    // TODO: Confirmation dialog
                    hegemonia.getNetworkHandler().requestLeaveNation();
                }
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

    private void addPlaceholderMembers() {
        // Placeholder - real data from server
        String[][] members = {
                {"👑", "LeaderName", "Chef", "En ligne"},
                {"⚔", "Officer1", "Officier", "Il y a 2h"},
                {"⚔", "Officer2", "Officier", "En ligne"},
                {"🛡", "Member1", "Membre", "Il y a 1j"},
                {"🛡", "Member2", "Membre", "En ligne"},
                {"🛡", "Member3", "Membre", "Il y a 3h"},
        };

        int itemWidth = membersPanel.getContentWidth() - 10;

        for (String[] data : members) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 30,
                    data[0] + " " + data[1]
            );
            item.setRightText(data[3], data[3].equals("En ligne") ? HegemoniaColors.SUCCESS : HegemoniaColors.TEXT_MUTED);
            item.setSelectable(false);
            membersPanel.addChild(item);
        }
    }

    @Override
    protected void renderHeader(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Custom header with nation name
        int headerHeight = 35;
        context.fill(contentX, contentY, contentX + contentWidth, contentY + headerHeight, HegemoniaColors.PANEL_HEADER);

        // Nation tag and name
        String nationDisplay = "§b[" + data.nationTag + "] §f" + data.nationName;
        context.drawText(textRenderer, nationDisplay, contentX + 15, contentY + 12, HegemoniaColors.TEXT_PRIMARY, true);

        // Role badge
        String roleColor = switch (data.nationRole) {
            case "LEADER" -> "§6";
            case "OFFICER" -> "§c";
            default -> "§7";
        };
        String roleText = roleColor + data.nationRole;
        int roleWidth = textRenderer.getWidth(data.nationRole);
        context.drawText(textRenderer, roleText, contentX + contentWidth - roleWidth - 50, contentY + 12, 0xFFFFFF, true);

        // Header border
        context.fill(contentX, contentY + headerHeight - 1, contentX + contentWidth, contentY + headerHeight, HegemoniaColors.ACCENT_BLUE);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int infoPanelWidth = 200;

        // Info panel content
        int infoX = contentX + 25;
        int infoY = contentY + 80;
        int lineHeight = 16;

        context.drawText(textRenderer, "§7Membres: §f12/50", infoX, infoY, 0xFFFFFF, false);
        infoY += lineHeight;

        context.drawText(textRenderer, "§7Chunks: §f45/100", infoX, infoY, 0xFFFFFF, false);
        infoY += lineHeight;

        context.drawText(textRenderer, "§7Trésor: §e125,000 H", infoX, infoY, 0xFFFFFF, false);
        infoY += lineHeight;

        context.drawText(textRenderer, "§7Fondée: §f12 Jan 2024", infoX, infoY, 0xFFFFFF, false);
        infoY += lineHeight;

        // War status
        if (data.atWar) {
            context.drawText(textRenderer, "§c⚔ EN GUERRE", infoX, infoY + 10, HegemoniaColors.WAR_ACTIVE, true);
        } else {
            context.drawText(textRenderer, "§a☮ En paix", infoX, infoY + 10, HegemoniaColors.WAR_PEACE, true);
        }

        // Members header
        context.drawText(textRenderer, "§7Membres (12)",
                contentX + infoPanelWidth + 30, contentY + 35, HegemoniaColors.TEXT_MUTED, false);
    }
}
