package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import com.hegemonia.client.gui.widget.HegemoniaTextInput;
import net.minecraft.client.gui.DrawContext;

/**
 * War declaration screen - select target nation and war goal
 */
public class WarDeclarationScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel nationsPanel;
    private HegemoniaTextInput searchInput;
    private String selectedNation = null;
    private String selectedWarGoal = "CONQUEST";

    private static final String[][] WAR_GOALS = {
            {"CONQUEST", "Conquete", "Annexer des territoires"},
            {"TRADE", "Commerce", "Forcer un accord commercial"},
            {"HUMILIATION", "Humiliation", "Affaiblir l'ennemi"},
            {"LIBERATION", "Liberation", "Liberer un vassal"},
    };

    public WarDeclarationScreen() {
        super("Declaration de Guerre");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(500, (int) (screenWidth * 0.8));
        contentHeight = Math.min(420, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        // Search input
        searchInput = addWidget(new HegemoniaTextInput(
                contentX + 15, contentY + 45,
                contentWidth - 30, 28,
                "Rechercher une nation..."
        ));

        // Nations list
        nationsPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 80,
                (contentWidth - 40) / 2, 180
        ));
        nationsPanel.setPadding(5).setItemSpacing(3);
        loadNations();

        // War goals panel
        int goalsX = contentX + (contentWidth / 2) + 5;
        int goalsY = contentY + 80;
        int goalWidth = (contentWidth - 40) / 2;

        for (int i = 0; i < WAR_GOALS.length; i++) {
            final String goalId = WAR_GOALS[i][0];
            final String goalName = WAR_GOALS[i][1];

            HegemoniaButton goalBtn = addWidget(new HegemoniaButton(
                    goalsX, goalsY + (i * 45),
                    goalWidth, 40,
                    goalName,
                    goalId.equals(selectedWarGoal) ? HegemoniaButton.ButtonStyle.PRIMARY : HegemoniaButton.ButtonStyle.DEFAULT,
                    btn -> selectWarGoal(goalId)
            ));
        }

        // Declare button
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 180, contentY + contentHeight - 55,
                165, 40,
                "Declarer la Guerre",
                HegemoniaButton.ButtonStyle.DANGER,
                btn -> declareWar()
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 55,
                100, 40,
                "Annuler",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void loadNations() {
        nationsPanel.clearChildren();

        // Placeholder nations - real data from server
        String[][] nations = {
                {"Empire Romain", "ROM", "NEUTRAL"},
                {"Royaume de France", "FRA", "TRADE_PARTNER"},
                {"Horde Mongole", "MON", "ENEMY"},
                {"Empire Ottoman", "OTT", "NEUTRAL"},
                {"Royaume d'Angleterre", "ENG", "NEUTRAL"},
                {"Republique de Venise", "VEN", "ALLY"},
        };

        int itemWidth = nationsPanel.getContentWidth() - 10;

        for (String[] data : nations) {
            // Can't declare war on allies
            if (data[2].equals("ALLY")) continue;

            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 35,
                    "[" + data[1] + "] " + data[0]
            );

            int relationColor = switch (data[2]) {
                case "ENEMY" -> HegemoniaColors.ERROR;
                case "TRADE_PARTNER" -> HegemoniaColors.GOLD;
                default -> HegemoniaColors.TEXT_MUTED;
            };
            item.setRightText(data[2], relationColor);
            item.setSelectable(true);
            item.setOnClick(() -> selectNation(data[0]));

            if (data[0].equals(selectedNation)) {
                item.setSelected(true);
            }

            nationsPanel.addChild(item);
        }
    }

    private void selectNation(String nationName) {
        selectedNation = nationName;
        loadNations(); // Refresh to show selection
    }

    private void selectWarGoal(String goalId) {
        selectedWarGoal = goalId;
        clearAndInit(); // Refresh buttons
    }

    private void declareWar() {
        if (selectedNation != null && selectedWarGoal != null) {
            if (client != null) {
                client.setScreen(ConfirmationDialog.danger(
                        this,
                        "Confirmer Declaration",
                        "Declarer la guerre a " + selectedNation + "?\nObjectif: " + getWarGoalName(selectedWarGoal) + "\n\nCette action est irreversible!",
                        "Declarer",
                        () -> {
                            hegemonia.getNetworkHandler().requestDeclareWar(selectedNation, selectedWarGoal);
                            goBack();
                        }
                ));
            }
        }
    }

    private String getWarGoalName(String goalId) {
        for (String[] goal : WAR_GOALS) {
            if (goal[0].equals(goalId)) return goal[1];
        }
        return goalId;
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Section labels
        context.drawText(textRenderer, "Cible",
                contentX + 20, contentY + 68, HegemoniaColors.TEXT_MUTED, false);

        context.drawText(textRenderer, "Objectif de guerre",
                contentX + (contentWidth / 2) + 10, contentY + 68, HegemoniaColors.TEXT_MUTED, false);

        // Selected info at bottom
        int infoY = contentY + 275;
        if (selectedNation != null) {
            context.drawText(textRenderer, "Cible: " + selectedNation,
                    contentX + 20, infoY, HegemoniaColors.TEXT_PRIMARY, false);
        } else {
            context.drawText(textRenderer, "Selectionnez une nation cible",
                    contentX + 20, infoY, HegemoniaColors.WARNING, false);
        }

        // War goal description
        for (String[] goal : WAR_GOALS) {
            if (goal[0].equals(selectedWarGoal)) {
                context.drawText(textRenderer, goal[2],
                        contentX + 20, infoY + 18, HegemoniaColors.TEXT_MUTED, false);
                break;
            }
        }
    }
}
