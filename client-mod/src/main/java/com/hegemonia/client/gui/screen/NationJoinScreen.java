package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import com.hegemonia.client.gui.widget.HegemoniaTextInput;
import com.hegemonia.client.gui.widget.HegemoniaWidget;
import net.minecraft.client.gui.DrawContext;

/**
 * Nation join/browse screen for players without a nation
 */
public class NationJoinScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel nationsPanel;
    private HegemoniaTextInput searchInput;
    private HegemoniaListItem selectedNation;

    public NationJoinScreen() {
        super("Nations");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(500, (int) (screenWidth * 0.8));
        contentHeight = Math.min(400, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        // Search input
        searchInput = addWidget(new HegemoniaTextInput(
                contentX + 15, contentY + 45,
                contentWidth - 30, 25,
                "Rechercher une nation..."
        ));
        searchInput.setOnTextChanged(text -> filterNations(text));

        // Nations list
        nationsPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 80,
                contentWidth - 30, contentHeight - 170
        ));
        nationsPanel.setPadding(5).setItemSpacing(4);

        // Load nations
        loadNations();

        // Create nation button
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 180, contentY + contentHeight - 75,
                165, 35,
                "✨ Créer une nation",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> hegemonia.getScreenManager().openNationCreateScreen()
        ));

        // Join button (disabled until selection)
        HegemoniaButton joinButton = addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 75,
                150, 35,
                "🚀 Rejoindre",
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> joinSelectedNation()
        ));
        joinButton.setEnabled(false);

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 35,
                100, 25,
                "← Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> goBack()
        ));
    }

    private void loadNations() {
        nationsPanel.clearChildren();

        // Placeholder nations - real data from server
        String[][] nations = {
                {"France", "FR", "15/50", "Ouverte"},
                {"Germany", "DE", "23/50", "Sur invitation"},
                {"United Kingdom", "UK", "8/30", "Ouverte"},
                {"Spain", "ES", "45/50", "Sur invitation"},
                {"Italy", "IT", "12/40", "Ouverte"},
                {"Poland", "PL", "5/20", "Ouverte"},
                {"Netherlands", "NL", "18/30", "Sur invitation"},
        };

        int itemWidth = nationsPanel.getContentWidth() - 10;

        for (String[] data : nations) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 45,
                    "§b[" + data[1] + "] §f" + data[0],
                    "§7Membres: " + data[2] + " | " + data[3]
            );

            int statusColor = data[3].equals("Ouverte") ? HegemoniaDesign.SUCCESS : HegemoniaDesign.WARNING;
            item.setRightText(data[3], statusColor);

            item.setOnClick(btn -> selectNation(item));
            nationsPanel.addChild(item);
        }
    }

    private void filterNations(String query) {
        // TODO: Filter nations based on search query
    }

    private void selectNation(HegemoniaListItem item) {
        // Deselect previous
        if (selectedNation != null) {
            selectedNation.setSelected(false);
        }

        // Select new
        selectedNation = item;
        item.setSelected(true);

        // Enable join button
        for (HegemoniaWidget widget : widgets) {
            if (widget instanceof HegemoniaButton btn && btn.getText().contains("Rejoindre")) {
                btn.setEnabled(true);
            }
        }
    }

    private void joinSelectedNation() {
        if (selectedNation == null) return;

        // TODO: Extract nation ID from selection
        // hegemonia.getNetworkHandler().requestJoinNation(nationId);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Info text at bottom
        String infoText = selectedNation == null ?
                "Sélectionnez une nation pour la rejoindre" :
                "Cliquez sur 'Rejoindre' pour envoyer une demande";

        int infoWidth = textRenderer.getWidth(infoText);
        context.drawText(textRenderer, "§7" + infoText,
                contentX + (contentWidth - infoWidth) / 2,
                contentY + contentHeight - 30,
                HegemoniaDesign.TEXT_MUTED, false);
    }
}
