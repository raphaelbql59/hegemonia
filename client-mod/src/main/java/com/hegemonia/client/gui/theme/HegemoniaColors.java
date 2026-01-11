package com.hegemonia.client.gui.theme;

/**
 * Hegemonia color palette - consistent colors across all GUIs
 */
public final class HegemoniaColors {

    private HegemoniaColors() {}

    // ==================== Primary Colors ====================

    // Gold accent - main brand color
    public static final int ACCENT_GOLD = 0xFFD4AF37;
    public static final int ACCENT_GOLD_DARK = 0xFFA88A2C;
    public static final int ACCENT_GOLD_LIGHT = 0xFFE8C84A;

    // Blue accent - secondary brand color
    public static final int ACCENT_BLUE = 0xFF3498DB;
    public static final int ACCENT_BLUE_DARK = 0xFF2980B9;
    public static final int ACCENT_BLUE_LIGHT = 0xFF5DADE2;

    // ==================== Backgrounds ====================

    public static final int BACKGROUND_DARK = 0xE0101018;
    public static final int BACKGROUND_MEDIUM = 0xE01A1A24;
    public static final int BACKGROUND_LIGHT = 0xE0252532;

    public static final int PANEL_BACKGROUND = 0xD0181820;
    public static final int PANEL_HEADER = 0xFF202030;
    public static final int PANEL_BORDER = 0xFF353545;

    // ==================== Text Colors ====================

    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFFAAAAAA;
    public static final int TEXT_MUTED = 0xFF666666;
    public static final int TEXT_TITLE = 0xFFD4AF37;

    // ==================== Button States ====================

    public static final int BUTTON_DEFAULT = 0xFF2A2A3A;
    public static final int BUTTON_HOVER = 0xFF3A3A4A;
    public static final int BUTTON_PRESSED = 0xFF1A1A2A;
    public static final int BUTTON_DISABLED = 0xFF1A1A22;
    public static final int BUTTON_BORDER = 0xFF454555;
    public static final int BUTTON_BORDER_HOVER = 0xFFD4AF37;

    // Primary button (gold)
    public static final int BUTTON_PRIMARY = 0xFF3D3520;
    public static final int BUTTON_PRIMARY_HOVER = 0xFF4D4530;
    public static final int BUTTON_PRIMARY_BORDER = 0xFFD4AF37;

    // Danger button (red)
    public static final int BUTTON_DANGER = 0xFF3D2020;
    public static final int BUTTON_DANGER_HOVER = 0xFF4D2530;
    public static final int BUTTON_DANGER_BORDER = 0xFFE74C3C;

    // ==================== Input Fields ====================

    public static final int INPUT_BACKGROUND = 0xFF151520;
    public static final int INPUT_BORDER = 0xFF353545;
    public static final int INPUT_BORDER_FOCUS = 0xFFD4AF37;
    public static final int INPUT_TEXT = 0xFFFFFFFF;
    public static final int INPUT_PLACEHOLDER = 0xFF555555;

    // ==================== Status Colors ====================

    public static final int SUCCESS = 0xFF2ECC71;
    public static final int SUCCESS_DARK = 0xC0183D23;

    public static final int ERROR = 0xFFE74C3C;
    public static final int ERROR_DARK = 0xC03D1818;

    public static final int WARNING = 0xFFF39C12;
    public static final int WARNING_DARK = 0xC03D3018;

    public static final int INFO = 0xFF3498DB;
    public static final int INFO_DARK = 0xC0183040;

    // ==================== Nation Colors ====================

    public static final int NATION_LEADER = 0xFFFFD700;     // Gold
    public static final int NATION_OFFICER = 0xFFE74C3C;    // Red
    public static final int NATION_MEMBER = 0xFF3498DB;     // Blue
    public static final int NATION_RECRUIT = 0xFF95A5A6;    // Gray

    // ==================== Economy Colors ====================

    public static final int MONEY_POSITIVE = 0xFF2ECC71;    // Green
    public static final int MONEY_NEGATIVE = 0xFFE74C3C;    // Red
    public static final int MONEY_NEUTRAL = 0xFFD4AF37;     // Gold

    // ==================== War Colors ====================

    public static final int WAR_ACTIVE = 0xFFE74C3C;
    public static final int WAR_PEACE = 0xFF2ECC71;
    public static final int WAR_TRUCE = 0xFFF39C12;

    // ==================== Scrollbar ====================

    public static final int SCROLLBAR_TRACK = 0xFF151520;
    public static final int SCROLLBAR_THUMB = 0xFF353545;
    public static final int SCROLLBAR_THUMB_HOVER = 0xFF454555;

    // ==================== Utility Methods ====================

    /**
     * Apply alpha to a color
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * Interpolate between two colors
     */
    public static int lerp(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
