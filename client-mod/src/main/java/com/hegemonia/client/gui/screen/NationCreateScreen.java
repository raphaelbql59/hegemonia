package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaTextInput;
import net.minecraft.client.gui.DrawContext;

/**
 * Nation creation screen
 */
public class NationCreateScreen extends HegemoniaScreen {

    private HegemoniaTextInput nameInput;
    private HegemoniaTextInput tagInput;
    private String errorMessage = null;

    private static final int CREATION_COST = 10000;

    public NationCreateScreen() {
        super("Créer une Nation");
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
        int inputWidth = contentWidth - 60;
        int centerX = contentX + contentWidth / 2;

        // Name input
        nameInput = addWidget(new HegemoniaTextInput(
                centerX - inputWidth / 2, contentY + 90,
                inputWidth, 30,
                "Nom de la nation..."
        ));
        nameInput.setMaxLength(32);
        nameInput.setValidator(this::validateName);

        // Tag input
        tagInput = addWidget(new HegemoniaTextInput(
                centerX - inputWidth / 2, contentY + 160,
                inputWidth, 30,
                "Tag (2-4 lettres)..."
        ));
        tagInput.setMaxLength(4);
        tagInput.setValidator(this::validateTag);

        // Create button
        addWidget(new HegemoniaButton(
                centerX - 75, contentY + 230,
                150, 40,
                "✨ Créer",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> createNation()
        ));

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 15, contentY + contentHeight - 45,
                100, 30,
                "← Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openNationMenu()
        ));
    }

    private boolean validateName(String name) {
        if (name.isEmpty()) return true;
        return name.length() >= 3 && name.matches("[a-zA-ZÀ-ÿ0-9\\s_-]+");
    }

    private boolean validateTag(String tag) {
        if (tag.isEmpty()) return true;
        return tag.length() >= 2 && tag.matches("[A-Z]+");
    }

    private void createNation() {
        String name = nameInput.getText().trim();
        String tag = tagInput.getText().trim().toUpperCase();

        // Validate
        if (name.length() < 3) {
            errorMessage = "Le nom doit contenir au moins 3 caractères";
            return;
        }

        if (tag.length() < 2 || tag.length() > 4) {
            errorMessage = "Le tag doit contenir 2 à 4 lettres";
            return;
        }

        if (!tag.matches("[A-Z]+")) {
            errorMessage = "Le tag ne peut contenir que des lettres majuscules";
            return;
        }

        // Check if player has enough money
        if (hegemonia.getPlayerData().getTotalBalance() < CREATION_COST) {
            errorMessage = "Fonds insuffisants (10,000 H requis)";
            return;
        }

        errorMessage = null;
        hegemonia.getNetworkHandler().requestCreateNation(name, tag);
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = contentX + contentWidth / 2;

        // Labels
        context.drawText(textRenderer, "§fNom de la nation",
                contentX + 30, contentY + 75, HegemoniaDesign.TEXT_PRIMARY, true);

        context.drawText(textRenderer, "§fTag (affiché dans le chat)",
                contentX + 30, contentY + 145, HegemoniaDesign.TEXT_PRIMARY, true);

        // Cost info
        context.fill(contentX + 20, contentY + 200, contentX + contentWidth - 20, contentY + 220, HegemoniaDesign.BACKGROUND_LIGHT);
        String costText = "§7Coût de création: §e10,000 H";
        int costWidth = textRenderer.getWidth(costText.replaceAll("§.", ""));
        context.drawText(textRenderer, costText, centerX - costWidth / 2, contentY + 206, 0xFFFFFF, true);

        // Error message
        if (errorMessage != null) {
            int errorWidth = textRenderer.getWidth(errorMessage);
            context.drawText(textRenderer, "§c" + errorMessage,
                    centerX - errorWidth / 2, contentY + 280, HegemoniaDesign.ERROR, true);
        }

        // Preview
        if (!nameInput.getText().isEmpty() && !tagInput.getText().isEmpty()) {
            String preview = "§7Aperçu: §b[" + tagInput.getText().toUpperCase() + "] §f" + nameInput.getText();
            int previewWidth = textRenderer.getWidth(preview.replaceAll("§.", ""));
            context.drawText(textRenderer, preview, centerX - previewWidth / 2, contentY + 55, 0xFFFFFF, true);
        }
    }
}
