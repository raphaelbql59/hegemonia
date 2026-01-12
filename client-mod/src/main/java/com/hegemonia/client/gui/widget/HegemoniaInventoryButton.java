package com.hegemonia.client.gui.widget;

import com.hegemonia.client.gui.theme.HegemoniaColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

/**
 * Custom Hegemonia button for inventory screen
 * Extends PressableWidget to be compatible with Minecraft's screen system
 */
public class HegemoniaInventoryButton extends PressableWidget {

    private final PressAction onPress;
    private float hoverProgress = 0f;
    private float pulsePhase = 0f;

    public HegemoniaInventoryButton(int x, int y, int width, int height, PressAction onPress) {
        super(x, y, width, height, Text.empty());
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        if (onPress != null) {
            onPress.onPress(this);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animations
        pulsePhase += delta * 0.05f;
        if (isHovered()) {
            hoverProgress = Math.min(1f, hoverProgress + 0.15f);
        } else {
            hoverProgress = Math.max(0f, hoverProgress - 0.1f);
        }

        int x = getX();
        int y = getY();

        // Subtle pulse glow when not hovered
        float pulse = (float) (Math.sin(pulsePhase) * 0.5 + 0.5) * 0.3f;
        int glowAlpha = (int) ((pulse + hoverProgress * 0.7f) * 80);

        // Background with rounded corners effect (simulated)
        int bgColor = HegemoniaColors.withAlpha(0x1a1a24, 230);
        int borderColor = HegemoniaColors.lerp(
                HegemoniaColors.withAlpha(HegemoniaColors.ACCENT_GOLD, 100),
                HegemoniaColors.ACCENT_GOLD,
                hoverProgress
        );

        // Glow effect
        if (glowAlpha > 0) {
            int glowColor = HegemoniaColors.withAlpha(HegemoniaColors.ACCENT_GOLD, glowAlpha);
            context.fill(x - 2, y - 2, x + width + 2, y + height + 2, glowColor);
        }

        // Main background
        context.fill(x, y, x + width, y + height, bgColor);

        // Border
        context.fill(x, y, x + width, y + 1, borderColor); // Top
        context.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        context.fill(x, y, x + 1, y + height, borderColor); // Left
        context.fill(x + width - 1, y, x + width, y + height, borderColor); // Right

        // Crown/Logo icon (simplified H symbol)
        int iconColor = HegemoniaColors.lerp(HegemoniaColors.ACCENT_GOLD, 0xFFFFD700, hoverProgress);

        // Draw stylized "H" for Hegemonia
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int iconSize = 8;

        // Left vertical bar
        context.fill(centerX - 5, centerY - iconSize, centerX - 3, centerY + iconSize, iconColor);
        // Right vertical bar
        context.fill(centerX + 3, centerY - iconSize, centerX + 5, centerY + iconSize, iconColor);
        // Horizontal bar
        context.fill(centerX - 5, centerY - 1, centerX + 5, centerY + 1, iconColor);

        // Crown points on top
        context.fill(centerX - 5, centerY - iconSize - 2, centerX - 3, centerY - iconSize, iconColor);
        context.fill(centerX - 1, centerY - iconSize - 3, centerX + 1, centerY - iconSize, iconColor);
        context.fill(centerX + 3, centerY - iconSize - 2, centerX + 5, centerY - iconSize, iconColor);

        // Hover highlight
        if (hoverProgress > 0) {
            int highlightAlpha = (int) (40 * hoverProgress);
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1,
                    HegemoniaColors.withAlpha(0xFFFFFF, highlightAlpha));
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @FunctionalInterface
    public interface PressAction {
        void onPress(HegemoniaInventoryButton button);
    }
}
