package com.hegemonia.client.gui.screen;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.theme.HegemoniaDesign;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * Economy overview screen
 * Modern design with card layout
 */
public class EconomyScreen extends HegemoniaScreen {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    // Button states
    private int hoveredButton = -1;

    public EconomyScreen() {
        super("ECONOMIE");
    }

    @Override
    protected void calculatePanelSize() {
        panelWidth = Math.min(520, (int)(screenWidth * 0.85));
        panelHeight = Math.min(380, (int)(screenHeight * 0.85));
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
    }

    @Override
    protected void initContent() {
        // Content initialized in render
    }

    @Override
    protected void renderContent(DrawContext ctx, int mouseX, int mouseY, float delta) {
        HegemoniaClient.PlayerData data = hegemonia.getPlayerData();
        float anim = HegemoniaDesign.easeOut(openAnim);
        int alpha = (int)(255 * anim);

        int cardWidth = (contentWidth - HegemoniaDesign.SPACE_MD) / 2;
        int cardHeight = 80;

        // ═══════════════════════════════════════════════════════════════
        // WALLET CARD
        // ═══════════════════════════════════════════════════════════════
        int walletX = contentX;
        int walletY = contentY;
        boolean walletHovered = mouseX >= walletX && mouseX < walletX + cardWidth &&
                mouseY >= walletY && mouseY < walletY + cardHeight;

        drawStatCard(ctx, walletX, walletY, cardWidth, cardHeight,
                "PORTEFEUILLE", "Argent liquide",
                MONEY_FORMAT.format(data.balance) + " H",
                HegemoniaDesign.GOLD, walletHovered, alpha);

        // ═══════════════════════════════════════════════════════════════
        // BANK CARD
        // ═══════════════════════════════════════════════════════════════
        int bankX = contentX + cardWidth + HegemoniaDesign.SPACE_MD;
        int bankY = contentY;
        boolean bankHovered = mouseX >= bankX && mouseX < bankX + cardWidth &&
                mouseY >= bankY && mouseY < bankY + cardHeight;

        drawStatCard(ctx, bankX, bankY, cardWidth, cardHeight,
                "BANQUE", "Epargne securisee",
                MONEY_FORMAT.format(data.bankBalance) + " H",
                HegemoniaDesign.INFO, bankHovered, alpha);

        // ═══════════════════════════════════════════════════════════════
        // TOTAL WEALTH BAR
        // ═══════════════════════════════════════════════════════════════
        int totalY = walletY + cardHeight + HegemoniaDesign.SPACE_MD;
        ctx.fill(contentX, totalY, contentX + contentWidth, totalY + 36,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BG_CARD, alpha));
        HegemoniaDesign.drawBorder(ctx, contentX, totalY, contentWidth, 36,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_DEFAULT, alpha));

        String totalLabel = "Fortune totale:";
        String totalValue = MONEY_FORMAT.format(data.getTotalBalance()) + " Hegemonia";
        int totalLabelW = textRenderer.getWidth(totalLabel);
        int totalValueW = textRenderer.getWidth(totalValue);

        ctx.drawText(textRenderer, totalLabel,
                contentX + HegemoniaDesign.SPACE_MD, totalY + 14,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_SECONDARY, alpha), false);
        ctx.drawText(textRenderer, totalValue,
                contentX + contentWidth - totalValueW - HegemoniaDesign.SPACE_MD, totalY + 14,
                HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha), true);

        // ═══════════════════════════════════════════════════════════════
        // ACTION BUTTONS
        // ═══════════════════════════════════════════════════════════════
        int btnY = totalY + 36 + HegemoniaDesign.SPACE_LG;
        int btnWidth = (contentWidth - HegemoniaDesign.SPACE_MD) / 2;
        int btnHeight = HegemoniaDesign.BUTTON_HEIGHT_MD;

        hoveredButton = -1;

        // Bank button
        boolean bankBtnHovered = mouseX >= contentX && mouseX < contentX + btnWidth &&
                mouseY >= btnY && mouseY < btnY + btnHeight;
        if (bankBtnHovered) hoveredButton = 0;
        drawActionButton(ctx, contentX, btnY, btnWidth, btnHeight,
                "Acceder a la Banque", HegemoniaDesign.INFO, bankBtnHovered, alpha);

        // Market button
        int marketBtnX = contentX + btnWidth + HegemoniaDesign.SPACE_MD;
        boolean marketBtnHovered = mouseX >= marketBtnX && mouseX < marketBtnX + btnWidth &&
                mouseY >= btnY && mouseY < btnY + btnHeight;
        if (marketBtnHovered) hoveredButton = 1;
        drawActionButton(ctx, marketBtnX, btnY, btnWidth, btnHeight,
                "Marche", HegemoniaDesign.WARNING, marketBtnHovered, alpha);

        // Second row
        int btn2Y = btnY + btnHeight + HegemoniaDesign.SPACE_SM;

        // Transfer button
        boolean transferHovered = mouseX >= contentX && mouseX < contentX + btnWidth &&
                mouseY >= btn2Y && mouseY < btn2Y + btnHeight;
        if (transferHovered) hoveredButton = 2;
        drawActionButton(ctx, contentX, btn2Y, btnWidth, btnHeight,
                "Transferer", HegemoniaDesign.BLUE, transferHovered, alpha);

        // History button
        boolean historyHovered = mouseX >= marketBtnX && mouseX < marketBtnX + btnWidth &&
                mouseY >= btn2Y && mouseY < btn2Y + btnHeight;
        if (historyHovered) hoveredButton = 3;
        drawActionButton(ctx, marketBtnX, btn2Y, btnWidth, btnHeight,
                "Historique", HegemoniaDesign.TEXT_SECONDARY, historyHovered, alpha);
    }

    private void drawStatCard(DrawContext ctx, int x, int y, int w, int h,
                               String title, String subtitle, String value,
                               int accentColor, boolean hovered, int alpha) {
        // Background
        int bgColor = hovered ? HegemoniaDesign.BG_CARD_HOVER : HegemoniaDesign.BG_CARD;
        ctx.fill(x, y, x + w, y + h, HegemoniaDesign.withAlpha(bgColor, alpha));

        // Border
        int borderColor = hovered ? accentColor : HegemoniaDesign.BORDER_DEFAULT;
        HegemoniaDesign.drawBorder(ctx, x, y, w, h, HegemoniaDesign.withAlpha(borderColor, alpha));

        // Accent bar
        ctx.fill(x, y + 6, x + 3, y + h - 6, HegemoniaDesign.withAlpha(accentColor, alpha));

        // Glow on hover
        if (hovered) {
            HegemoniaDesign.drawGlow(ctx, x, y, w, h, accentColor);
        }

        // Title
        ctx.drawText(textRenderer, title,
                x + HegemoniaDesign.SPACE_MD, y + HegemoniaDesign.SPACE_SM,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_PRIMARY, alpha), false);

        // Subtitle
        ctx.drawText(textRenderer, subtitle,
                x + HegemoniaDesign.SPACE_MD, y + HegemoniaDesign.SPACE_SM + 12,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);

        // Value
        ctx.drawText(textRenderer, value,
                x + HegemoniaDesign.SPACE_MD, y + h - 20,
                HegemoniaDesign.withAlpha(accentColor, alpha), true);
    }

    private void drawActionButton(DrawContext ctx, int x, int y, int w, int h,
                                   String text, int accentColor, boolean hovered, int alpha) {
        // Background
        int bgColor = hovered ? HegemoniaDesign.BG_BUTTON_HOVER : HegemoniaDesign.BG_BUTTON;
        ctx.fill(x, y, x + w, y + h, HegemoniaDesign.withAlpha(bgColor, alpha));

        // Border
        int borderColor = hovered ? accentColor : HegemoniaDesign.BORDER_DEFAULT;
        HegemoniaDesign.drawBorder(ctx, x, y, w, h, HegemoniaDesign.withAlpha(borderColor, alpha));

        // Text
        int textW = textRenderer.getWidth(text);
        int textColor = hovered ? accentColor : HegemoniaDesign.TEXT_SECONDARY;
        ctx.drawText(textRenderer, text,
                x + (w - textW) / 2, y + (h - 8) / 2,
                HegemoniaDesign.withAlpha(textColor, alpha), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredButton >= 0) {
            switch (hoveredButton) {
                case 0 -> hegemonia.getScreenManager().openBankMenu();
                case 1 -> hegemonia.getScreenManager().openMarketMenu();
                case 2 -> navigateTo(new TransferScreen());
                case 3 -> navigateTo(new TransactionHistoryScreen());
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
