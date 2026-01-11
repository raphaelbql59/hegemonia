package com.hegemonia.client.gui.widget;

import net.minecraft.client.gui.DrawContext;

/**
 * Base interface for all Hegemonia widgets
 */
public interface HegemoniaWidget {

    /**
     * Render the widget
     */
    void render(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     * Handle mouse click
     * @return true if the click was handled
     */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    /**
     * Handle mouse release
     */
    boolean mouseReleased(double mouseX, double mouseY, int button);

    /**
     * Handle mouse drag
     */
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    /**
     * Handle mouse scroll
     */
    boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);

    /**
     * Handle key press
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * Handle character typed
     */
    boolean charTyped(char chr, int modifiers);

    /**
     * Check if point is within widget bounds
     */
    boolean isMouseOver(double mouseX, double mouseY);

    /**
     * Get widget x position
     */
    int getX();

    /**
     * Get widget y position
     */
    int getY();

    /**
     * Get widget width
     */
    int getWidth();

    /**
     * Get widget height
     */
    int getHeight();

    /**
     * Set widget position
     */
    void setPosition(int x, int y);

    /**
     * Set widget visibility
     */
    void setVisible(boolean visible);

    /**
     * Check if widget is visible
     */
    boolean isVisible();

    /**
     * Set widget enabled state
     */
    void setEnabled(boolean enabled);

    /**
     * Check if widget is enabled
     */
    boolean isEnabled();
}
