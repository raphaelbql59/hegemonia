package com.hegemonia.client.gui.components;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaTheme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Base screen with consistent styling
 */
public abstract class BaseScreen extends Screen {

    protected final HegemoniaClient hegemonia;
    protected final List<FlatButton> buttons = new ArrayList<>();

    // Layout
    protected int panelX, panelY, panelWidth, panelHeight;
    protected String title;

    public BaseScreen(String title) {
        super(Text.of(title));
        this.title = title;
        this.hegemonia = HegemoniaClient.getInstance();
    }

    @Override
    protected void init() {
        buttons.clear();
        calculateLayout();
        initComponents();
    }

    /**
     * Calculate panel dimensions - override for custom sizing
     */
    protected void calculateLayout() {
        panelWidth = Math.min(400, (int) (width * 0.8));
        panelHeight = Math.min(320, (int) (height * 0.75));
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    /**
     * Initialize UI components - implement in subclasses
     */
    protected abstract void initComponents();

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dark overlay
        ctx.fill(0, 0, width, height, HegemoniaTheme.withAlpha(HegemoniaTheme.BG_DARK, 200));

        // Main panel background
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, HegemoniaTheme.BG_PRIMARY);

        // Panel border
        drawBorder(ctx, panelX, panelY, panelWidth, panelHeight, HegemoniaTheme.BORDER_DEFAULT);

        // Header
        renderHeader(ctx);

        // Content
        renderContent(ctx, mouseX, mouseY, delta);

        // Render buttons
        for (FlatButton button : buttons) {
            button.render(ctx, mouseX, mouseY, delta);
        }
    }

    protected void renderHeader(DrawContext ctx) {
        int headerHeight = HegemoniaTheme.HEADER_HEIGHT;

        // Header background
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + headerHeight, HegemoniaTheme.BG_SECONDARY);

        // Header bottom border
        ctx.fill(panelX, panelY + headerHeight - 1, panelX + panelWidth, panelY + headerHeight, HegemoniaTheme.BORDER_DEFAULT);

        // Gold accent line at top
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + 2, HegemoniaTheme.ACCENT_GOLD);

        // Title
        ctx.drawText(textRenderer, title, panelX + HegemoniaTheme.SPACE_MD, panelY + (headerHeight - 8) / 2, HegemoniaTheme.TEXT_PRIMARY, false);
    }

    /**
     * Render content area - implement in subclasses
     */
    protected abstract void renderContent(DrawContext ctx, int mouseX, int mouseY, float delta);

    /**
     * Get content area Y start (after header)
     */
    protected int getContentY() {
        return panelY + HegemoniaTheme.HEADER_HEIGHT + HegemoniaTheme.SPACE_MD;
    }

    /**
     * Draw a 1px border
     */
    protected void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);           // Top
        ctx.fill(x, y + h - 1, x + w, y + h, color);   // Bottom
        ctx.fill(x, y, x + 1, y + h, color);           // Left
        ctx.fill(x + w - 1, y, x + w, y + h, color);   // Right
    }

    /**
     * Add a button and return it
     */
    protected FlatButton addButton(FlatButton button) {
        buttons.add(button);
        return button;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (FlatButton btn : buttons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (FlatButton btn : buttons) {
            btn.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(null);
        }
    }

    /**
     * Navigate to another screen
     */
    protected void navigateTo(Screen screen) {
        if (client != null) {
            client.setScreen(screen);
        }
    }

    /**
     * Go back (close this screen)
     */
    protected void goBack() {
        close();
    }
}
