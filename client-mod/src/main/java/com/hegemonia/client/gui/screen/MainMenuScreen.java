package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

/**
 * HEGEMONIA MAIN MENU
 * Ultra-professional central hub design
 *
 * Layout:
 * - Central logo with glow effect
 * - 6 main cards in hexagonal layout around center
 * - Smooth animations and hover effects
 * - Bottom quick actions bar
 */
public class MainMenuScreen extends Screen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final Identifier LOGO = Identifier.of("hegemonia", "textures/gui/logo.png");

    private final HegemoniaClient hegemonia;

    // Layout
    private int centerX, centerY;
    private int panelX, panelY, panelWidth, panelHeight;

    // Animation
    private float openAnim = 0f;
    private float logoGlow = 0f;
    private float[] cardAnim = new float[6];
    private float[] cardHover = new float[6];
    private int hoveredCard = -1;
    private int hoveredQuickAction = -1;
    private long startTime;

    // Main feature cards (6 cards around center)
    private static final MenuCard[] CARDS = {
        new MenuCard("ECONOMIE", "Votre fortune", "economy", 0xFFD4A634, "H"),
        new MenuCard("NATION", "Votre empire", "nation", 0xFF4A9FD4, "N"),
        new MenuCard("GUERRE", "Combat", "war", 0xFFE53935, "W"),
        new MenuCard("MARCHE", "Commerce", "market", 0xFFFFCC00, "M"),
        new MenuCard("BANQUE", "Epargne", "bank", 0xFF5B9BD5, "B"),
        new MenuCard("CARTE", "Monde", "territory", 0xFF34C759, "C"),
    };

    // Quick actions
    private static final String[] QUICK_ACTIONS = {"Diplomatie", "Elections", "Classements", "Reglages"};
    private static final String[] QUICK_ACTION_IDS = {"diplomacy", "election", "leaderboard", "settings"};

    public MainMenuScreen() {
        super(Text.literal("Hegemonia"));
        this.hegemonia = HegemoniaClient.getInstance();
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        panelWidth = Math.min(720, (int)(width * 0.92));
        panelHeight = Math.min(520, (int)(height * 0.92));
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        centerX = width / 2;
        centerY = panelY + panelHeight / 2 - 20;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateAnimations(mouseX, mouseY);
        float ease = easeOutCubic(openAnim);

        // Background
        renderBackground(ctx, ease);

        // Main panel
        renderPanel(ctx, ease);

        // Header with player info
        renderHeader(ctx, mouseX, mouseY, ease);

        // Central logo hub
        renderCentralHub(ctx, mouseX, mouseY, ease);

        // Feature cards around center
        renderCards(ctx, mouseX, mouseY, ease);

        // Bottom quick actions
        renderQuickActions(ctx, mouseX, mouseY, ease);

        // Keyboard hints
        renderKeyHints(ctx, ease);
    }

    private void updateAnimations(int mouseX, int mouseY) {
        openAnim = Math.min(1f, openAnim + 0.08f);
        logoGlow = (float)(0.5f + 0.5f * Math.sin((System.currentTimeMillis() - startTime) / 800.0));

        // Card entrance (staggered)
        for (int i = 0; i < 6; i++) {
            float delay = 0.15f + i * 0.06f;
            if (openAnim > delay) {
                cardAnim[i] = Math.min(1f, cardAnim[i] + 0.12f);
            }
        }

        // Card hover
        for (int i = 0; i < 6; i++) {
            float target = (i == hoveredCard) ? 1f : 0f;
            cardHover[i] += (target - cardHover[i]) * 0.25f;
        }
    }

    private void renderBackground(DrawContext ctx, float ease) {
        // Gradient overlay
        int alpha = (int)(245 * ease);
        for (int y = 0; y < height; y++) {
            float t = (float) y / height;
            int a = (int)(alpha * (0.95f + 0.05f * t));
            ctx.fill(0, y, width, y + 1, (a << 24) | 0x06080C);
        }

        // Animated ambient lines (subtle)
        if (ease > 0.3f) {
            long time = System.currentTimeMillis();
            int lineAlpha = (int)(8 * ease);
            for (int i = 0; i < 3; i++) {
                int y = (int)((time / 80 + i * 120) % height);
                ctx.fill(0, y, width, y + 1, (lineAlpha << 24) | 0xD4A634);
            }
        }
    }

    private void renderPanel(DrawContext ctx, float ease) {
        int alpha = (int)(255 * ease);

        // Main background with subtle gradient
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0x0A0A10, alpha));

        // Outer glow
        int glowAlpha = (int)(25 * ease);
        ctx.fill(panelX - 3, panelY - 3, panelX + panelWidth + 3, panelY,
                HegemoniaDesign.withAlpha(0xD4A634, glowAlpha));
        ctx.fill(panelX - 3, panelY + panelHeight, panelX + panelWidth + 3, panelY + panelHeight + 3,
                HegemoniaDesign.withAlpha(0xD4A634, glowAlpha));
        ctx.fill(panelX - 3, panelY, panelX, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0xD4A634, glowAlpha));
        ctx.fill(panelX + panelWidth, panelY, panelX + panelWidth + 3, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0xD4A634, glowAlpha));

        // Top accent bar (gold gradient effect)
        ctx.fill(panelX, panelY, panelX + panelWidth, panelY + 3,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));

        // Side borders
        ctx.fill(panelX, panelY + 3, panelX + 1, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0x1A1A24, alpha));
        ctx.fill(panelX + panelWidth - 1, panelY + 3, panelX + panelWidth, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0x1A1A24, alpha));
        ctx.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0x1A1A24, alpha));

        // Corner accents
        int cornerLen = 12;
        ctx.fill(panelX, panelY + panelHeight - cornerLen, panelX + 3, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));
        ctx.fill(panelX + panelWidth - 3, panelY + panelHeight - cornerLen, panelX + panelWidth, panelY + panelHeight,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));
    }

    private void renderHeader(DrawContext ctx, int mouseX, int mouseY, float ease) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        int alpha = (int)(255 * ease);

        // Header bar
        int headerHeight = 48;
        ctx.fill(panelX + 1, panelY + 3, panelX + panelWidth - 1, panelY + headerHeight,
                HegemoniaDesign.withAlpha(0x0E0E14, alpha));
        ctx.fill(panelX + 1, panelY + headerHeight - 1, panelX + panelWidth - 1, panelY + headerHeight,
                HegemoniaDesign.withAlpha(0x1A1A24, alpha));

        // Left: Title
        ctx.drawText(textRenderer, "HEGEMONIA",
                panelX + 20, panelY + 16,
                HegemoniaDesign.withAlpha(0xD4A634, alpha), true);

        // Center: Balance display
        String balance = MONEY_FORMAT.format(data.getTotalBalance()) + " H";
        int balanceWidth = textRenderer.getWidth(balance);
        int balanceX = centerX - balanceWidth / 2;

        // Balance background pill
        int pillPadding = 12;
        ctx.fill(balanceX - pillPadding, panelY + 12,
                balanceX + balanceWidth + pillPadding, panelY + 32,
                HegemoniaDesign.withAlpha(0x12121A, alpha));
        ctx.fill(balanceX - pillPadding, panelY + 12,
                balanceX - pillPadding + 2, panelY + 32,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));

        ctx.drawText(textRenderer, balance, balanceX, panelY + 18,
                HegemoniaDesign.withAlpha(0xD4A634, alpha), true);

        // Right: Player info
        String playerName = client != null && client.player != null ? client.player.getName().getString() : "Joueur";
        if (data.hasNation()) {
            playerName = "[" + data.nationTag + "] " + playerName;
        }
        int nameWidth = textRenderer.getWidth(playerName);
        ctx.drawText(textRenderer, playerName,
                panelX + panelWidth - nameWidth - 20, panelY + 16,
                HegemoniaDesign.withAlpha(0xB8B8C8, alpha), false);

        // Close button
        int closeBtnX = panelX + panelWidth - 36;
        int closeBtnY = panelY + 8;
        int closeBtnSize = 32;
        boolean closeHovered = mouseX >= closeBtnX && mouseX < closeBtnX + closeBtnSize &&
                mouseY >= closeBtnY && mouseY < closeBtnY + closeBtnSize;

        if (closeHovered) {
            ctx.fill(closeBtnX, closeBtnY, closeBtnX + closeBtnSize, closeBtnY + closeBtnSize,
                    HegemoniaDesign.withAlpha(0xE53935, 40));
        }
        ctx.drawCenteredTextWithShadow(textRenderer, "X",
                closeBtnX + closeBtnSize / 2, closeBtnY + 12,
                closeHovered ? 0xFFE53935 : 0xFF6B6B7B);
    }

    private void renderCentralHub(DrawContext ctx, int mouseX, int mouseY, float ease) {
        int alpha = (int)(255 * ease);

        // Logo size
        int logoSize = 80;
        int logoX = centerX - logoSize / 2;
        int logoY = centerY - logoSize / 2;

        // Pulsing glow effect behind logo
        int glowSize = logoSize + 30;
        int glowX = centerX - glowSize / 2;
        int glowY = centerY - glowSize / 2;

        int glowAlpha = (int)(40 * logoGlow * ease);
        int glowColor = HegemoniaDesign.withAlpha(0xD4A634, glowAlpha);

        // Multi-layer glow
        for (int i = 3; i >= 1; i--) {
            int layerSize = logoSize + 10 * i;
            int layerX = centerX - layerSize / 2;
            int layerY = centerY - layerSize / 2;
            int layerAlpha = (int)((15 - i * 3) * logoGlow * ease);
            ctx.fill(layerX, layerY, layerX + layerSize, layerY + layerSize,
                    HegemoniaDesign.withAlpha(0xD4A634, layerAlpha));
        }

        // Logo background circle
        int bgSize = logoSize + 12;
        int bgX = centerX - bgSize / 2;
        int bgY = centerY - bgSize / 2;
        ctx.fill(bgX, bgY, bgX + bgSize, bgY + bgSize,
                HegemoniaDesign.withAlpha(0x0A0A10, alpha));
        ctx.fill(bgX, bgY, bgX + bgSize, bgY + 2,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));
        ctx.fill(bgX, bgY + bgSize - 2, bgX + bgSize, bgY + bgSize,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));
        ctx.fill(bgX, bgY, bgX + 2, bgY + bgSize,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));
        ctx.fill(bgX + bgSize - 2, bgY, bgX + bgSize, bgY + bgSize,
                HegemoniaDesign.withAlpha(0xD4A634, alpha));

        // Render logo
        ctx.drawTexture(LOGO, logoX, logoY, 0, 0, logoSize, logoSize, logoSize, logoSize);
    }

    private void renderCards(DrawContext ctx, int mouseX, int mouseY, float ease) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Card dimensions
        int cardWidth = 140;
        int cardHeight = 90;
        int radius = 150; // Distance from center

        hoveredCard = -1;

        // 6 cards in circular layout
        double[] angles = {-60, 0, 60, 120, 180, 240}; // Degrees

        for (int i = 0; i < 6; i++) {
            float anim = easeOutCubic(cardAnim[i]);
            if (anim < 0.01f) continue;

            double angle = Math.toRadians(angles[i] - 90); // -90 to start from top
            int cardCenterX = centerX + (int)(radius * Math.cos(angle));
            int cardCenterY = centerY + (int)(radius * Math.sin(angle));
            int cardX = cardCenterX - cardWidth / 2;
            int cardY = cardCenterY - cardHeight / 2;

            // Animate in from center
            float slideProgress = anim;
            int animCardX = centerX + (int)((cardX - centerX) * slideProgress) - cardWidth / 2 + (int)(cardWidth / 2 * slideProgress);
            int animCardY = centerY + (int)((cardY - centerY) * slideProgress) - cardHeight / 2 + (int)(cardHeight / 2 * slideProgress);

            // Corrected position
            animCardX = (int)(centerX + (cardCenterX - centerX) * slideProgress) - cardWidth / 2;
            animCardY = (int)(centerY + (cardCenterY - centerY) * slideProgress) - cardHeight / 2;

            boolean hovered = mouseX >= animCardX && mouseX < animCardX + cardWidth &&
                    mouseY >= animCardY && mouseY < animCardY + cardHeight;

            if (hovered && isCardEnabled(CARDS[i], data)) {
                hoveredCard = i;
            }

            renderCard(ctx, animCardX, animCardY, cardWidth, cardHeight, CARDS[i], data, anim, cardHover[i], hovered);
        }
    }

    private boolean isCardEnabled(MenuCard card, HegemoniaClient.PlayerData data) {
        if (card.id.equals("war") && !data.hasNation()) return false;
        return true;
    }

    private void renderCard(DrawContext ctx, int x, int y, int w, int h,
                            MenuCard card, HegemoniaClient.PlayerData data,
                            float anim, float hover, boolean isHovered) {

        int alpha = (int)(255 * anim);
        boolean enabled = isCardEnabled(card, data);

        // Hover offset
        int offsetY = (int)(-4 * hover);
        y += offsetY;

        // Background
        int bgColor = enabled ? HegemoniaDesign.lerp(0x12121A, 0x1A1A24, hover) : 0x0E0E14;
        ctx.fill(x, y, x + w, y + h, HegemoniaDesign.withAlpha(bgColor, alpha));

        // Border
        int borderColor = enabled ?
                HegemoniaDesign.lerp(0x2A2A36, card.color, hover * 0.7f) : 0x1A1A22;

        ctx.fill(x, y, x + w, y + 1, HegemoniaDesign.withAlpha(borderColor, alpha));
        ctx.fill(x, y + h - 1, x + w, y + h, HegemoniaDesign.withAlpha(borderColor, alpha));
        ctx.fill(x, y, x + 1, y + h, HegemoniaDesign.withAlpha(borderColor, alpha));
        ctx.fill(x + w - 1, y, x + w, y + h, HegemoniaDesign.withAlpha(borderColor, alpha));

        // Left accent
        int accentColor = enabled ? card.color : 0x3A3A4A;
        ctx.fill(x, y + 8, x + 4, y + h - 8, HegemoniaDesign.withAlpha(accentColor, alpha));

        // Hover glow
        if (hover > 0.1f && enabled) {
            int glowAlpha = (int)(20 * hover * anim);
            int glowColor = HegemoniaDesign.withAlpha(card.color, glowAlpha);
            ctx.fill(x - 4, y - 4, x + w + 4, y, glowColor);
            ctx.fill(x - 4, y + h, x + w + 4, y + h + 4, glowColor);
            ctx.fill(x - 4, y, x, y + h, glowColor);
            ctx.fill(x + w, y, x + w + 4, y + h, glowColor);
        }

        // Icon background
        int iconBgSize = 32;
        int iconX = x + 12;
        int iconY = y + 10;
        ctx.fill(iconX, iconY, iconX + iconBgSize, iconY + iconBgSize,
                HegemoniaDesign.withAlpha(card.color, (int)(25 * anim)));

        // Icon letter
        int iconColor = enabled ? card.color : 0x4A4A5A;
        ctx.drawCenteredTextWithShadow(textRenderer, card.icon,
                iconX + iconBgSize / 2, iconY + iconBgSize / 2 - 4,
                HegemoniaDesign.withAlpha(iconColor, alpha));

        // Title
        int textColor = enabled ? 0xFFFFFF : 0x5A5A6A;
        ctx.drawText(textRenderer, card.title,
                iconX + iconBgSize + 8, iconY + 4,
                HegemoniaDesign.withAlpha(textColor, alpha), false);

        // Subtitle
        ctx.drawText(textRenderer, card.subtitle,
                iconX + iconBgSize + 8, iconY + 16,
                HegemoniaDesign.withAlpha(0x6B6B7B, alpha), false);

        // Value at bottom
        String value = getCardValue(card.id, data);
        ctx.drawText(textRenderer, value,
                x + 12, y + h - 20,
                HegemoniaDesign.withAlpha(enabled ? card.color : 0x4A4A5A, alpha), true);

        // Arrow on hover
        if (enabled) {
            int arrowAlpha = (int)(alpha * (0.3f + hover * 0.7f));
            ctx.drawText(textRenderer, ">",
                    x + w - 16, y + h / 2 - 4,
                    HegemoniaDesign.withAlpha(0x6B6B7B, arrowAlpha), false);
        }

        // Disabled overlay
        if (!enabled) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1,
                    HegemoniaDesign.withAlpha(0x000000, (int)(80 * anim)));
        }
    }

    private String getCardValue(String id, HegemoniaClient.PlayerData data) {
        return switch (id) {
            case "economy" -> MONEY_FORMAT.format(data.balance) + " H";
            case "nation" -> data.hasNation() ? data.nationName : "Rejoindre";
            case "war" -> data.atWar ? "EN GUERRE" : "Paix";
            case "market" -> "Offres";
            case "bank" -> MONEY_FORMAT.format(data.bankBalance) + " H";
            case "territory" -> "Explorer";
            default -> "";
        };
    }

    private void renderQuickActions(DrawContext ctx, int mouseX, int mouseY, float ease) {
        int alpha = (int)(255 * ease);
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Quick action bar at bottom
        int barY = panelY + panelHeight - 55;
        int barHeight = 40;

        // Background
        ctx.fill(panelX + 1, barY, panelX + panelWidth - 1, barY + barHeight,
                HegemoniaDesign.withAlpha(0x0C0C12, alpha));
        ctx.fill(panelX + 1, barY, panelX + panelWidth - 1, barY + 1,
                HegemoniaDesign.withAlpha(0x1A1A24, alpha));

        // Quick action buttons
        int buttonWidth = 100;
        int buttonHeight = 28;
        int buttonSpacing = 12;
        int totalWidth = QUICK_ACTIONS.length * buttonWidth + (QUICK_ACTIONS.length - 1) * buttonSpacing;
        int startX = centerX - totalWidth / 2;
        int buttonY = barY + (barHeight - buttonHeight) / 2;

        hoveredQuickAction = -1;

        for (int i = 0; i < QUICK_ACTIONS.length; i++) {
            int btnX = startX + i * (buttonWidth + buttonSpacing);

            boolean hovered = mouseX >= btnX && mouseX < btnX + buttonWidth &&
                    mouseY >= buttonY && mouseY < buttonY + buttonHeight;

            // Disable nation-required actions
            boolean enabled = true;
            if ((QUICK_ACTION_IDS[i].equals("diplomacy") || QUICK_ACTION_IDS[i].equals("election")) && !data.hasNation()) {
                enabled = false;
            }

            if (hovered && enabled) {
                hoveredQuickAction = i;
            }

            int bgColor = !enabled ? 0x0A0A10 :
                    hovered ? 0x1E1E28 : 0x14141C;
            int borderColor = !enabled ? 0x1A1A22 :
                    hovered ? 0xD4A634 : 0x2A2A36;
            int textColor = !enabled ? 0x4A4A5A :
                    hovered ? 0xD4A634 : 0x8B8B9B;

            ctx.fill(btnX, buttonY, btnX + buttonWidth, buttonY + buttonHeight,
                    HegemoniaDesign.withAlpha(bgColor, alpha));
            ctx.fill(btnX, buttonY, btnX + buttonWidth, buttonY + 1,
                    HegemoniaDesign.withAlpha(borderColor, alpha));
            ctx.fill(btnX, buttonY + buttonHeight - 1, btnX + buttonWidth, buttonY + buttonHeight,
                    HegemoniaDesign.withAlpha(borderColor, alpha));
            ctx.fill(btnX, buttonY, btnX + 1, buttonY + buttonHeight,
                    HegemoniaDesign.withAlpha(borderColor, alpha));
            ctx.fill(btnX + buttonWidth - 1, buttonY, btnX + buttonWidth, buttonY + buttonHeight,
                    HegemoniaDesign.withAlpha(borderColor, alpha));

            int textWidth = textRenderer.getWidth(QUICK_ACTIONS[i]);
            ctx.drawText(textRenderer, QUICK_ACTIONS[i],
                    btnX + (buttonWidth - textWidth) / 2, buttonY + (buttonHeight - 8) / 2,
                    HegemoniaDesign.withAlpha(textColor, alpha), false);
        }

        // Version at bottom right
        String version = "v1.0.0";
        ctx.drawText(textRenderer, version,
                panelX + panelWidth - textRenderer.getWidth(version) - 16,
                barY + (barHeight - 8) / 2,
                HegemoniaDesign.withAlpha(0x3A3A4A, alpha), false);

        // Server status at bottom left
        String status = data.isConnectedToHegemonia ? "Connecte" : "Hors ligne";
        int statusColor = data.isConnectedToHegemonia ? 0x34C759 : 0xE53935;
        ctx.drawText(textRenderer, status,
                panelX + 16, barY + (barHeight - 8) / 2,
                HegemoniaDesign.withAlpha(statusColor, alpha), false);
    }

    private void renderKeyHints(DrawContext ctx, float ease) {
        int alpha = (int)(120 * ease);

        // Keyboard hints under cards
        String hint = "[E] Menu  [ESC] Fermer";
        int hintWidth = textRenderer.getWidth(hint);
        ctx.drawText(textRenderer, hint,
                centerX - hintWidth / 2, panelY + panelHeight - 12,
                HegemoniaDesign.withAlpha(0x4A4A5A, alpha), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();

        // Play click sound
        if (client != null && (hoveredCard >= 0 || hoveredQuickAction >= 0)) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }

        // Close button
        int closeBtnX = panelX + panelWidth - 36;
        int closeBtnY = panelY + 8;
        if (mouseX >= closeBtnX && mouseX < closeBtnX + 32 &&
                mouseY >= closeBtnY && mouseY < closeBtnY + 32) {
            close();
            return true;
        }

        // Card clicks
        if (hoveredCard >= 0) {
            MenuCard card = CARDS[hoveredCard];
            if (isCardEnabled(card, data)) {
                openCardMenu(card.id);
                return true;
            }
        }

        // Quick action clicks
        if (hoveredQuickAction >= 0) {
            String actionId = QUICK_ACTION_IDS[hoveredQuickAction];
            boolean enabled = true;
            if ((actionId.equals("diplomacy") || actionId.equals("election")) && !data.hasNation()) {
                enabled = false;
            }
            if (enabled) {
                openQuickAction(actionId);
                return true;
            }
        }

        // Click outside closes
        if (mouseX < panelX || mouseX >= panelX + panelWidth ||
                mouseY < panelY || mouseY >= panelY + panelHeight) {
            close();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openCardMenu(String id) {
        switch (id) {
            case "economy" -> hegemonia.getScreenManager().openEconomyMenu();
            case "nation" -> hegemonia.getScreenManager().openNationMenu();
            case "war" -> hegemonia.getScreenManager().openWarMenu();
            case "market" -> hegemonia.getScreenManager().openMarketMenu();
            case "bank" -> hegemonia.getScreenManager().openBankMenu();
            case "territory" -> { if (client != null) client.setScreen(new TerritoryScreen()); }
        }
    }

    private void openQuickAction(String id) {
        switch (id) {
            case "diplomacy" -> { if (client != null) client.setScreen(new DiplomacyScreen()); }
            case "election" -> { /* TODO */ }
            case "leaderboard" -> { /* TODO */ }
            case "settings" -> hegemonia.getScreenManager().openSettingsMenu();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private float easeOutCubic(float t) {
        return 1 - (float) Math.pow(1 - t, 3);
    }

    private static class MenuCard {
        final String title;
        final String subtitle;
        final String id;
        final int color;
        final String icon;

        MenuCard(String title, String subtitle, String id, int color, String icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.id = id;
            this.color = color;
            this.icon = icon;
        }
    }
}
