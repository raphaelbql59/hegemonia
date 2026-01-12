package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaPanel;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import net.minecraft.client.gui.DrawContext;

/**
 * Territory management screen for nation chunks
 */
public class TerritoryScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel chunksPanel;
    private int totalChunks = 45;
    private int maxChunks = 100;

    public TerritoryScreen() {
        super("Territoire");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(500, (int) (screenWidth * 0.8));
        contentHeight = Math.min(400, (int) (screenHeight * 0.75));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        // Stats panel
        HegemoniaPanel statsPanel = addWidget(new HegemoniaPanel(
                contentX + 15, contentY + 45,
                contentWidth - 30, 60,
                "Statistiques"
        ));

        // Chunks list
        chunksPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 15, contentY + 120,
                contentWidth - 30, 180
        ));
        chunksPanel.setPadding(5).setItemSpacing(3);

        addPlaceholderChunks();

        // Action buttons
        int buttonY = contentY + contentHeight - 60;
        int buttonWidth = 140;

        // Claim chunk button
        addWidget(new HegemoniaButton(
                contentX + 15, buttonY,
                buttonWidth, 35,
                "+ Revendiquer",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> claimCurrentChunk()
        ));

        // Unclaim button
        addWidget(new HegemoniaButton(
                contentX + 20 + buttonWidth, buttonY,
                buttonWidth, 35,
                "- Abandonner",
                HegemoniaButton.ButtonStyle.DANGER,
                btn -> unclaimCurrentChunk()
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

    private void addPlaceholderChunks() {
        // Placeholder chunks - real data from server
        String[][] chunks = {
                {"0, 0", "Capitale", "Protege"},
                {"1, 0", "Zone Est", "Normal"},
                {"0, 1", "Zone Nord", "Normal"},
                {"-1, 0", "Zone Ouest", "Frontiere"},
                {"0, -1", "Zone Sud", "Normal"},
                {"2, 0", "Extension Est", "Frontiere"},
        };

        int itemWidth = chunksPanel.getContentWidth() - 10;

        for (String[] data : chunks) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 28,
                    "Chunk [" + data[0] + "] - " + data[1]
            );
            int statusColor = switch (data[2]) {
                case "Protege" -> HegemoniaDesign.SUCCESS;
                case "Frontiere" -> HegemoniaDesign.WARNING;
                default -> HegemoniaDesign.TEXT_MUTED;
            };
            item.setRightText(data[2], statusColor);
            item.setSelectable(true);
            chunksPanel.addChild(item);
        }
    }

    private void claimCurrentChunk() {
        if (totalChunks < maxChunks) {
            hegemonia.getNetworkHandler().requestClaimChunk();
        }
    }

    private void unclaimCurrentChunk() {
        hegemonia.getNetworkHandler().requestUnclaimChunk();
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Stats content
        int statsY = contentY + 65;
        int col1 = contentX + 30;
        int col2 = contentX + 180;
        int col3 = contentX + 330;

        // Chunks owned
        context.drawText(textRenderer, "Chunks:", col1, statsY, HegemoniaDesign.TEXT_MUTED, false);
        String chunksText = totalChunks + "/" + maxChunks;
        int chunksColor = totalChunks >= maxChunks ? HegemoniaDesign.ERROR : HegemoniaDesign.TEXT_PRIMARY;
        context.drawText(textRenderer, chunksText, col1 + 50, statsY, chunksColor, false);

        // Claim cost
        int claimCost = 100 + (totalChunks * 10);
        context.drawText(textRenderer, "Cout:", col2, statsY, HegemoniaDesign.TEXT_MUTED, false);
        context.drawText(textRenderer, claimCost + " H", col2 + 40, statsY, HegemoniaDesign.GOLD, false);

        // Upkeep
        int upkeep = totalChunks * 5;
        context.drawText(textRenderer, "Entretien:", col3, statsY, HegemoniaDesign.TEXT_MUTED, false);
        context.drawText(textRenderer, upkeep + " H/jour", col3 + 60, statsY, HegemoniaDesign.WARNING, false);

        // Section header
        context.drawText(textRenderer, "Chunks revendiques (" + totalChunks + ")",
                contentX + 20, contentY + 108, HegemoniaDesign.TEXT_MUTED, false);
    }
}
