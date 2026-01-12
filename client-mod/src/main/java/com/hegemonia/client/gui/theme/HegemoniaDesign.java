package com.hegemonia.client.gui.theme;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * HEGEMONIA DESIGN SYSTEM v2.0
 *
 * Modern, futuristic design inspired by Nations Glory
 * Dark theme with gold accents
 *
 * Layout Grid: 8px base unit
 * Colors: Dark backgrounds, gold primary, semantic colors
 */
public final class HegemoniaDesign {

    private HegemoniaDesign() {}

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXTURES & IDENTIFIERS
    // ═══════════════════════════════════════════════════════════════════════════

    public static final Identifier LOGO = Identifier.of("hegemonia", "textures/gui/logo.png");
    public static final Identifier LOGO_LARGE = Identifier.of("hegemonia", "textures/gui/logo_large.png");
    public static final Identifier ICONS = Identifier.of("hegemonia", "textures/gui/icons.png");

    // ═══════════════════════════════════════════════════════════════════════════
    // GRID SYSTEM (8px base)
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int UNIT = 8;
    public static final int SPACE_XXS = 2;
    public static final int SPACE_XS = 4;
    public static final int SPACE_SM = 8;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_XL = 32;
    public static final int SPACE_XXL = 48;

    // ═══════════════════════════════════════════════════════════════════════════
    // COLORS - DARK THEME
    // ═══════════════════════════════════════════════════════════════════════════

    // Backgrounds (darkest to lightest)
    public static final int BG_DARK = 0xFF08080C;           // Darkest background
    public static final int BG_PRIMARY = 0xFF0C0C12;        // Main background
    public static final int BG_SECONDARY = 0xFF101018;      // Secondary panels
    public static final int BG_TERTIARY = 0xFF14141C;       // Cards, elevated elements
    public static final int BG_HOVER = 0xFF1A1A24;          // Hover state
    public static final int BG_ACTIVE = 0xFF1E1E2A;         // Active/pressed state

    // Overlay
    public static final int OVERLAY_DARK = 0xE6000000;      // 90% black
    public static final int OVERLAY_MEDIUM = 0xB3000000;    // 70% black
    public static final int OVERLAY_LIGHT = 0x80000000;     // 50% black

    // Primary - Gold
    public static final int GOLD = 0xFFD4A634;              // Primary gold
    public static final int GOLD_LIGHT = 0xFFE8C04A;        // Lighter gold (hover)
    public static final int GOLD_DARK = 0xFFB08A28;         // Darker gold
    public static final int GOLD_MUTED = 0x80D4A634;        // 50% opacity
    public static final int GOLD_GLOW = 0x40D4A634;         // 25% opacity (glow)
    public static final int GOLD_SUBTLE = 0x20D4A634;       // 12% opacity

    // Secondary - Blue
    public static final int BLUE = 0xFF4A9FD4;
    public static final int BLUE_LIGHT = 0xFF6BB8E8;
    public static final int BLUE_DARK = 0xFF3080B0;
    public static final int BLUE_MUTED = 0x804A9FD4;

    // Semantic Colors
    public static final int SUCCESS = 0xFF34C759;           // Green
    public static final int SUCCESS_DARK = 0xFF28A745;
    public static final int WARNING = 0xFFFFCC00;           // Yellow
    public static final int WARNING_DARK = 0xFFE6B800;
    public static final int ERROR = 0xFFE53935;             // Red
    public static final int ERROR_LIGHT = 0xFFFF5252;
    public static final int ERROR_DARK = 0xFFB02A2A;
    public static final int INFO = 0xFF5B9BD5;              // Light blue

    // Text
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;      // White
    public static final int TEXT_SECONDARY = 0xFFB8B8C8;    // Light gray
    public static final int TEXT_MUTED = 0xFF6B6B7B;        // Gray
    public static final int TEXT_DISABLED = 0xFF404050;     // Dark gray
    public static final int TEXT_GOLD = GOLD;               // Gold accent text

    // Borders
    public static final int BORDER_DEFAULT = 0xFF2A2A36;
    public static final int BORDER_SUBTLE = 0xFF1E1E28;
    public static final int BORDER_FOCUS = 0xFF3A3A4A;
    public static final int BORDER_GOLD = GOLD;

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPONENT DIMENSIONS
    // ═══════════════════════════════════════════════════════════════════════════

    // Buttons
    public static final int BTN_HEIGHT_SM = 24;
    public static final int BTN_HEIGHT_MD = 32;
    public static final int BTN_HEIGHT_LG = 40;

    // Category buttons (for inventory sidebar)
    public static final int CATEGORY_BTN_SIZE = 32;         // Square buttons
    public static final int CATEGORY_BTN_GAP = 4;           // Gap between buttons
    public static final int CATEGORY_ICON_SIZE = 20;        // Icon inside button

    // Info bar
    public static final int INFO_BAR_HEIGHT = 28;

    // Panels
    public static final int PANEL_PADDING = 12;
    public static final int CARD_PADDING = 8;

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════════════

    public static final float ANIM_FAST = 0.2f;
    public static final float ANIM_NORMAL = 0.12f;
    public static final float ANIM_SLOW = 0.08f;

    // ═══════════════════════════════════════════════════════════════════════════
    // CATEGORY DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════════════

    public enum Category {
        ECONOMY("Economie", "Gerez votre argent", GOLD, "E"),
        BANK("Banque", "Epargne et prets", 0xFF5B9BD5, "B"),
        NATION("Nation", "Votre empire", BLUE, "N"),
        WAR("Guerre", "Combat et conquete", ERROR, "G"),
        MARKET("Marche", "Achat et vente", WARNING, "M"),
        DIPLOMACY("Diplomatie", "Relations", 0xFF9C27B0, "D"),
        TERRITORY("Territoires", "Carte du monde", SUCCESS, "T"),
        LEADERBOARD("Classements", "Top joueurs", 0xFF00BCD4, "C");

        public final String name;
        public final String description;
        public final int color;
        public final String shortcut;

        Category(String name, String description, int color, String shortcut) {
            this.name = name;
            this.description = description;
            this.color = color;
            this.shortcut = shortcut;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Create color with custom alpha (0-255) */
    public static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    /** Interpolate between two colors */
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

    /** Ease out cubic */
    public static float easeOut(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }

    /** Ease in out cubic */
    public static float easeInOut(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    /** Smooth step */
    public static float smoothStep(float t) {
        return t * t * (3 - 2 * t);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DRAWING HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Draw a simple border */
    public static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);              // Top
        ctx.fill(x, y + h - 1, x + w, y + h, color);      // Bottom
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);      // Left
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color); // Right
    }

    /** Draw border with gold accent on top */
    public static void drawAccentBorder(DrawContext ctx, int x, int y, int w, int h, int borderColor) {
        ctx.fill(x, y, x + w, y + 2, GOLD);               // Top accent (2px)
        ctx.fill(x, y + h - 1, x + w, y + h, borderColor);
        ctx.fill(x, y + 2, x + 1, y + h - 1, borderColor);
        ctx.fill(x + w - 1, y + 2, x + w, y + h - 1, borderColor);
    }

    /** Draw outer glow effect */
    public static void drawGlow(DrawContext ctx, int x, int y, int w, int h, int color, int size) {
        int glowColor = withAlpha(color, 15);
        for (int i = 1; i <= size; i++) {
            int alpha = 15 - (i * 3);
            if (alpha <= 0) break;
            int c = withAlpha(color, alpha);
            ctx.fill(x - i, y - i, x + w + i, y - i + 1, c);
            ctx.fill(x - i, y + h + i - 1, x + w + i, y + h + i, c);
            ctx.fill(x - i, y - i, x - i + 1, y + h + i, c);
            ctx.fill(x + w + i - 1, y - i, x + w + i, y + h + i, c);
        }
    }

    /** Draw horizontal divider */
    public static void drawDivider(DrawContext ctx, int x, int y, int width) {
        ctx.fill(x, y, x + width, y + 1, BORDER_SUBTLE);
    }

    /** Draw vertical accent bar */
    public static void drawAccentBar(DrawContext ctx, int x, int y, int height, int color) {
        ctx.fill(x, y, x + 3, y + height, color);
    }

    /** Draw a category button (icon style) */
    public static void drawCategoryButton(DrawContext ctx, int x, int y, Category cat,
                                          boolean hovered, float hoverAnim, boolean enabled) {
        int size = CATEGORY_BTN_SIZE;

        // Background
        int bgColor = !enabled ? BG_PRIMARY :
                      hovered ? lerp(BG_TERTIARY, BG_HOVER, hoverAnim) : BG_TERTIARY;
        ctx.fill(x, y, x + size, y + size, bgColor);

        // Border
        int borderColor = !enabled ? BORDER_SUBTLE :
                          hovered ? lerp(BORDER_DEFAULT, cat.color, hoverAnim * 0.7f) : BORDER_DEFAULT;
        drawBorder(ctx, x, y, size, size, borderColor);

        // Left accent when hovered
        if (hovered && enabled) {
            int accentAlpha = (int)(255 * hoverAnim);
            ctx.fill(x, y + 4, x + 2, y + size - 4, withAlpha(cat.color, accentAlpha));
        }

        // Glow effect on hover
        if (hoverAnim > 0.1f && enabled) {
            drawGlow(ctx, x, y, size, size, cat.color, 3);
        }

        // Icon (letter for now, can be replaced with actual icons)
        int iconColor = !enabled ? TEXT_DISABLED :
                        hovered ? lerp(TEXT_MUTED, cat.color, hoverAnim) : TEXT_SECONDARY;

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(cat.shortcut);
        ctx.drawText(textRenderer, cat.shortcut,
                x + (size - textWidth) / 2,
                y + (size - 8) / 2,
                iconColor, false);
    }

    /** Draw tooltip */
    public static void drawTooltip(DrawContext ctx, int x, int y, String title, String description) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;

        int titleWidth = textRenderer.getWidth(title);
        int descWidth = textRenderer.getWidth(description);
        int width = Math.max(titleWidth, descWidth) + SPACE_MD * 2;
        int height = description.isEmpty() ? 20 : 34;

        // Background
        ctx.fill(x, y, x + width, y + height, BG_PRIMARY);
        drawAccentBorder(ctx, x, y, width, height, BORDER_DEFAULT);

        // Title
        ctx.drawText(textRenderer, title, x + SPACE_SM, y + 6, TEXT_GOLD, false);

        // Description
        if (!description.isEmpty()) {
            ctx.drawText(textRenderer, description, x + SPACE_SM, y + 18, TEXT_MUTED, false);
        }
    }

    /** Draw info pill (for balance, nation tag, etc.) */
    public static void drawInfoPill(DrawContext ctx, int x, int y, String label, String value, int accentColor) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;

        int labelWidth = textRenderer.getWidth(label);
        int valueWidth = textRenderer.getWidth(value);
        int totalWidth = labelWidth + valueWidth + SPACE_SM * 3;
        int height = 18;

        // Background
        ctx.fill(x, y, x + totalWidth, y + height, BG_SECONDARY);
        ctx.fill(x, y, x + 2, y + height, accentColor);
        drawBorder(ctx, x, y, totalWidth, height, BORDER_SUBTLE);

        // Label
        ctx.drawText(textRenderer, label, x + SPACE_SM, y + 5, TEXT_MUTED, false);

        // Value
        ctx.drawText(textRenderer, value, x + SPACE_SM + labelWidth + SPACE_XS, y + 5, accentColor, false);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LEGACY COMPATIBILITY (for existing screens)
    // ═══════════════════════════════════════════════════════════════════════════

    public static final int BG_PANEL = BG_PRIMARY;
    public static final int BG_PANEL_HEADER = BG_SECONDARY;
    public static final int BG_CARD = BG_TERTIARY;
    public static final int BG_CARD_HOVER = BG_HOVER;
    public static final int BG_BUTTON = BG_TERTIARY;
    public static final int BG_BUTTON_HOVER = BG_HOVER;
    public static final int BG_INPUT = BG_DARK;

    public static final int HEADER_HEIGHT = 48;
    public static final int FOOTER_HEIGHT = 40;

    public static final int ACCENT_GOLD = GOLD;
    public static final int PANEL_BACKGROUND = BG_PRIMARY;
    public static final int BUTTON_DEFAULT = BG_TERTIARY;
    public static final int BUTTON_HOVER = BG_HOVER;
    public static final int BUTTON_DISABLED = 0xFF1A1A22;
    public static final int BUTTON_PRIMARY = GOLD;

    public static final int SCROLLBAR_BG = 0xFF1A1A22;
    public static final int SCROLLBAR_THUMB = 0xFF3A3A4A;
    public static final int SCROLLBAR_THUMB_HOVER = 0xFF4A4A5A;
    public static final int SCROLLBAR_TRACK = SCROLLBAR_BG;

    public static final int INPUT_BACKGROUND = BG_DARK;
    public static final int INPUT_BORDER = BORDER_DEFAULT;
    public static final int INPUT_BORDER_FOCUS = GOLD;
    public static final int INPUT_TEXT = TEXT_PRIMARY;
    public static final int INPUT_PLACEHOLDER = TEXT_MUTED;

    public static final float ANIM_SPEED_FAST = ANIM_FAST;
    public static final float ANIM_SPEED_NORMAL = ANIM_NORMAL;
    public static final float ANIM_SPEED_SLOW = ANIM_SLOW;

    // More legacy aliases
    public static final int PANEL_HEADER = BG_SECONDARY;
    public static final int ACCENT_BLUE = BLUE;
    public static final int WAR_ACTIVE = ERROR;
    public static final int WAR_PEACE = SUCCESS;
    public static final int BUTTON_HEIGHT_MD = BTN_HEIGHT_MD;
    public static final int MONEY_NEUTRAL = GOLD;
    public static final int MONEY_POSITIVE = SUCCESS;
    public static final int MONEY_NEGATIVE = ERROR;
    public static final int INFO_DARK = 0xFF4080B0;
    public static final int SUCCESS_MUTED = 0x8034C759;
    public static final int ERROR_LIGHT_VAR = ERROR_LIGHT;
    public static final int BACKGROUND_LIGHT = BG_TERTIARY;
    public static final int BACKGROUND_DARK = BG_DARK;
    public static final int BACKGROUND_MEDIUM = BG_SECONDARY;
    public static final int PANEL_BORDER = BORDER_DEFAULT;
    public static final int BUTTON_BORDER = BORDER_DEFAULT;
    public static final int BUTTON_BORDER_HOVER = GOLD;
    public static final int BUTTON_PRIMARY_HOVER = GOLD_LIGHT;
    public static final int BUTTON_PRIMARY_BORDER = GOLD_DARK;
    public static final int BUTTON_DANGER = ERROR;
    public static final int BUTTON_DANGER_HOVER = ERROR_LIGHT;
    public static final int BUTTON_DANGER_BORDER = ERROR_DARK;
    public static final int BUTTON_PRESSED = BG_ACTIVE;
    public static final int INPUT_BG = BG_DARK;
    public static final int INPUT_BORDER_VAR = BORDER_DEFAULT;
    public static final int TEXT_TITLE = TEXT_GOLD;

    /** Draw outer glow effect (legacy overload) */
    public static void drawGlow(DrawContext ctx, int x, int y, int w, int h, int color) {
        drawGlow(ctx, x, y, w, h, color, 3);
    }

    /** Draw border with accent top (legacy signature) */
    public static void drawPanelBorder(DrawContext ctx, int x, int y, int w, int h, int borderColor, int accentColor) {
        ctx.fill(x, y, x + w, y + 2, accentColor);              // Top accent (2px)
        ctx.fill(x, y + h - 1, x + w, y + h, borderColor);
        ctx.fill(x, y + 2, x + 1, y + h - 1, borderColor);
        ctx.fill(x + w - 1, y + 2, x + w, y + h - 1, borderColor);
    }
}
