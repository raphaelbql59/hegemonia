package com.hegemonia.client.gui.components;

import com.hegemonia.client.gui.theme.HegemoniaTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * Flat, modern button component
 */
public class FlatButton {

    public enum Style {
        PRIMARY,    // Gold accent
        SECONDARY,  // Blue accent
        GHOST,      // Transparent background
        DANGER      // Red accent
    }

    private int x, y, width, height;
    private String text;
    private Style style;
    private Consumer<FlatButton> onClick;
    private boolean enabled = true;
    private boolean visible = true;

    // State
    private boolean hovered = false;
    private boolean pressed = false;
    private float hoverAnim = 0f;

    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public FlatButton(int x, int y, int width, int height, String text, Style style, Consumer<FlatButton> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.style = style;
        this.onClick = onClick;
    }

    public FlatButton(int x, int y, int width, String text, Consumer<FlatButton> onClick) {
        this(x, y, width, HegemoniaTheme.BUTTON_HEIGHT, text, Style.PRIMARY, onClick);
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Update hover state
        hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

        // Animate hover
        float animSpeed = HegemoniaTheme.ANIM_FAST;
        if (hovered && enabled) {
            hoverAnim = Math.min(1f, hoverAnim + animSpeed);
        } else {
            hoverAnim = Math.max(0f, hoverAnim - animSpeed);
        }

        // Get colors based on style
        int bgColor, borderColor, textColor;

        if (!enabled) {
            bgColor = HegemoniaTheme.BG_SECONDARY;
            borderColor = HegemoniaTheme.BORDER_DEFAULT;
            textColor = HegemoniaTheme.TEXT_MUTED;
        } else {
            switch (style) {
                case PRIMARY -> {
                    bgColor = HegemoniaTheme.lerp(HegemoniaTheme.BG_SECONDARY, HegemoniaTheme.ACCENT_GOLD_DARK, hoverAnim * 0.3f);
                    borderColor = HegemoniaTheme.lerp(HegemoniaTheme.ACCENT_GOLD_DARK, HegemoniaTheme.ACCENT_GOLD, hoverAnim);
                    textColor = HegemoniaTheme.lerp(HegemoniaTheme.ACCENT_GOLD, HegemoniaTheme.ACCENT_GOLD_LIGHT, hoverAnim);
                }
                case SECONDARY -> {
                    bgColor = HegemoniaTheme.lerp(HegemoniaTheme.BG_SECONDARY, HegemoniaTheme.ACCENT_BLUE_DARK, hoverAnim * 0.3f);
                    borderColor = HegemoniaTheme.lerp(HegemoniaTheme.ACCENT_BLUE_DARK, HegemoniaTheme.ACCENT_BLUE, hoverAnim);
                    textColor = HegemoniaTheme.lerp(HegemoniaTheme.ACCENT_BLUE, HegemoniaTheme.ACCENT_BLUE_LIGHT, hoverAnim);
                }
                case GHOST -> {
                    bgColor = HegemoniaTheme.withAlpha(HegemoniaTheme.BG_HOVER, (int) (hoverAnim * 180));
                    borderColor = HegemoniaTheme.withAlpha(HegemoniaTheme.BORDER_DEFAULT, (int) (100 + hoverAnim * 100));
                    textColor = HegemoniaTheme.lerp(HegemoniaTheme.TEXT_SECONDARY, HegemoniaTheme.TEXT_PRIMARY, hoverAnim);
                }
                case DANGER -> {
                    bgColor = HegemoniaTheme.lerp(HegemoniaTheme.BG_SECONDARY, HegemoniaTheme.ERROR_DARK, hoverAnim * 0.4f);
                    borderColor = HegemoniaTheme.lerp(HegemoniaTheme.ERROR_DARK, HegemoniaTheme.ERROR, hoverAnim);
                    textColor = HegemoniaTheme.lerp(HegemoniaTheme.ERROR, 0xFFFFAAAA, hoverAnim);
                }
                default -> {
                    bgColor = HegemoniaTheme.BG_SECONDARY;
                    borderColor = HegemoniaTheme.BORDER_DEFAULT;
                    textColor = HegemoniaTheme.TEXT_PRIMARY;
                }
            }
        }

        // Pressed state
        if (pressed && enabled) {
            bgColor = HegemoniaTheme.darken(bgColor, 0.1f);
        }

        // Draw background
        ctx.fill(x, y, x + width, y + height, bgColor);

        // Draw border (1px)
        ctx.fill(x, y, x + width, y + 1, borderColor);           // Top
        ctx.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        ctx.fill(x, y, x + 1, y + height, borderColor);          // Left
        ctx.fill(x + width - 1, y, x + width, y + height, borderColor);  // Right

        // Draw text centered
        int textWidth = textRenderer.getWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;
        ctx.drawText(textRenderer, text, textX, textY, textColor, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;

        if (button == 0 && hovered) {
            pressed = true;
            if (onClick != null) {
                onClick.accept(this);
            }
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        pressed = false;
        return false;
    }

    // Setters
    public FlatButton setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public FlatButton setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public FlatButton setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public FlatButton setText(String text) {
        this.text = text;
        return this;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isEnabled() { return enabled; }
    public boolean isHovered() { return hovered; }
}
