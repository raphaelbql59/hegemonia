package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import net.minecraft.client.gui.DrawContext;

import java.text.DecimalFormat;

/**
 * HEGEMONIA LEADERBOARD SCREEN
 *
 * Shows rankings for:
 * - Richest Players (wealth)
 * - Best Warriors (kills)
 * - Powerful Nations (power score)
 * - Largest Nations (members)
 */
public class LeaderboardScreen extends HegemoniaScreen {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");

    // Tab indices
    private static final int TAB_PLAYERS_WEALTH = 0;
    private static final int TAB_PLAYERS_KILLS = 1;
    private static final int TAB_NATIONS_POWER = 2;
    private static final int TAB_NATIONS_SIZE = 3;

    private int selectedTab = TAB_PLAYERS_WEALTH;
    private HegemoniaScrollPanel leaderboardPanel;

    // Tab button positions
    private int[] tabX = new int[4];
    private int tabWidth;
    private int tabY;

    // Colors for top 3
    private static final int GOLD_MEDAL = 0xFFFFD700;
    private static final int SILVER_MEDAL = 0xFFC0C0C0;
    private static final int BRONZE_MEDAL = 0xFFCD7F32;

    public LeaderboardScreen() {
        super("Classements");
    }

    @Override
    protected void calculatePanelSize() {
        panelWidth = Math.min(550, (int)(screenWidth * 0.85));
        panelHeight = Math.min(480, (int)(screenHeight * 0.85));
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;
    }

    @Override
    protected void initContent() {
        // Calculate tab positions
        int tabsStartX = contentX;
        int tabsWidth = contentWidth;
        tabWidth = (tabsWidth - 12) / 4;  // 4 tabs with spacing
        tabY = contentY + 4;

        for (int i = 0; i < 4; i++) {
            tabX[i] = tabsStartX + i * (tabWidth + 4);
        }

        // Leaderboard scroll panel
        int panelStartY = tabY + 36;
        leaderboardPanel = addWidget(new HegemoniaScrollPanel(
                contentX, panelStartY,
                contentWidth, contentHeight - 50
        ));
        leaderboardPanel.setPadding(8).setItemSpacing(4);

        // Refresh button
        addWidget(new HegemoniaButton(
                panelX + panelWidth - 110, panelY + panelHeight - 50,
                90, 32,
                "Actualiser",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> refreshLeaderboard()
        ));

        // Load initial data
        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        leaderboardPanel.clearChildren();

        switch (selectedTab) {
            case TAB_PLAYERS_WEALTH -> loadPlayerWealth();
            case TAB_PLAYERS_KILLS -> loadPlayerKills();
            case TAB_NATIONS_POWER -> loadNationPower();
            case TAB_NATIONS_SIZE -> loadNationSize();
        }
    }

    private void loadPlayerWealth() {
        // Placeholder data - real data from server
        String[][] data = {
                {"1", "Emperor_Maximus", "2,450,000"},
                {"2", "GoldKing99", "1,890,000"},
                {"3", "TradeMonarch", "1,234,567"},
                {"4", "WealthyWarrior", "987,654"},
                {"5", "RichNoble", "756,432"},
                {"6", "MerchantKing", "654,321"},
                {"7", "TreasureHunter", "543,210"},
                {"8", "GoldDigger", "432,109"},
                {"9", "CoinCollector", "321,098"},
                {"10", "SilverBaron", "210,987"},
        };

        for (String[] entry : data) {
            addLeaderboardEntry(entry[0], entry[1], entry[2] + " H", "player");
        }
    }

    private void loadPlayerKills() {
        String[][] data = {
                {"1", "WarMaster_X", "1,247"},
                {"2", "DeathBringer", "1,089"},
                {"3", "BattleLord", "956"},
                {"4", "ShadowKiller", "847"},
                {"5", "BloodWarrior", "723"},
                {"6", "DarkKnight", "654"},
                {"7", "StormBlade", "543"},
                {"8", "IronFist", "432"},
                {"9", "FlameStriker", "321"},
                {"10", "ThunderBolt", "210"},
        };

        for (String[] entry : data) {
            addLeaderboardEntry(entry[0], entry[1], entry[2] + " kills", "player");
        }
    }

    private void loadNationPower() {
        String[][] data = {
                {"1", "[ROM] Empire Romain", "15,420"},
                {"2", "[FRA] Royaume de France", "12,890"},
                {"3", "[BYZ] Empire Byzantin", "11,234"},
                {"4", "[MON] Horde Mongole", "9,876"},
                {"5", "[VEN] Republique de Venise", "8,765"},
                {"6", "[ENG] Kingdom of England", "7,654"},
                {"7", "[OTT] Empire Ottoman", "6,543"},
                {"8", "[ESP] Reino de Espana", "5,432"},
                {"9", "[HRE] Saint Empire", "4,321"},
                {"10", "[POL] Royaume de Pologne", "3,210"},
        };

        for (String[] entry : data) {
            addLeaderboardEntry(entry[0], entry[1], entry[2] + " pts", "nation");
        }
    }

    private void loadNationSize() {
        String[][] data = {
                {"1", "[ROM] Empire Romain", "48"},
                {"2", "[FRA] Royaume de France", "42"},
                {"3", "[MON] Horde Mongole", "38"},
                {"4", "[BYZ] Empire Byzantin", "35"},
                {"5", "[ENG] Kingdom of England", "31"},
                {"6", "[OTT] Empire Ottoman", "28"},
                {"7", "[VEN] Republique de Venise", "24"},
                {"8", "[ESP] Reino de Espana", "21"},
                {"9", "[HRE] Saint Empire", "18"},
                {"10", "[POL] Royaume de Pologne", "15"},
        };

        for (String[] entry : data) {
            addLeaderboardEntry(entry[0], entry[1], entry[2] + " membres", "nation");
        }
    }

    private void addLeaderboardEntry(String rank, String name, String value, String type) {
        int rankNum = Integer.parseInt(rank);

        // Medal color for top 3
        int accentColor = switch (rankNum) {
            case 1 -> GOLD_MEDAL;
            case 2 -> SILVER_MEDAL;
            case 3 -> BRONZE_MEDAL;
            default -> HegemoniaDesign.TEXT_MUTED;
        };

        // Medal icon
        String medalIcon = switch (rankNum) {
            case 1 -> "1";
            case 2 -> "2";
            case 3 -> "3";
            default -> rank;
        };

        int itemWidth = leaderboardPanel.getContentWidth() - 16;
        int itemHeight = 36;

        HegemoniaListItem item = new HegemoniaListItem(0, 0, itemWidth, itemHeight, name) {
            @Override
            public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
                super.render(ctx, mouseX, mouseY, delta);

                // Custom rank rendering
                int rankWidth = 32;

                // Rank box
                if (rankNum <= 3) {
                    ctx.fill(x + 4, y + 4, x + rankWidth + 4, y + height - 4, accentColor);
                    // Black text for visibility on gold/silver/bronze
                    ctx.drawCenteredTextWithShadow(textRenderer, medalIcon,
                            x + rankWidth / 2 + 4, y + (height - 8) / 2, 0xFF000000);
                } else {
                    ctx.fill(x + 4, y + 6, x + rankWidth + 4, y + height - 6,
                            HegemoniaDesign.BG_TERTIARY);
                    ctx.drawCenteredTextWithShadow(textRenderer, rank,
                            x + rankWidth / 2 + 4, y + (height - 8) / 2, HegemoniaDesign.TEXT_MUTED);
                }
            }
        };

        // Value on the right
        int valueColor = type.equals("nation") ? HegemoniaDesign.BLUE : HegemoniaDesign.GOLD;
        item.setRightText(value, valueColor);

        leaderboardPanel.addChild(item);
    }

    private void refreshLeaderboard() {
        // TODO: Request fresh data from server when API is ready
        // hegemonia.getNetworkHandler().requestLeaderboard(getTabType());
        loadLeaderboardData();
    }

    private String getTabType() {
        return switch (selectedTab) {
            case TAB_PLAYERS_WEALTH -> "player_wealth";
            case TAB_PLAYERS_KILLS -> "player_kills";
            case TAB_NATIONS_POWER -> "nation_power";
            case TAB_NATIONS_SIZE -> "nation_size";
            default -> "player_wealth";
        };
    }

    @Override
    protected void renderContent(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int alpha = (int)(255 * openAnim);

        // Render tabs
        String[] tabLabels = {"Richesse", "Guerriers", "Puissance", "Membres"};
        String[] tabIcons = {"$", "X", "P", "M"};  // Simple icons

        for (int i = 0; i < 4; i++) {
            boolean selected = (i == selectedTab);
            boolean hovered = mouseX >= tabX[i] && mouseX < tabX[i] + tabWidth &&
                    mouseY >= tabY && mouseY < tabY + 28;

            // Tab background
            int tabBg = selected ? HegemoniaDesign.GOLD :
                    (hovered ? HegemoniaDesign.BG_HOVER : HegemoniaDesign.BG_TERTIARY);
            ctx.fill(tabX[i], tabY, tabX[i] + tabWidth, tabY + 28,
                    HegemoniaDesign.withAlpha(tabBg, alpha));

            // Tab border
            if (selected) {
                ctx.fill(tabX[i], tabY + 26, tabX[i] + tabWidth, tabY + 28,
                        HegemoniaDesign.withAlpha(HegemoniaDesign.GOLD, alpha));
            }

            // Tab text
            int textColor = selected ? 0xFF000000 : HegemoniaDesign.TEXT_PRIMARY;
            ctx.drawCenteredTextWithShadow(textRenderer, tabLabels[i],
                    tabX[i] + tabWidth / 2, tabY + 10, HegemoniaDesign.withAlpha(textColor, alpha));
        }

        // Subtitle based on tab
        String subtitle = switch (selectedTab) {
            case TAB_PLAYERS_WEALTH -> "Top joueurs les plus riches";
            case TAB_PLAYERS_KILLS -> "Top joueurs par eliminations";
            case TAB_NATIONS_POWER -> "Nations les plus puissantes";
            case TAB_NATIONS_SIZE -> "Nations les plus grandes";
            default -> "";
        };

        ctx.drawText(textRenderer, subtitle,
                contentX + 8, tabY + 36,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);

        // Column headers
        int headerY = tabY + 52;
        ctx.drawText(textRenderer, "#", contentX + 18, headerY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);
        ctx.drawText(textRenderer, selectedTab >= 2 ? "Nation" : "Joueur",
                contentX + 50, headerY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);

        String valueHeader = switch (selectedTab) {
            case TAB_PLAYERS_WEALTH -> "Richesse";
            case TAB_PLAYERS_KILLS -> "Kills";
            case TAB_NATIONS_POWER -> "Points";
            case TAB_NATIONS_SIZE -> "Membres";
            default -> "Valeur";
        };
        int valueHeaderX = contentX + contentWidth - textRenderer.getWidth(valueHeader) - 24;
        ctx.drawText(textRenderer, valueHeader, valueHeaderX, headerY,
                HegemoniaDesign.withAlpha(HegemoniaDesign.TEXT_MUTED, alpha), false);

        // Divider line
        ctx.fill(contentX, headerY + 12, contentX + contentWidth, headerY + 13,
                HegemoniaDesign.withAlpha(HegemoniaDesign.BORDER_SUBTLE, alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check tab clicks
            for (int i = 0; i < 4; i++) {
                if (mouseX >= tabX[i] && mouseX < tabX[i] + tabWidth &&
                        mouseY >= tabY && mouseY < tabY + 28) {
                    if (selectedTab != i) {
                        selectedTab = i;
                        loadLeaderboardData();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
