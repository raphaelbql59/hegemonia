package com.hegemonia.client.gui.widget;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable panel that can contain more content than its visible area
 */
public class HegemoniaScrollPanel extends AbstractWidget {

    private final List<HegemoniaWidget> children = new ArrayList<>();

    private int contentHeight = 0;
    private double scrollOffset = 0;
    private double targetScrollOffset = 0;
    private boolean smoothScrolling = true;

    private int scrollbarWidth = 6;
    private boolean showScrollbar = true;
    private boolean draggingScrollbar = false;
    private double dragStartY = 0;
    private double dragStartOffset = 0;

    private int padding = 4;
    private int itemSpacing = 4;

    public HegemoniaScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update smooth scrolling
        if (smoothScrolling) {
            scrollOffset = MathHelper.lerp(0.3, scrollOffset, targetScrollOffset);
        } else {
            scrollOffset = targetScrollOffset;
        }

        // Draw background
        context.fill(x, y, x + width, y + height, HegemoniaDesign.BACKGROUND_DARK);

        // Enable scissor for content clipping
        context.enableScissor(x, y, x + width - (showScrollbar && needsScrollbar() ? scrollbarWidth : 0), y + height);

        // Render children with scroll offset
        int currentY = y + padding - (int) scrollOffset;
        for (HegemoniaWidget child : children) {
            if (currentY + child.getHeight() >= y && currentY <= y + height) {
                // Temporarily move child for rendering
                int originalY = child.getY();
                child.setPosition(child.getX(), currentY);
                child.render(context, mouseX, mouseY, delta);
                child.setPosition(child.getX(), originalY);
            }
            currentY += child.getHeight() + itemSpacing;
        }

        context.disableScissor();

        // Draw scrollbar
        if (showScrollbar && needsScrollbar()) {
            renderScrollbar(context, mouseX, mouseY);
        }

        // Draw border
        context.fill(x, y, x + width, y + 1, HegemoniaDesign.PANEL_BORDER);
        context.fill(x, y + height - 1, x + width, y + height, HegemoniaDesign.PANEL_BORDER);
        context.fill(x, y, x + 1, y + height, HegemoniaDesign.PANEL_BORDER);
        context.fill(x + width - 1, y, x + width, y + height, HegemoniaDesign.PANEL_BORDER);
    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int scrollbarX = x + width - scrollbarWidth;

        // Track
        context.fill(scrollbarX, y, x + width, y + height, HegemoniaDesign.SCROLLBAR_TRACK);

        // Thumb
        int thumbHeight = Math.max(20, (int) ((float) height / contentHeight * height));
        int maxScroll = getMaxScroll();
        int thumbY = y + (maxScroll > 0 ? (int) (scrollOffset / maxScroll * (height - thumbHeight)) : 0);

        boolean thumbHovered = mouseX >= scrollbarX && mouseX < x + width &&
                mouseY >= thumbY && mouseY < thumbY + thumbHeight;

        int thumbColor = (draggingScrollbar || thumbHovered) ?
                HegemoniaDesign.SCROLLBAR_THUMB_HOVER : HegemoniaDesign.SCROLLBAR_THUMB;

        context.fill(scrollbarX + 1, thumbY, x + width - 1, thumbY + thumbHeight, thumbColor);
    }

    private boolean needsScrollbar() {
        return contentHeight > height;
    }

    private int getMaxScroll() {
        return Math.max(0, contentHeight - height + padding * 2);
    }

    public void recalculateContentHeight() {
        contentHeight = padding;
        for (HegemoniaWidget child : children) {
            contentHeight += child.getHeight() + itemSpacing;
        }
        contentHeight -= itemSpacing; // Remove last spacing
        contentHeight += padding;

        // Clamp scroll
        targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0, getMaxScroll());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Check scrollbar click
        if (showScrollbar && needsScrollbar()) {
            int scrollbarX = x + width - scrollbarWidth;
            if (mouseX >= scrollbarX && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                draggingScrollbar = true;
                dragStartY = mouseY;
                dragStartOffset = scrollOffset;
                return true;
            }
        }

        // Check if within content area
        if (isMouseOver(mouseX, mouseY)) {
            // Adjust mouseY for scroll offset when checking children
            double adjustedMouseY = mouseY + scrollOffset - y - padding + y;

            for (int i = children.size() - 1; i >= 0; i--) {
                HegemoniaWidget child = children.get(i);
                int childY = y + padding - (int) scrollOffset;
                for (int j = 0; j < i; j++) {
                    childY += children.get(j).getHeight() + itemSpacing;
                }

                if (mouseY >= Math.max(y, childY) && mouseY < Math.min(y + height, childY + child.getHeight())) {
                    if (child.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;

        for (HegemoniaWidget child : children) {
            child.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int thumbHeight = Math.max(20, (int) ((float) height / contentHeight * height));
            int trackHeight = height - thumbHeight;
            int maxScroll = getMaxScroll();

            double dragDelta = mouseY - dragStartY;
            double scrollDelta = (dragDelta / trackHeight) * maxScroll;

            targetScrollOffset = MathHelper.clamp(dragStartOffset + scrollDelta, 0, maxScroll);
            if (!smoothScrolling) scrollOffset = targetScrollOffset;
            return true;
        }

        for (HegemoniaWidget child : children) {
            if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        int scrollAmount = 30;
        targetScrollOffset = MathHelper.clamp(
                targetScrollOffset - verticalAmount * scrollAmount,
                0, getMaxScroll()
        );

        if (!smoothScrolling) scrollOffset = targetScrollOffset;
        return true;
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

    public HegemoniaScrollPanel addChild(HegemoniaWidget widget) {
        // Position child within the scroll panel
        widget.setPosition(x + padding, 0); // Y is calculated during render
        children.add(widget);
        recalculateContentHeight();
        return this;
    }

    public HegemoniaScrollPanel removeChild(HegemoniaWidget widget) {
        children.remove(widget);
        recalculateContentHeight();
        return this;
    }

    public void clearChildren() {
        children.clear();
        contentHeight = 0;
        scrollOffset = 0;
        targetScrollOffset = 0;
    }

    public List<HegemoniaWidget> getChildren() {
        return children;
    }

    public int getChildCount() {
        return children.size();
    }

    public void scrollToTop() {
        targetScrollOffset = 0;
        if (!smoothScrolling) scrollOffset = 0;
    }

    public void scrollToBottom() {
        targetScrollOffset = getMaxScroll();
        if (!smoothScrolling) scrollOffset = targetScrollOffset;
    }

    // ==================== Setters ====================

    public HegemoniaScrollPanel setScrollbarWidth(int width) {
        this.scrollbarWidth = width;
        return this;
    }

    public HegemoniaScrollPanel setShowScrollbar(boolean show) {
        this.showScrollbar = show;
        return this;
    }

    public HegemoniaScrollPanel setSmoothScrolling(boolean smooth) {
        this.smoothScrolling = smooth;
        return this;
    }

    public HegemoniaScrollPanel setPadding(int padding) {
        this.padding = padding;
        recalculateContentHeight();
        return this;
    }

    public HegemoniaScrollPanel setItemSpacing(int spacing) {
        this.itemSpacing = spacing;
        recalculateContentHeight();
        return this;
    }

    public int getContentWidth() {
        return width - padding * 2 - (showScrollbar && needsScrollbar() ? scrollbarWidth : 0);
    }
}
