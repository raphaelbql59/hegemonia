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
 * Hegemonia HUD overlay - displays player info and notifications
 */
public class HegemoniaHud {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private boolean visible = true;
    private final List<Notification> notifications = new ArrayList<>();
    private final MinecraftClient client;

    // Animation
    private float animationProgress = 0f;
    private boolean animatingIn = true;

    public HegemoniaHud() {
        this.client = MinecraftClient.getInstance();
    }

    public void register() {
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext context, float tickDelta) {
        if (client.player == null) return;
        if (!HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia) return;
        if (client.options.hudHidden) return;

        // Update animation
        updateAnimation(tickDelta);

        if (!visible && animationProgress <= 0) return;

        // Render components
        renderPlayerInfo(context);
        renderNotifications(context, tickDelta);
    }

    private void updateAnimation(float delta) {
        float speed = 0.1f;
        if (visible && animatingIn) {
            animationProgress = Math.min(1f, animationProgress + speed);
        } else if (!visible) {
            animationProgress = Math.max(0f, animationProgress - speed);
            animatingIn = false;
        }
        if (visible) animatingIn = true;
    }

    private void renderPlayerInfo(DrawContext context) {
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();
        TextRenderer textRenderer = client.textRenderer;

        int x = 10;
        int y = 10;
        int padding = 8;
        int lineHeight = 12;

        // Calculate panel dimensions
        int panelWidth = 160;
        int lines = 2; // Balance + Bank
        if (data.hasNation()) lines += 2; // Nation + Role
        if (data.atWar) lines += 1; // War status
        int panelHeight = (lines * lineHeight) + (padding * 2);

        // Apply animation offset
        int animOffset = (int) ((1f - animationProgress) * -200);
        x += animOffset;

        // Draw panel background
        context.fill(x, y, x + panelWidth, y + panelHeight, HegemoniaDesign.PANEL_BACKGROUND);
        context.fill(x, y, x + 3, y + panelHeight, HegemoniaDesign.ACCENT_GOLD); // Left border

        // Draw content
        int contentX = x + padding + 3;
        int contentY = y + padding;

        // Balance
        String balanceText = "§6" + MONEY_FORMAT.format(data.balance) + " §7H";
        context.drawText(textRenderer, balanceText, contentX, contentY, 0xFFFFFF, true);
        contentY += lineHeight;

        // Bank balance
        String bankText = "§eBank: §f" + MONEY_FORMAT.format(data.bankBalance) + " §7H";
        context.drawText(textRenderer, bankText, contentX, contentY, 0xFFFFFF, true);
        contentY += lineHeight;

        // Nation info
        if (data.hasNation()) {
            String nationText = "§b[" + data.nationTag + "] §f" + data.nationName;
            context.drawText(textRenderer, nationText, contentX, contentY, 0xFFFFFF, true);
            contentY += lineHeight;

            String roleText = "§7Rang: §f" + data.nationRole;
            context.drawText(textRenderer, roleText, contentX, contentY, 0xFFFFFF, true);
            contentY += lineHeight;
        }

        // War status
        if (data.atWar) {
            String warText = "§c⚔ EN GUERRE: " + data.warTarget;
            context.drawText(textRenderer, warText, contentX, contentY, 0xFF5555, true);
        }
    }

    private void renderNotifications(DrawContext context, float delta) {
        if (notifications.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int y = 60;

        Iterator<Notification> it = notifications.iterator();
        while (it.hasNext()) {
            Notification notif = it.next();
            notif.update(delta);

            if (notif.isExpired()) {
                it.remove();
                continue;
            }

            int notifWidth = Math.max(
                    textRenderer.getWidth(notif.title),
                    textRenderer.getWidth(notif.message)
            ) + 20;
            int notifHeight = 40;
            int x = screenWidth - notifWidth - 10;

            // Animation
            float slideProgress = notif.getSlideProgress();
            x += (int) ((1f - slideProgress) * (notifWidth + 20));

            // Background color based on type
            int bgColor = switch (notif.type) {
                case "success" -> HegemoniaDesign.SUCCESS_DARK;
                case "error" -> HegemoniaDesign.ERROR_DARK;
                case "warning" -> HegemoniaDesign.WARNING_DARK;
                default -> HegemoniaDesign.INFO_DARK;
            };

            int borderColor = switch (notif.type) {
                case "success" -> HegemoniaDesign.SUCCESS;
                case "error" -> HegemoniaDesign.ERROR;
                case "warning" -> HegemoniaDesign.WARNING;
                default -> HegemoniaDesign.INFO;
            };

            // Draw notification
            context.fill(x, y, x + notifWidth, y + notifHeight, bgColor);
            context.fill(x, y, x + 3, y + notifHeight, borderColor);

            // Title
            context.drawText(textRenderer, notif.title, x + 10, y + 6, borderColor, true);

            // Message
            context.drawText(textRenderer, notif.message, x + 10, y + 20, 0xCCCCCC, true);

            y += notifHeight + 5;
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

    public void showNotification(String type, String title, String message, int durationTicks) {
        notifications.add(new Notification(type, title, message, durationTicks));
        // Limit to 5 notifications
        while (notifications.size() > 5) {
            notifications.remove(0);
        }
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
                slideIn = Math.min(1f, slideIn + 0.15f);
            }
        }

        boolean isExpired() {
            return age > duration;
        }

        float getSlideProgress() {
            if (age > duration - 20) {
                // Slide out
                return Math.max(0f, (duration - age) / 20f);
            }
            return slideIn;
        }
    }
}
