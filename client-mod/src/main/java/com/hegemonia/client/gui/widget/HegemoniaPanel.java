package com.hegemonia.client.gui.widget;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A container panel that can hold other widgets
 */
public class HegemoniaPanel extends AbstractWidget {

    private String title;
    private boolean showTitle = true;
    private boolean showBorder = true;
    private boolean showBackground = true;

    private int titleHeight = 25;
    private int padding = 8;

    private int backgroundColor = HegemoniaDesign.PANEL_BACKGROUND;
    private int headerColor = HegemoniaDesign.PANEL_HEADER;
    private int borderColor = HegemoniaDesign.PANEL_BORDER;
    private int titleColor = HegemoniaDesign.TEXT_TITLE;

    private final List<HegemoniaWidget> children = new ArrayList<>();

    public HegemoniaPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public HegemoniaPanel(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        this.title = title;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background
        if (showBackground) {
            context.fill(x, y, x + width, y + height, backgroundColor);
        }

        // Draw header if title is set
        if (showTitle && title != null && !title.isEmpty()) {
            context.fill(x, y, x + width, y + titleHeight, headerColor);

            // Title text
            drawText(context, title, x + padding, y + (titleHeight - 8) / 2, titleColor);

            // Header bottom border
            context.fill(x, y + titleHeight - 1, x + width, y + titleHeight, borderColor);
        }

        // Draw border
        if (showBorder) {
            context.fill(x, y, x + width, y + 1, borderColor); // Top
            context.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
            context.fill(x, y, x + 1, y + height, borderColor); // Left
            context.fill(x + width - 1, y, x + width, y + height, borderColor); // Right
        }

        // Render children
        for (HegemoniaWidget child : children) {
            child.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Check children in reverse order (top to bottom)
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (HegemoniaWidget child : children) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (HegemoniaWidget child : children) {
            if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HegemoniaWidget child : children) {
            if (child.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (HegemoniaWidget child : children) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (HegemoniaWidget child : children) {
            if (child.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Child Management ====================

    public HegemoniaPanel addChild(HegemoniaWidget widget) {
        children.add(widget);
        return this;
    }

    public HegemoniaPanel removeChild(HegemoniaWidget widget) {
        children.remove(widget);
        return this;
    }

    public void clearChildren() {
        children.clear();
    }

    public List<HegemoniaWidget> getChildren() {
        return children;
    }

    /**
     * Get the content area bounds (inside padding and title)
     */
    public int getContentX() {
        return x + padding;
    }

    public int getContentY() {
        return y + (showTitle && title != null ? titleHeight : 0) + padding;
    }

    public int getContentWidth() {
        return width - (padding * 2);
    }

    public int getContentHeight() {
        int headerOffset = showTitle && title != null ? titleHeight : 0;
        return height - headerOffset - (padding * 2);
    }

    // ==================== Setters ====================

    public HegemoniaPanel setTitle(String title) {
        this.title = title;
        return this;
    }

    public HegemoniaPanel setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
        return this;
    }

    public HegemoniaPanel setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
        return this;
    }

    public HegemoniaPanel setShowBackground(boolean showBackground) {
        this.showBackground = showBackground;
        return this;
    }

    public HegemoniaPanel setTitleHeight(int titleHeight) {
        this.titleHeight = titleHeight;
        return this;
    }

    public HegemoniaPanel setPadding(int padding) {
        this.padding = padding;
        return this;
    }

    public HegemoniaPanel setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public HegemoniaPanel setHeaderColor(int color) {
        this.headerColor = color;
        return this;
    }

    public HegemoniaPanel setBorderColor(int color) {
        this.borderColor = color;
        return this;
    }

    public HegemoniaPanel setTitleColor(int color) {
        this.titleColor = color;
        return this;
    }
}
