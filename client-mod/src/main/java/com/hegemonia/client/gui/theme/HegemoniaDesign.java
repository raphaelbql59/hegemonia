package com.hegemonia.client.gui.theme;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * HEGEMONIA DESIGN SYSTEM
 * Ultra-modern, Nations Glory inspired design
 *
 * Based on an 8px grid with a dark, professional color palette
 */
public final class HegemoniaDesign {

    private HegemoniaDesign() {}

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXTURES
    // ═══════════════════════════════════════════════════════════════════════════
    public static final Identifier LOGO = Identifier.of("hegemonia", "textures/gui/logo.png");
    public static final Identifier LOGO_LARGE = Identifier.of("hegemonia", "textures/gui/logo_large.png");
    public static final Identifier LOGO_TRANSPARENT = Identifier.of("hegemonia", "textures/gui/logo_transparent.png");

    // ═══════════════════════════════════════════════════════════════════════════
    // SPACING (8px grid)
    // ═══════════════════════════════════════════════════════════════════════════
    public static final int SPACE_XXS = 2;
    public static final int SPACE_XS = 4;
    public static final int SPACE_SM = 8;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_XL = 32;
    public static final int SPACE_XXL = 48;
    public static final int SPACE_HUGE = 64;

    // ═══════════════════════════════════════════════════════════════════════════
    // COLORS - DARK THEME (Nations Glory inspired)
    // ═══════════════════════════════════════════════════════════════════════════

    // Backgrounds
    public static final int BG_OVERLAY = 0xE6000000;        // 90% opacity black overlay
    public static final int BG_PANEL = 0xFF0D0D12;          // Main panel background
    public static final int BG_PANEL_HEADER = 0xFF131318;   // Header background
    public static final int BG_CARD = 0xFF15151C;           // Card background
    public static final int BG_CARD_HOVER = 0xFF1A1A24;     // Card hover
    public static final int BG_INPUT = 0xFF0A0A0E;          // Input field background
    public static final int BG_BUTTON = 0xFF1A1A24;         // Button background
    public static final int BG_BUTTON_HOVER = 0xFF242430;   // Button hover

    // Borders
    public static final int BORDER_DEFAULT = 0xFF2A2A36;    // Default border
    public static final int BORDER_SUBTLE = 0xFF1E1E28;     // Subtle border
    public static final int BORDER_FOCUS = 0xFF3A3A4A;      // Focus border

    // Primary Accent - Gold
    public static final int GOLD = 0xFFD4A634;              // Primary gold
    public static final int GOLD_LIGHT = 0xFFE8C04A;        // Light gold (hover)
    public static final int GOLD_DARK = 0xFFB08A28;         // Dark gold
    public static final int GOLD_MUTED = 0x80D4A634;        // 50% opacity gold
    public static final int GOLD_GLOW = 0x33D4A634;         // 20% opacity gold (glow effect)

    // Secondary Accent - Blue
    public static final int BLUE = 0xFF4A9FD4;              // Secondary blue
    public static final int BLUE_LIGHT = 0xFF6BB8E8;        // Light blue
    public static final int BLUE_MUTED = 0x804A9FD4;        // 50% opacity blue

    // Status Colors
    public static final int SUCCESS = 0xFF34C759;           // Success green
    public static final int SUCCESS_MUTED = 0x8034C759;     // Muted success
    public static final int WARNING = 0xFFFFCC00;           // Warning yellow
    public static final int ERROR = 0xFFE53935;             // Error red
    public static final int ERROR_LIGHT = 0xFFFF5252;       // Light error
    public static final int INFO = 0xFF5B9BD5;              // Info blue

    // Text Colors
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;      // Primary text (white)
    public static final int TEXT_SECONDARY = 0xFFB8B8C8;    // Secondary text
    public static final int TEXT_MUTED = 0xFF6B6B7B;        // Muted text
    public static final int TEXT_DISABLED = 0xFF444454;     // Disabled text
    public static final int TEXT_TITLE = 0xFFD4A634;        // Title text (gold)

    // ═══════════════════════════════════════════════════════════════════════════
    // LEGACY COMPATIBILITY ALIASES
    // ═══════════════════════════════════════════════════════════════════════════
    public static final int ACCENT_GOLD = GOLD;
    public static final int ACCENT_GOLD_LIGHT = GOLD_LIGHT;
    public static final int ACCENT_BLUE = BLUE;

    // Button colors
    public static final int BUTTON_DEFAULT = BG_BUTTON;
    public static final int BUTTON_HOVER = BG_BUTTON_HOVER;
    public static final int BUTTON_PRESSED = 0xFF101018;
    public static final int BUTTON_DISABLED = 0xFF1A1A22;
    public static final int BUTTON_PRIMARY = GOLD;
    public static final int BUTTON_PRIMARY_HOVER = GOLD_LIGHT;
    public static final int BUTTON_PRIMARY_BORDER = GOLD_DARK;
    public static final int BUTTON_DANGER = ERROR;
    public static final int BUTTON_DANGER_HOVER = ERROR_LIGHT;
    public static final int BUTTON_DANGER_BORDER = 0xFFB02A2A;
    public static final int BUTTON_BORDER = BORDER_DEFAULT;

    // Panel/Background aliases
    public static final int PANEL_HEADER = BG_PANEL_HEADER;
    public static final int PANEL_BORDER = BORDER_DEFAULT;
    public static final int BACKGROUND_DARK = BG_PANEL;
    public static final int BACKGROUND_LIGHT = BG_CARD;

    // Legacy color aliases
    public static final int BG_SECONDARY = BG_CARD;
    public static final int BG_HOVER = BG_CARD_HOVER;
    public static final int MONEY_NEUTRAL = GOLD;
    public static final int MONEY_POSITIVE = SUCCESS;
    public static final int MONEY_NEGATIVE = ERROR;

    // Scrollbar
    public static final int SCROLLBAR_BG = 0xFF1A1A22;
    public static final int SCROLLBAR_THUMB = 0xFF3A3A4A;
    public static final int SCROLLBAR_THUMB_HOVER = 0xFF4A4A5A;
    public static final int SCROLLBAR_TRACK = SCROLLBAR_BG;

    // Input
    public static final int INPUT_BG = BG_INPUT;
    public static final int INPUT_BORDER = BORDER_DEFAULT;
    public static final int INPUT_BORDER_FOCUS = GOLD;
    public static final int INPUT_BACKGROUND = BG_INPUT;
    public static final int INPUT_TEXT = TEXT_PRIMARY;
    public static final int INPUT_PLACEHOLDER = TEXT_MUTED;

    // Additional compatibility
    public static final int BACKGROUND_MEDIUM = 0xFF181820;
    public static final int PANEL_BACKGROUND = BG_PANEL;
    public static final int BUTTON_BORDER_HOVER = GOLD;

    // Dark variants
    public static final int ERROR_DARK = 0xFFB02A2A;
    public static final int SUCCESS_DARK = 0xFF28A745;
    public static final int WARNING_DARK = 0xFFD4A634;
    public static final int INFO_DARK = 0xFF4080B0;

    // War colors
    public static final int WAR_ACTIVE = ERROR;
    public static final int WAR_PEACE = SUCCESS;

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT DIMENSIONS
    // ═══════════════════════════════════════════════════════════════════════════
    public static final int BUTTON_HEIGHT_SM = 28;
    public static final int BUTTON_HEIGHT_MD = 36;
    public static final int BUTTON_HEIGHT_LG = 44;

    public static final int CARD_HEIGHT = 80;
    public static final int CARD_CORNER_RADIUS = 4;

    public static final int HEADER_HEIGHT = 56;
    public static final int FOOTER_HEIGHT = 48;

    public static final int INPUT_HEIGHT = 32;
    public static final int ICON_SIZE_SM = 16;
    public static final int ICON_SIZE_MD = 24;
    public static final int ICON_SIZE_LG = 32;

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════════════
    public static final float ANIM_SPEED_FAST = 0.15f;
    public static final float ANIM_SPEED_NORMAL = 0.1f;
    public static final float ANIM_SPEED_SLOW = 0.06f;

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create color with custom alpha
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * Interpolate between two colors
     */
    public static int lerp(int color1, int color2, float t) {
        t = Math.max(0, Math.min(1, t));
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int)(a1 + (a2 - a1) * t);
        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Ease out cubic
     */
    public static float easeOut(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }

    /**
     * Ease in out cubic
     */
    public static float easeInOut(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DRAWING UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Draw a filled rounded rectangle (approximated with corner fills)
     */
    public static void fillRounded(DrawContext ctx, int x, int y, int width, int height, int radius, int color) {
        // Main body
        ctx.fill(x + radius, y, x + width - radius, y + height, color);
        ctx.fill(x, y + radius, x + radius, y + height - radius, color);
        ctx.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        // Corners (simplified as small fills)
        ctx.fill(x + 1, y + 1, x + radius, y + radius, color);
        ctx.fill(x + width - radius, y + 1, x + width - 1, y + radius, color);
        ctx.fill(x + 1, y + height - radius, x + radius, y + height - 1, color);
        ctx.fill(x + width - radius, y + height - radius, x + width - 1, y + height - 1, color);
    }

    /**
     * Draw a border
     */
    public static void drawBorder(DrawContext ctx, int x, int y, int width, int height, int color) {
        ctx.fill(x, y, x + width, y + 1, color);                    // Top
        ctx.fill(x, y + height - 1, x + width, y + height, color);  // Bottom
        ctx.fill(x, y, x + 1, y + height, color);                   // Left
        ctx.fill(x + width - 1, y, x + width, y + height, color);   // Right
    }

    /**
     * Draw border with accent top
     */
    public static void drawPanelBorder(DrawContext ctx, int x, int y, int width, int height, int borderColor, int accentColor) {
        ctx.fill(x, y, x + width, y + 2, accentColor);              // Top accent (thicker)
        ctx.fill(x, y + height - 1, x + width, y + height, borderColor);
        ctx.fill(x, y + 2, x + 1, y + height - 1, borderColor);
        ctx.fill(x + width - 1, y + 2, x + width, y + height - 1, borderColor);
    }

    /**
     * Draw a horizontal divider
     */
    public static void drawDivider(DrawContext ctx, int x, int y, int width) {
        ctx.fill(x, y, x + width, y + 1, BORDER_SUBTLE);
    }

    /**
     * Draw a vertical accent bar
     */
    public static void drawAccentBar(DrawContext ctx, int x, int y, int height, int color) {
        ctx.fill(x, y, x + 3, y + height, color);
    }

    /**
     * Draw a glow effect (outer)
     */
    public static void drawGlow(DrawContext ctx, int x, int y, int width, int height, int color) {
        int glowColor = withAlpha(color, 20);
        ctx.fill(x - 2, y - 2, x + width + 2, y, glowColor);
        ctx.fill(x - 2, y + height, x + width + 2, y + height + 2, glowColor);
        ctx.fill(x - 2, y, x, y + height, glowColor);
        ctx.fill(x + width, y, x + width + 2, y + height, glowColor);
    }

    /**
     * Draw the Hegemonia logo at specified position and size
     */
    public static void drawLogo(DrawContext ctx, int x, int y, int size) {
        ctx.drawTexture(LOGO, x, y, 0, 0, size, size, size, size);
    }

    /**
     * Draw card background with optional hover state
     */
    public static void drawCard(DrawContext ctx, int x, int y, int width, int height, boolean hovered, int accentColor) {
        int bgColor = hovered ? BG_CARD_HOVER : BG_CARD;
        int borderColor = hovered ? accentColor : BORDER_DEFAULT;

        // Background
        ctx.fill(x, y, x + width, y + height, bgColor);

        // Border
        drawBorder(ctx, x, y, width, height, borderColor);

        // Left accent bar
        if (accentColor != 0) {
            ctx.fill(x, y + 4, x + 3, y + height - 4, accentColor);
        }

        // Hover glow effect
        if (hovered) {
            drawGlow(ctx, x, y, width, height, accentColor);
        }
    }

    /**
     * Draw a button background
     */
    public static void drawButton(DrawContext ctx, int x, int y, int width, int height,
                                   boolean hovered, boolean pressed, int accentColor) {
        int bgColor;
        int borderColor;

        if (pressed) {
            bgColor = withAlpha(accentColor, 40);
            borderColor = accentColor;
        } else if (hovered) {
            bgColor = BG_BUTTON_HOVER;
            borderColor = withAlpha(accentColor, 180);
        } else {
            bgColor = BG_BUTTON;
            borderColor = BORDER_DEFAULT;
        }

        ctx.fill(x, y, x + width, y + height, bgColor);
        drawBorder(ctx, x, y, width, height, borderColor);
    }

    /**
     * Draw a primary (gold) button
     */
    public static void drawPrimaryButton(DrawContext ctx, int x, int y, int width, int height,
                                          boolean hovered, boolean pressed) {
        int bgColor;

        if (pressed) {
            bgColor = GOLD_DARK;
        } else if (hovered) {
            bgColor = GOLD_LIGHT;
        } else {
            bgColor = GOLD;
        }

        ctx.fill(x, y, x + width, y + height, bgColor);

        if (hovered) {
            drawGlow(ctx, x, y, width, height, GOLD);
        }
    }

    /**
     * Draw an icon placeholder (simple geometric shapes)
     */
    public static void drawIcon(DrawContext ctx, String iconType, int x, int y, int size, int color) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        int s = size / 4;

        switch (iconType.toLowerCase()) {
            case "economy", "coin" -> {
                // Coin shape
                ctx.fill(cx - s, cy - s - 2, cx + s, cy + s + 2, color);
                ctx.fill(cx - s - 2, cy - s, cx + s + 2, cy + s, color);
            }
            case "nation", "flag" -> {
                // Flag shape
                ctx.fill(x + 3, y + 2, x + 5, y + size - 2, color);
                ctx.fill(x + 5, y + 3, x + size - 3, y + size / 2, color);
            }
            case "war", "sword" -> {
                // Diagonal sword
                for (int i = 0; i < size - 4; i++) {
                    ctx.fill(x + 2 + i, y + 2 + i, x + 4 + i, y + 4 + i, color);
                }
            }
            case "territory", "map" -> {
                // Grid pattern
                ctx.fill(x + 2, y + 2, x + size - 2, y + 4, color);
                ctx.fill(x + 2, y + size / 2 - 1, x + size - 2, y + size / 2 + 1, color);
                ctx.fill(x + 2, y + size - 4, x + size - 2, y + size - 2, color);
                ctx.fill(x + 2, y + 2, x + 4, y + size - 2, color);
                ctx.fill(x + size / 2 - 1, y + 2, x + size / 2 + 1, y + size - 2, color);
            }
            case "settings", "gear" -> {
                // Gear approximation
                ctx.fill(cx - s, cy - s, cx + s, cy + s, color);
                ctx.fill(cx - 1, cy - s - 2, cx + 1, cy + s + 2, color);
                ctx.fill(cx - s - 2, cy - 1, cx + s + 2, cy + 1, color);
            }
            case "bank" -> {
                // Building shape
                ctx.fill(x + 2, y + size - 4, x + size - 2, y + size - 2, color);
                ctx.fill(x + 4, y + 4, x + size - 4, y + size - 4, color);
                ctx.fill(x + 2, y + 2, x + size - 2, y + 4, color);
            }
            case "market" -> {
                // Chart bars
                ctx.fill(x + 2, y + size / 2, x + 5, y + size - 2, color);
                ctx.fill(x + 6, y + 4, x + 9, y + size - 2, color);
                ctx.fill(x + 10, y + size / 3, x + 13, y + size - 2, color);
            }
            case "arrow" -> {
                // Right arrow
                ctx.fill(cx - s, cy - 1, cx + s - 2, cy + 1, color);
                ctx.fill(cx + s - 4, cy - s + 2, cx + s, cy, color);
                ctx.fill(cx + s - 4, cy, cx + s, cy + s - 2, color);
            }
            default -> {
                // Default circle
                ctx.fill(cx - s, cy - s, cx + s, cy + s, color);
            }
        }
    }
}
