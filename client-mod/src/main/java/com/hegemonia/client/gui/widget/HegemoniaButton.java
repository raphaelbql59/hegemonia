package com.hegemonia.client.gui.widget;

import com.hegemonia.client.gui.theme.HegemoniaColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.function.Consumer;

/**
 * Custom styled button for Hegemonia GUIs
 */
public class HegemoniaButton extends AbstractWidget {

    public enum ButtonStyle {
        DEFAULT,
        PRIMARY,
        DANGER,
        GHOST
    }

    private String text;
    private String icon;
    private ButtonStyle style;
    private Consumer<HegemoniaButton> onClick;
    private boolean pressed = false;

    // Animation
    private float hoverProgress = 0f;
    private float pressProgress = 0f;

    public HegemoniaButton(int x, int y, int width, int height, String text) {
        this(x, y, width, height, text, ButtonStyle.DEFAULT, null);
    }

    public HegemoniaButton(int x, int y, int width, int height, String text, ButtonStyle style) {
        this(x, y, width, height, text, style, null);
    }

    public HegemoniaButton(int x, int y, int width, int height, String text, ButtonStyle style, Consumer<HegemoniaButton> onClick) {
        super(x, y, width, height);
        this.text = text;
        this.style = style;
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animations
        updateAnimations(delta);

        // Determine colors based on style and state
        int bgColor = getBackgroundColor();
        int borderColor = getBorderColor();
        int textColor = getTextColor();

        // Draw button background with gradient effect
        context.fill(x, y, x + width, y + height, bgColor);

        // Draw border
        int borderAlpha = (int) (255 * (0.5f + hoverProgress * 0.5f));
        int animatedBorder = HegemoniaColors.withAlpha(borderColor, borderAlpha);
        context.fill(x, y, x + width, y + 1, animatedBorder); // Top
        context.fill(x, y + height - 1, x + width, y + height, animatedBorder); // Bottom
        context.fill(x, y, x + 1, y + height, animatedBorder); // Left
        context.fill(x + width - 1, y, x + width, y + height, animatedBorder); // Right

        // Draw hover glow effect
        if (hoverProgress > 0 && enabled) {
            int glowAlpha = (int) (30 * hoverProgress);
            int glowColor = HegemoniaColors.withAlpha(borderColor, glowAlpha);
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, glowColor);
        }

        // Draw icon if present
        int textX = x + width / 2;
        int textY = y + (height - 8) / 2;

        if (icon != null && !icon.isEmpty()) {
            String fullText = icon + " " + text;
            drawCenteredText(context, fullText, textX, textY, textColor);
        } else {
            drawCenteredText(context, text, textX, textY, textColor);
        }

        // Draw disabled overlay
        if (!enabled) {
            context.fill(x, y, x + width, y + height, 0x80000000);
        }
    }

    private void updateAnimations(float delta) {
        float speed = 0.15f;

        // Hover animation
        if (hovered && enabled) {
            hoverProgress = Math.min(1f, hoverProgress + speed);
        } else {
            hoverProgress = Math.max(0f, hoverProgress - speed);
        }

        // Press animation
        if (pressed) {
            pressProgress = Math.min(1f, pressProgress + speed * 2);
        } else {
            pressProgress = Math.max(0f, pressProgress - speed);
        }
    }

    private int getBackgroundColor() {
        if (!enabled) return HegemoniaColors.BUTTON_DISABLED;

        return switch (style) {
            case PRIMARY -> pressed ? HegemoniaColors.BUTTON_PRESSED :
                    (hovered ? HegemoniaColors.BUTTON_PRIMARY_HOVER : HegemoniaColors.BUTTON_PRIMARY);
            case DANGER -> pressed ? HegemoniaColors.BUTTON_PRESSED :
                    (hovered ? HegemoniaColors.BUTTON_DANGER_HOVER : HegemoniaColors.BUTTON_DANGER);
            case GHOST -> hovered ? HegemoniaColors.withAlpha(HegemoniaColors.BUTTON_HOVER, 128) : 0x00000000;
            default -> pressed ? HegemoniaColors.BUTTON_PRESSED :
                    (hovered ? HegemoniaColors.BUTTON_HOVER : HegemoniaColors.BUTTON_DEFAULT);
        };
    }

    private int getBorderColor() {
        if (!enabled) return HegemoniaColors.BUTTON_BORDER;

        return switch (style) {
            case PRIMARY -> HegemoniaColors.BUTTON_PRIMARY_BORDER;
            case DANGER -> HegemoniaColors.BUTTON_DANGER_BORDER;
            case GHOST -> hovered ? HegemoniaColors.ACCENT_GOLD : 0x00000000;
            default -> hovered ? HegemoniaColors.BUTTON_BORDER_HOVER : HegemoniaColors.BUTTON_BORDER;
        };
    }

    private int getTextColor() {
        if (!enabled) return HegemoniaColors.TEXT_MUTED;
        return HegemoniaColors.TEXT_PRIMARY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;

        if (isMouseOver(mouseX, mouseY) && button == 0) {
            pressed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressed && button == 0) {
            pressed = false;
            if (isMouseOver(mouseX, mouseY) && enabled) {
                playClickSound();
                if (onClick != null) {
                    onClick.accept(this);
                }
                return true;
            }
        }
        return false;
    }

    private void playClickSound() {
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    // ==================== Setters ====================

    public HegemoniaButton setText(String text) {
        this.text = text;
        return this;
    }

    public HegemoniaButton setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public HegemoniaButton setStyle(ButtonStyle style) {
        this.style = style;
        return this;
    }

    public HegemoniaButton setOnClick(Consumer<HegemoniaButton> onClick) {
        this.onClick = onClick;
        return this;
    }

    public String getText() {
        return text;
    }
}
