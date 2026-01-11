package com.hegemonia.client.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Abstract base class for all Hegemonia widgets
 */
public abstract class AbstractWidget implements HegemoniaWidget {

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean focused = false;
    protected boolean hovered = false;

    protected final MinecraftClient client;
    protected final TextRenderer textRenderer;

    public AbstractWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        hovered = isMouseOver(mouseX, mouseY);
        renderWidget(context, mouseX, mouseY, delta);
    }

    /**
     * Render the widget implementation
     */
    protected abstract void renderWidget(DrawContext context, int mouseX, int mouseY, float delta);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isHovered() {
        return hovered;
    }

    // ==================== Utility Methods ====================

    /**
     * Draw centered text
     */
    protected void drawCenteredText(DrawContext context, String text, int centerX, int y, int color) {
        int textWidth = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, centerX - textWidth / 2, y, color, true);
    }

    /**
     * Draw text with shadow
     */
    protected void drawText(DrawContext context, String text, int x, int y, int color) {
        context.drawText(textRenderer, text, x, y, color, true);
    }

    /**
     * Draw a rectangle with border
     */
    protected void drawRectWithBorder(DrawContext context, int x, int y, int width, int height,
                                       int fillColor, int borderColor) {
        // Fill
        context.fill(x, y, x + width, y + height, fillColor);
        // Border
        context.fill(x, y, x + width, y + 1, borderColor); // Top
        context.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        context.fill(x, y, x + 1, y + height, borderColor); // Left
        context.fill(x + width - 1, y, x + width, y + height, borderColor); // Right
    }

    /**
     * Draw a horizontal gradient
     */
    protected void drawHorizontalGradient(DrawContext context, int x, int y, int width, int height,
                                           int colorStart, int colorEnd) {
        context.fillGradient(x, y, x + width, y + height, colorStart, colorEnd);
    }
}
