package com.hegemonia.client.hud;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * HEGEMONIA HUD v2.0
 *
 * Modern overlay with player stats and notifications.
 * Noir/Or theme with smooth animations.
 *
 * Top-left: Compact info bar with Balance, Bank, Nation, War
 * Right: Sliding notifications
 */
public class HegemoniaHud {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private boolean visible = true;
    private final List<Notification> notifications = new ArrayList<>();
    private final MinecraftClient client;

    // Animation
    private float openAnim = 0f;
    private float pulseAnim = 0f;

    // Hover states for interactive elements
    private boolean balanceHovered = false;
    private boolean bankHovered = false;
    private boolean nationHovered = false;

    public HegemoniaHud() {
        this.client = MinecraftClient.getInstance();
    }

    public void register() {
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext ctx, float tickDelta) {
        if (client.player == null) return;
        if (!HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia) return;
        if (client.options.hudHidden) return;

        // Update animations
        updateAnimations(tickDelta);

        if (!visible && openAnim <= 0.01f) return;

        float ease = HegemoniaDesign.easeOut(openAnim);

        // Render components
        renderInfoBar(ctx, ease);
        renderNotifications(ctx, tickDelta, ease);
    }

    private void updateAnimations(float delta) {
        // Open animation
        float target = visible ? 1f : 0f;
        openAnim += (target - openAnim) * HegemoniaDesign.ANIM_NORMAL;

        // Pulse animation (for war indicator)
        pulseAnim += 0.05f;
        if (pulseAnim > Math.PI * 2) pulseAnim -= Math.PI * 2;
    }

    /**
     * Render the main info bar (top-left)
     */
    private void renderInfoBar(DrawContext ctx, float ease) {
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();
        TextRenderer textRenderer = client.textRenderer;

        int x = 8;
        int y = 8;

        // Animate in from left
        int animOffset = (int) ((1f - ease) * -100);
        x += animOffset;

        int alpha = (int) (255 * ease);

        // Calculate total width needed
        int contentWidth = 0;

        String balanceText = MONEY_FORMAT.format(data.balance) + " H";
        String bankText = MONEY_FORMAT.format(data.bankBalance) + " H";

        contentWidth += textRenderer.getWidth("$") + 4 + textRenderer.getWidth(balanceText) + 12;
        contentWidth += textRenderer.getWidth("B") + 4 + textRenderer.getWidth(bankText);

        if (data.hasNation()) {
            String nationTag = "[" + data.nationTag + "]";
            contentWidth += 16 + textRenderer.getWidth(nationTag);

            if (data.atWar) {
                contentWidth += 16 + textRenderer.getWidth("GUERRE");
            }
        }

        int panelWidth = contentWidth + 20;
        int panelHeight = 24;

        // Background with gold accent border
        ctx.fill(x, y, x + panelWidth, y + panelHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_PRIMARY, alpha));
        ctx.fill(x, y, x + panelWidth, y + 2,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
        ctx.fill(x, y, x + 2, y + panelHeight,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));

        // Content
        int cx = x + 10;
        int cy = y + 8;

        // === Balance ===
        // Icon
        ctx.drawText(textRenderer, "$", cx, cy,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), false);
        cx += textRenderer.getWidth("$") + 4;

        // Value
        ctx.drawText(textRenderer, balanceText, cx, cy,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_PRIMARY, alpha), false);
        cx += textRenderer.getWidth(balanceText) + 6;

        // Separator
        ctx.fill(cx, y + 6, cx + 1, y + panelHeight - 6,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
        cx += 6;

        // === Bank ===
        ctx.drawText(textRenderer, "B", cx, cy,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
        cx += textRenderer.getWidth("B") + 4;

        ctx.drawText(textRenderer, bankText, cx, cy,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_PRIMARY, alpha), false);
        cx += textRenderer.getWidth(bankText);

        // === Nation (if has one) ===
        if (data.hasNation()) {
            // Separator
            cx += 6;
            ctx.fill(cx, y + 6, cx + 1, y + panelHeight - 6,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
            cx += 10;

            String nationTag = "[" + data.nationTag + "]";
            ctx.drawText(textRenderer, nationTag, cx, cy,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.BLUE, alpha), false);
            cx += textRenderer.getWidth(nationTag);

            // === War status (if at war) ===
            if (data.atWar) {
                cx += 6;
                ctx.fill(cx, y + 6, cx + 1, y + panelHeight - 6,
                        HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));
                cx += 10;

                // Pulsing war indicator
                float pulse = (float) (0.7f + 0.3f * Math.sin(pulseAnim * 3));
                int warAlpha = (int) (alpha * pulse);

                ctx.drawText(textRenderer, "GUERRE", cx, cy,
                        HegemoniaDesign.withAlpha(HegemoniaDesign.ERROR, warAlpha), false);
            }
        }
    }

    /**
     * Render sliding notifications (right side)
     */
    private void renderNotifications(DrawContext ctx, float delta, float ease) {
        if (notifications.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int y = 8;

        Iterator<Notification> it = notifications.iterator();
        while (it.hasNext()) {
            Notification notif = it.next();
            notif.update(delta);

            if (notif.isExpired()) {
                it.remove();
                continue;
            }

            // Calculate dimensions
            int titleWidth = textRenderer.getWidth(notif.title);
            int msgWidth = textRenderer.getWidth(notif.message);
            int notifWidth = Math.max(titleWidth, msgWidth) + 24;
            int notifHeight = 36;

            int x = screenWidth - notifWidth - 8;

            // Slide animation
            float slideProgress = notif.getSlideProgress() * ease;
            x += (int) ((1f - slideProgress) * (notifWidth + 20));

            int alpha = (int) (255 * slideProgress);

            // Colors based on type
            int bgColor, accentColor;
            switch (notif.type) {
                case "success" -> {
                    bgColor = HegemoniaDesign.SUCCESS_DARK;
                    accentColor = HegemoniaDesign.SUCCESS;
                }
                case "error" -> {
                    bgColor = HegemoniaDesign.ERROR_DARK;
                    accentColor = HegemoniaDesign.ERROR;
                }
                case "warning" -> {
                    bgColor = HegemoniaDesign.WARNING_DARK;
                    accentColor = HegemoniaDesign.WARNING;
                }
                default -> {
                    bgColor = HegemoniaDesign.INFO_DARK;
                    accentColor = HegemoniaDesign.INFO;
                }
            }

            // Background
            ctx.fill(x, y, x + notifWidth, y + notifHeight,
                    HegemoniaDesign.withAlpha(bgColor, alpha));

            // Left accent bar
            ctx.fill(x, y, x + 3, y + notifHeight,
                    HegemoniaDesign.withAlpha(accentColor, alpha));

            // Top line
            ctx.fill(x, y, x + notifWidth, y + 1,
                    HegemoniaDesign.withAlpha(accentColor, alpha));

            // Title
            ctx.drawText(textRenderer, notif.title, x + 10, y + 6,
                    HegemoniaDesign.withAlpha(accentColor, alpha), false);

            // Message
            ctx.drawText(textRenderer, notif.message, x + 10, y + 20,
                    HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_SECONDARY, alpha), false);

            y += notifHeight + 4;
        }
    }

    // ==================== Public Methods ====================

    public void toggleVisibility() {
        visible = !visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Show a notification
     * @param type "success", "error", "warning", or "info"
     * @param title Notification title
     * @param message Notification message
     * @param durationTicks How long to show (20 ticks = 1 second)
     */
    public void showNotification(String type, String title, String message, int durationTicks) {
        notifications.add(new Notification(type, title, message, durationTicks));
        // Limit to 5 notifications
        while (notifications.size() > 5) {
            notifications.remove(0);
        }
    }

    // Convenience methods
    public void notifySuccess(String title, String message) {
        showNotification("success", title, message, 100);
    }

    public void notifyError(String title, String message) {
        showNotification("error", title, message, 100);
    }

    public void notifyWarning(String title, String message) {
        showNotification("warning", title, message, 100);
    }

    public void notifyInfo(String title, String message) {
        showNotification("info", title, message, 100);
    }

    // ==================== Notification Class ====================

    private static class Notification {
        final String type;
        final String title;
        final String message;
        final int duration;
        int age = 0;
        float slideIn = 0f;

        Notification(String type, String title, String message, int duration) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.duration = duration;
        }

        void update(float delta) {
            age++;
            if (slideIn < 1f) {
                slideIn = Math.min(1f, slideIn + HegemoniaDesign.ANIM_FAST);
            }
        }

        boolean isExpired() {
            return age > duration;
        }

        float getSlideProgress() {
            if (age > duration - 20) {
                // Slide out during last 20 ticks
                float progress = (duration - age) / 20f;
                return Math.max(0f, HegemoniaDesign.easeOut(progress));
            }
            return HegemoniaDesign.easeOut(slideIn);
        }
    }
}
