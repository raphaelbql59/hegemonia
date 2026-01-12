package com.hegemonia.client.gui.widget;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * A list item widget for use in scroll panels
 */
public class HegemoniaListItem extends AbstractWidget {

    private String title;
    private String subtitle;
    private String rightText;
    private int rightTextColor = HegemoniaDesign.TEXT_SECONDARY;

    private String icon;
    private int iconColor = HegemoniaDesign.ACCENT_GOLD;

    private boolean selected = false;
    private boolean selectable = true;
    private Consumer<HegemoniaListItem> onClick;

    private int backgroundColor = HegemoniaDesign.BACKGROUND_MEDIUM;
    private int hoverColor = HegemoniaDesign.BACKGROUND_LIGHT;
    private int selectedColor = HegemoniaDesign.withAlpha(HegemoniaDesign.ACCENT_GOLD, 40);

    public HegemoniaListItem(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        this.title = title;
    }

    public HegemoniaListItem(int x, int y, int width, int height, String title, String subtitle) {
        super(x, y, width, height);
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        int bgColor = selected ? selectedColor : (hovered ? hoverColor : backgroundColor);
        context.fill(x, y, x + width, y + height, bgColor);

        // Left accent for selected
        if (selected) {
            context.fill(x, y, x + 3, y + height, HegemoniaDesign.ACCENT_GOLD);
        }

        // Content positioning
        int contentX = x + (selected ? 10 : 8);
        int contentY = y + 4;

        // Icon
        if (icon != null && !icon.isEmpty()) {
            drawText(context, icon, contentX, contentY + (height - 8) / 2 - 4, iconColor);
            contentX += 16;
        }

        // Title and subtitle
        if (subtitle != null && !subtitle.isEmpty()) {
            // Two lines
            drawText(context, title, contentX, y + 4, HegemoniaDesign.TEXT_PRIMARY);
            drawText(context, subtitle, contentX, y + 16, HegemoniaDesign.TEXT_SECONDARY);
        } else {
            // Single line centered
            drawText(context, title, contentX, y + (height - 8) / 2, HegemoniaDesign.TEXT_PRIMARY);
        }

        // Right text
        if (rightText != null && !rightText.isEmpty()) {
            int rightWidth = textRenderer.getWidth(rightText);
            drawText(context, rightText, x + width - rightWidth - 8, y + (height - 8) / 2, rightTextColor);
        }

        // Bottom border
        context.fill(x + 4, y + height - 1, x + width - 4, y + height, HegemoniaDesign.withAlpha(HegemoniaDesign.PANEL_BORDER, 100));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;

        if (isMouseOver(mouseX, mouseY) && button == 0) {
            if (selectable) {
                selected = true;
            }
            if (onClick != null) {
                onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    // ==================== Setters ====================

    public HegemoniaListItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public HegemoniaListItem setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public HegemoniaListItem setRightText(String text) {
        this.rightText = text;
        return this;
    }

    public HegemoniaListItem setRightText(String text, int color) {
        this.rightText = text;
        this.rightTextColor = color;
        return this;
    }

    public HegemoniaListItem setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public HegemoniaListItem setIcon(String icon, int color) {
        this.icon = icon;
        this.iconColor = color;
        return this;
    }

    public HegemoniaListItem setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public HegemoniaListItem setSelectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    public HegemoniaListItem setOnClick(Consumer<HegemoniaListItem> onClick) {
        this.onClick = onClick;
        return this;
    }

    /**
     * Convenience method to set onClick with a simple Runnable
     */
    public HegemoniaListItem setOnClick(Runnable onClick) {
        this.onClick = item -> onClick.run();
        return this;
    }

    public HegemoniaListItem setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
