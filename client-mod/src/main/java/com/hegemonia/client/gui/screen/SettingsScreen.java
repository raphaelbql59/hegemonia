package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import net.minecraft.client.gui.DrawContext;

/**
 * Hegemonia settings screen
 */
public class SettingsScreen extends HegemoniaScreen {

    private boolean hudEnabled = true;
    private boolean notificationsEnabled = true;
    private boolean soundsEnabled = true;

    public SettingsScreen() {
        super("Paramètres");
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
        int buttonWidth = contentWidth - 60;
        int buttonY = contentY + 60;
        int buttonHeight = 35;
        int spacing = 10;

        // HUD toggle
        addWidget(new HegemoniaButton(
                contentX + 30, buttonY,
                buttonWidth, buttonHeight,
                getToggleText("Afficher le HUD", hudEnabled),
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> {
                    hudEnabled = !hudEnabled;
                    btn.setText(getToggleText("Afficher le HUD", hudEnabled));
                    hegemonia.getHud().setVisible(hudEnabled);
                }
        ));
        buttonY += buttonHeight + spacing;

        // Notifications toggle
        addWidget(new HegemoniaButton(
                contentX + 30, buttonY,
                buttonWidth, buttonHeight,
                getToggleText("Notifications", notificationsEnabled),
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> {
                    notificationsEnabled = !notificationsEnabled;
                    btn.setText(getToggleText("Notifications", notificationsEnabled));
                }
        ));
        buttonY += buttonHeight + spacing;

        // Sounds toggle
        addWidget(new HegemoniaButton(
                contentX + 30, buttonY,
                buttonWidth, buttonHeight,
                getToggleText("Sons", soundsEnabled),
                HegemoniaButton.ButtonStyle.DEFAULT,
                btn -> {
                    soundsEnabled = !soundsEnabled;
                    btn.setText(getToggleText("Sons", soundsEnabled));
                }
        ));
        buttonY += buttonHeight + spacing + 20;

        // Reset settings
        addWidget(new HegemoniaButton(
                contentX + 30, buttonY,
                buttonWidth, buttonHeight,
                "⟲ Réinitialiser",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> resetSettings()
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

    private String getToggleText(String label, boolean enabled) {
        return label + (enabled ? " §a[ON]" : " §c[OFF]");
    }

    private void resetSettings() {
        hudEnabled = true;
        notificationsEnabled = true;
        soundsEnabled = true;

        hegemonia.getHud().setVisible(true);

        // Refresh buttons
        init();
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Info text
        String infoText = "§7Appuyez sur F6 pour toggler le HUD rapidement";
        int infoWidth = textRenderer.getWidth(infoText.replaceAll("§.", ""));
        context.drawText(textRenderer, infoText,
                contentX + (contentWidth - infoWidth) / 2,
                contentY + contentHeight - 80,
                HegemoniaDesign.TEXT_MUTED, false);
    }
}
