package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaColors;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Base screen class for all Hegemonia screens
 */
public abstract class HegemoniaScreen extends Screen {

    protected final List<HegemoniaWidget> widgets = new ArrayList<>();
    protected final HegemoniaClient hegemonia;

    // Screen dimensions (calculated on init)
    protected int screenWidth;
    protected int screenHeight;

    // Content area (centered panel)
    protected int contentX;
    protected int contentY;
    protected int contentWidth;
    protected int contentHeight;

    // Background blur animation
    protected float openProgress = 0f;

    // Close button
    protected HegemoniaButton closeButton;

    public HegemoniaScreen(String title) {
        super(Text.literal(title));
        this.hegemonia = HegemoniaClient.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        widgets.clear();

        screenWidth = width;
        screenHeight = height;

        // Default content area (can be overridden)
        calculateContentArea();

        // Add close button
        closeButton = new HegemoniaButton(
                contentX + contentWidth - 24, contentY + 4,
                20, 20, "✕",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> close()
        );
        widgets.add(closeButton);

        // Initialize screen content
        initContent();
    }

    /**
     * Calculate the content area dimensions
     * Override to customize
     */
    protected void calculateContentArea() {
        // Default: 70% of screen, centered
        contentWidth = (int) (screenWidth * 0.7);
        contentHeight = (int) (screenHeight * 0.8);
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    /**
     * Initialize screen content
     * Override this to add widgets
     */
    protected abstract void initContent();

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update open animation
        openProgress = Math.min(1f, openProgress + 0.1f);

        // Render dimmed background
        renderBackground(context, mouseX, mouseY, delta);

        // Render main panel with animation
        float scale = 0.9f + 0.1f * openProgress;
        int alpha = (int) (255 * openProgress);

        // Main panel background
        int panelColor = HegemoniaColors.withAlpha(HegemoniaColors.BACKGROUND_DARK, (int) (224 * openProgress));
        context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, panelColor);

        // Panel border with gold accent
        int borderAlpha = (int) (255 * openProgress);
        int borderColor = HegemoniaColors.withAlpha(HegemoniaColors.PANEL_BORDER, borderAlpha);
        int accentColor = HegemoniaColors.withAlpha(HegemoniaColors.ACCENT_GOLD, borderAlpha);

        // Borders
        context.fill(contentX, contentY, contentX + contentWidth, contentY + 1, accentColor); // Top (gold)
        context.fill(contentX, contentY + contentHeight - 1, contentX + contentWidth, contentY + contentHeight, borderColor);
        context.fill(contentX, contentY, contentX + 1, contentY + contentHeight, borderColor);
        context.fill(contentX + contentWidth - 1, contentY, contentX + contentWidth, contentY + contentHeight, borderColor);

        // Render header
        renderHeader(context, mouseX, mouseY, delta);

        // Render all widgets
        for (HegemoniaWidget widget : widgets) {
            widget.render(context, mouseX, mouseY, delta);
        }

        // Render screen-specific content
        renderContent(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Semi-transparent dark overlay
        int bgAlpha = (int) (180 * openProgress);
        context.fill(0, 0, screenWidth, screenHeight, HegemoniaColors.withAlpha(0x000000, bgAlpha));
    }

    /**
     * Render the screen header
     */
    protected void renderHeader(DrawContext context, int mouseX, int mouseY, float delta) {
        // Header background
        context.fill(contentX, contentY, contentX + contentWidth, contentY + 30, HegemoniaColors.PANEL_HEADER);

        // Title
        String titleText = getTitle().getString();
        context.drawText(textRenderer, titleText, contentX + 12, contentY + 10, HegemoniaColors.TEXT_TITLE, true);

        // Header bottom border
        context.fill(contentX, contentY + 29, contentX + contentWidth, contentY + 30, HegemoniaColors.PANEL_BORDER);
    }

    /**
     * Render screen-specific content
     * Override this to add custom rendering
     */
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override in subclasses
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check widgets in reverse order (top to bottom)
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Click outside content area closes screen
        if (button == 0 && !isInContentArea(mouseX, mouseY)) {
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

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (HegemoniaWidget widget : widgets) {
            if (widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HegemoniaWidget widget : widgets) {
            if (widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Escape closes screen
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        // Let widgets handle key presses
        for (HegemoniaWidget widget : widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (HegemoniaWidget widget : widgets) {
            if (widget.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    /**
     * Check if point is within content area
     */
    protected boolean isInContentArea(double x, double y) {
        return x >= contentX && x < contentX + contentWidth &&
                y >= contentY && y < contentY + contentHeight;
    }

    /**
     * Add a widget to the screen
     */
    protected <T extends HegemoniaWidget> T addWidget(T widget) {
        widgets.add(widget);
        return widget;
    }

    /**
     * Navigate back to main menu
     */
    protected void goBack() {
        hegemonia.getScreenManager().openMainMenu();
    }

    /**
     * Navigate to another screen
     */
    protected void navigateTo(HegemoniaScreen screen) {
        if (client != null) {
            client.setScreen(screen);
        }
    }

    /**
     * Clear widgets and reinitialize the screen
     */
    protected void clearAndInit() {
        widgets.clear();
        init();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
