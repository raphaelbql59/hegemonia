package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable confirmation dialog for dangerous actions
 */
public class ConfirmationDialog extends Screen {

    private final List<HegemoniaWidget> widgets = new ArrayList<>();
    private final Screen parent;
    private final String title;
    private final String message;
    private final String confirmText;
    private final Runnable onConfirm;
    private final boolean isDangerous;

    private int dialogWidth;
    private int dialogHeight;
    private int dialogX;
    private int dialogY;

    public ConfirmationDialog(Screen parent, String title, String message, String confirmText, Runnable onConfirm, boolean isDangerous) {
        super(Text.of(title));
        this.parent = parent;
        this.title = title;
        this.message = message;
        this.confirmText = confirmText;
        this.onConfirm = onConfirm;
        this.isDangerous = isDangerous;
    }

    public static ConfirmationDialog danger(Screen parent, String title, String message, String confirmText, Runnable onConfirm) {
        return new ConfirmationDialog(parent, title, message, confirmText, onConfirm, true);
    }

    public static ConfirmationDialog confirm(Screen parent, String title, String message, String confirmText, Runnable onConfirm) {
        return new ConfirmationDialog(parent, title, message, confirmText, onConfirm, false);
    }

    @Override
    protected void init() {
        widgets.clear();

        dialogWidth = 300;
        dialogHeight = 150;
        dialogX = (width - dialogWidth) / 2;
        dialogY = (height - dialogHeight) / 2;

        int buttonWidth = 100;
        int buttonSpacing = 20;
        int buttonsY = dialogY + dialogHeight - 45;

        // Cancel button
        widgets.add(new HegemoniaButton(
                dialogX + (dialogWidth / 2) - buttonWidth - (buttonSpacing / 2),
                buttonsY,
                buttonWidth, 30,
                "Annuler",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> close()
        ));

        // Confirm button
        widgets.add(new HegemoniaButton(
                dialogX + (dialogWidth / 2) + (buttonSpacing / 2),
                buttonsY,
                buttonWidth, 30,
                confirmText,
                isDangerous ? HegemoniaButton.ButtonStyle.DANGER : HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> {
                    onConfirm.run();
                    close();
                }
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Darken background
        context.fill(0, 0, width, height, 0xAA000000);

        // Dialog background
        context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, HegemoniaDesign.BACKGROUND_DARK);

        // Dialog border
        int borderColor = isDangerous ? HegemoniaDesign.ERROR : HegemoniaDesign.ACCENT_BLUE;
        context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + 2, borderColor);
        context.fill(dialogX, dialogY + dialogHeight - 1, dialogX + dialogWidth, dialogY + dialogHeight, HegemoniaDesign.PANEL_BORDER);
        context.fill(dialogX, dialogY, dialogX + 1, dialogY + dialogHeight, HegemoniaDesign.PANEL_BORDER);
        context.fill(dialogX + dialogWidth - 1, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, HegemoniaDesign.PANEL_BORDER);

        // Header
        context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + 35, HegemoniaDesign.PANEL_HEADER);
        context.fill(dialogX, dialogY + 34, dialogX + dialogWidth, dialogY + 35, HegemoniaDesign.PANEL_BORDER);

        // Title
        String icon = isDangerous ? "⚠ " : "? ";
        int titleColor = isDangerous ? HegemoniaDesign.ERROR : HegemoniaDesign.TEXT_PRIMARY;
        context.drawText(textRenderer, icon + title, dialogX + 15, dialogY + 12, titleColor, true);

        // Message
        int messageY = dialogY + 50;
        String[] lines = message.split("\n");
        for (String line : lines) {
            context.drawCenteredTextWithShadow(textRenderer, line, dialogX + dialogWidth / 2, messageY, HegemoniaDesign.TEXT_SECONDARY);
            messageY += 14;
        }

        // Render widgets
        for (HegemoniaWidget widget : widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Click outside dialog closes it
        if (button == 0 && !isInDialog(mouseX, mouseY)) {
            close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (HegemoniaWidget widget : widgets) {
            widget.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isInDialog(double x, double y) {
        return x >= dialogX && x < dialogX + dialogWidth &&
                y >= dialogY && y < dialogY + dialogHeight;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
