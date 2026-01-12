package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * HEGEMONIA BASE SCREEN
 * Ultra-modern base class for all Hegemonia screens
 * Uses the unified HegemoniaDesign system
 */
public abstract class HegemoniaScreen extends Screen {

    protected final HegemoniaClient hegemonia;
    protected final List<HegemoniaWidget> widgets = new ArrayList<>();

    // Screen dimensions
    protected int screenWidth;
    protected int screenHeight;

    // Content panel dimensions
    protected int panelX, panelY, panelWidth, panelHeight;
    protected int contentX, contentY, contentWidth, contentHeight;

    // Animation
    protected float openAnim = 0f;
    protected boolean closeButtonHovered = false;

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

        // Calculate panel dimensions (can be overridden)
        calculatePanelSize();
        // Backward compatibility
        calculateContentArea();

        // Content area inside panel
        contentX = panelX + HegemoniaDesign.SPACE_LG;
        contentY = panelY + HegemoniaDesign.HEADER_HEIGHT + HegemoniaDesign.SPACE_MD;
        contentWidth = panelWidth - HegemoniaDesign.SPACE_LG * 2;
        contentHeight = panelHeight - HegemoniaDesign.HEADER_HEIGHT - HegemoniaDesign.FOOTER_HEIGHT - HegemoniaDesign.SPACE_MD * 2;

        // Initialize subclass content
        initContent();
    }

    /**
     * Calculate the panel dimensions
     * Override to customize panel size
     */
    protected void calculatePanelSize() {
        panelWidth = Math.min(500, (int)(screenWidth * 0.8));
        panelHeight = Math.min(400, (int)(screenHeight * 0.8));
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
    }

    /**
     * Legacy method for backward compatibility
     * Override this OR calculatePanelSize()
     */
    protected void calculateContentArea() {
        // Override in subclasses if needed
    }

    /**
     * Initialize screen content
     * Override this in subclasses
     */
    protected abstract void initContent();

    /**
     * Add a widget to the screen
     */
    protected <T extends HegemoniaWidget> T addWidget(T widget) {
        widgets.add(widget);
        return widget;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Update animation
        openAnim = Math.min(1f, openAnim + HegemoniaDesign.ANIM_SPEED_FAST);
        float eased = HegemoniaDesign.easeOut(openAnim);

        int alpha = (int)(255 * eased);

        // ═══════════════════════════════════════════════════════════════
        // OVERLAY BACKGROUND
        // ═══════════════════════════════════════════════════════════════
        int overlayAlpha = (int)(230 * eased);
        ctx.fill(0, 0, screenWidth, screenHeight, HegemoniaDesign.withAlpha(0x000000, overlayAlpha));

        // ═══════════════════════════════════════════════════════════════
        // MAIN PANEL
        // ═══════════════════════════════════════════════════════════════
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PANEL, alpha));

        // Panel border with gold accent
        HegemoniaDesign.drawPanelBorder(ctx, panelX, panelY, panelWidth, panelHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha),
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));

        // ═══════════════════════════════════════════════════════════════
        // HEADER
        // ═══════════════════════════════════════════════════════════════
        renderHeader(ctx, mouseX, mouseY, eased);

        // ═══════════════════════════════════════════════════════════════
        // CONTENT (subclass)
        // ═══════════════════════════════════════════════════════════════
        renderContent(ctx, mouseX, mouseY, delta);

        // ═══════════════════════════════════════════════════════════════
        // WIDGETS
        // ═══════════════════════════════════════════════════════════════
        for (HegemoniaWidget widget : widgets) {
            widget.render(ctx, mouseX, mouseY, delta);
        }

        // ═══════════════════════════════════════════════════════════════
        // FOOTER
        // ═══════════════════════════════════════════════════════════════
        renderFooter(ctx, mouseX, mouseY, eased);
    }

    /**
     * Render the header with title, back button, and close button
     */
    protected void renderHeader(DrawContext ctx, int mouseX, int mouseY, float anim) {
        int alpha = (int)(255 * anim);

        // Header background
        ctx.fill(panelX + 1, panelY + 2, panelX + panelWidth - 1, panelY + HegemoniaDesign.HEADER_HEIGHT,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PANEL_HEADER, alpha));

        // Header divider
        ctx.fill(panelX + 1, panelY + HegemoniaDesign.HEADER_HEIGHT - 1,
                panelX + panelWidth - 1, panelY + HegemoniaDesign.HEADER_HEIGHT,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_SUBTLE, alpha));

        // Back button (if not main menu)
        int backBtnX = panelX + HegemoniaDesign.SPACE_SM;
        int backBtnY = panelY + (HegemoniaDesign.HEADER_HEIGHT - 24) / 2;
        boolean backHovered = mouseX >= backBtnX && mouseX < backBtnX + 24 &&
                mouseY >= backBtnY && mouseY < backBtnY + 24;

        int backBg = backHovered ? HegemoniaDesign.BG_BUTTON_HOVER : 0;
        if (backBg != 0) {
            ctx.fill(backBtnX, backBtnY, backBtnX + 24, backBtnY + 24,
                    HegemoniaDesign.withAlpha(backBg, alpha));
        }
        ctx.drawText(textRenderer, "<",
                backBtnX + 8, backBtnY + 8,
                HegemoniaDesign.withAlpha(backHovered ? HegemoniaDesign.TEXT_PRIMARY : HegemoniaDesign.TEXT_MUTED, alpha), false);

        // Title
        String titleText = getTitle().getString();
        int titleX = backBtnX + 32;
        ctx.drawText(textRenderer, titleText,
                titleX, panelY + (HegemoniaDesign.HEADER_HEIGHT - 8) / 2,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), true);

        // Close button
        int closeBtnX = panelX + panelWidth - 32;
        int closeBtnY = panelY + (HegemoniaDesign.HEADER_HEIGHT - 24) / 2;
        closeButtonHovered = mouseX >= closeBtnX && mouseX < closeBtnX + 24 &&
                mouseY >= closeBtnY && mouseY < closeBtnY + 24;

        int closeBg = closeButtonHovered ? HegemoniaDesign.withAlpha(HegemoniaDesign.ERROR, 60) : 0;
        if (closeBg != 0) {
            ctx.fill(closeBtnX, closeBtnY, closeBtnX + 24, closeBtnY + 24, closeBg);
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "X",
                closeBtnX + 12, closeBtnY + 8,
                closeButtonHovered ? HegemoniaDesign.ERROR_LIGHT : HegemoniaDesign.TEXT_MUTED);
    }

    /**
     * Render screen content
     * Override in subclasses
     */
    protected void renderContent(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Override in subclasses
    }

    /**
     * Render the footer
     */
    protected void renderFooter(DrawContext ctx, int mouseX, int mouseY, float anim) {
        int alpha = (int)(255 * anim);
        int footerY = panelY + panelHeight - HegemoniaDesign.FOOTER_HEIGHT;

        // Footer divider
        ctx.fill(panelX + 1, footerY, panelX + panelWidth - 1, footerY + 1,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_SUBTLE, alpha));

        // Version
        String version = "v1.0.0";
        ctx.drawText(textRenderer, version,
                panelX + panelWidth - textRenderer.getWidth(version) - HegemoniaDesign.SPACE_MD,
                footerY + (HegemoniaDesign.FOOTER_HEIGHT - 8) / 2,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check widgets first
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Back button
        int backBtnX = panelX + HegemoniaDesign.SPACE_SM;
        int backBtnY = panelY + (HegemoniaDesign.HEADER_HEIGHT - 24) / 2;
        if (mouseX >= backBtnX && mouseX < backBtnX + 24 &&
                mouseY >= backBtnY && mouseY < backBtnY + 24) {
            goBack();
            return true;
        }

        // Close button - returns to inventory
        int closeBtnX = panelX + panelWidth - 32;
        int closeBtnY = panelY + (HegemoniaDesign.HEADER_HEIGHT - 24) / 2;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + 24 &&
                mouseY >= closeBtnY && mouseY < closeBtnY + 24) {
            goBack();
            return true;
        }

        // Click outside panel - returns to inventory
        if (mouseX < panelX || mouseX >= panelX + panelWidth ||
                mouseY < panelY || mouseY >= panelY + panelHeight) {
            goBack();
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            goBack();
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
     * Navigate back - returns to inventory screen
     * Category screens are opened from inventory, so we return there
     */
    protected void goBack() {
        if (client != null && client.player != null) {
            // Return to inventory
            close();
            client.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(client.player));
        } else {
            close();
        }
    }

    /**
     * Navigate to another screen
     */
    protected void navigateTo(HegemoniaScreen screen) {
        if (client != null) {
            client.setScreen(screen);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
