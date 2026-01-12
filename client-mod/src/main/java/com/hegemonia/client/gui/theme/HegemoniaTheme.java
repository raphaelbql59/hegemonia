package com.hegemonia.client.gui.theme;

/**
 * Hegemonia Design System
 *
 * Style: Flat, modern, dark theme inspired by professional game UIs
 * Grid: 8px base unit
 * Font: Minecraft default (8px height)
 */
public final class HegemoniaTheme {

    private HegemoniaTheme() {}

    // ═══════════════════════════════════════════════════════════════════════════
    // SPACING SYSTEM (8px grid)
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int SPACE_XS = 4;   // Extra small
    public static final int SPACE_SM = 8;   // Small
    public static final int SPACE_MD = 16;  // Medium
    public static final int SPACE_LG = 24;  // Large
    public static final int SPACE_XL = 32;  // Extra large
    public static final int SPACE_XXL = 48; // Double extra large

    // ═══════════════════════════════════════════════════════════════════════════
    // COLOR PALETTE
    // ═══════════════════════════════════════════════════════════════════════════

    // Background colors (dark to light)
    public static final int BG_DARK = 0xFF0D0D12;      // Darkest background
    public static final int BG_PRIMARY = 0xFF14141C;   // Main background
    public static final int BG_SECONDARY = 0xFF1A1A24; // Cards, panels
    public static final int BG_TERTIARY = 0xFF22222E;  // Elevated elements
    public static final int BG_HOVER = 0xFF2A2A38;     // Hover states

    // Border colors
    public static final int BORDER_DEFAULT = 0xFF2E2E3A;  // Subtle borders
    public static final int BORDER_HOVER = 0xFF3E3E4E;    // Hover borders
    public static final int BORDER_FOCUS = 0xFF4E4E60;    // Focus borders

    // Text colors
    public static final int TEXT_PRIMARY = 0xFFE8E8EC;    // Main text
    public static final int TEXT_SECONDARY = 0xFFA0A0AC;  // Secondary text
    public static final int TEXT_MUTED = 0xFF6A6A78;      // Muted/disabled text
    public static final int TEXT_INVERSE = 0xFF0D0D12;    // Text on light backgrounds

    // Accent colors
    public static final int ACCENT_GOLD = 0xFFD4A634;     // Primary accent (gold)
    public static final int ACCENT_GOLD_LIGHT = 0xFFE8C05C;
    public static final int ACCENT_GOLD_DARK = 0xFFB08828;

    public static final int ACCENT_BLUE = 0xFF4A90D4;     // Secondary accent
    public static final int ACCENT_BLUE_LIGHT = 0xFF6AA8E8;
    public static final int ACCENT_BLUE_DARK = 0xFF3470B0;

    // Semantic colors
    public static final int SUCCESS = 0xFF4CAF50;
    public static final int SUCCESS_DARK = 0xFF388E3C;
    public static final int WARNING = 0xFFFF9800;
    public static final int WARNING_DARK = 0xFFF57C00;
    public static final int ERROR = 0xFFE53935;
    public static final int ERROR_DARK = 0xFFC62828;
    public static final int INFO = 0xFF2196F3;

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT DIMENSIONS
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int BUTTON_HEIGHT = 32;
    public static final int BUTTON_HEIGHT_SM = 24;
    public static final int BUTTON_HEIGHT_LG = 40;

    public static final int INPUT_HEIGHT = 28;
    public static final int HEADER_HEIGHT = 48;
    public static final int CARD_PADDING = 16;
    public static final int BORDER_RADIUS = 4; // Simulated with corners

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════════════

    public static final float ANIM_FAST = 0.2f;
    public static final float ANIM_NORMAL = 0.15f;
    public static final float ANIM_SLOW = 0.1f;

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create color with alpha
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * Lerp between two colors
     */
    public static int lerp(int colorA, int colorB, float t) {
        t = Math.max(0, Math.min(1, t));

        int aA = (colorA >> 24) & 0xFF;
        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = colorA & 0xFF;

        int aB = (colorB >> 24) & 0xFF;
        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;

        int a = (int) (aA + (aB - aA) * t);
        int r = (int) (rA + (rB - rA) * t);
        int g = (int) (gA + (gB - gA) * t);
        int b = (int) (bA + (bB - bA) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Brighten a color
     */
    public static int brighten(int color, float amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * (1 + amount)));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * (1 + amount)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1 + amount)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Darken a color
     */
    public static int darken(int color, float amount) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * (1 - amount));
        int g = (int) (((color >> 8) & 0xFF) * (1 - amount));
        int b = (int) ((color & 0xFF) * (1 - amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
