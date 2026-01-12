package com.hegemonia.client.gui.screen;

import com.hegemonia.client.gui.theme.HegemoniaDesign;
import com.hegemonia.client.gui.widget.HegemoniaButton;
import com.hegemonia.client.gui.widget.HegemoniaListItem;
import com.hegemonia.client.gui.widget.HegemoniaScrollPanel;
import com.hegemonia.client.gui.widget.HegemoniaWidget;
import net.minecraft.client.gui.DrawContext;

/**
 * Market screen for buying and selling items
 */
public class MarketScreen extends HegemoniaScreen {

    private HegemoniaScrollPanel categoryPanel;
    private HegemoniaScrollPanel itemsPanel;
    private String selectedCategory = "RESOURCES";

    private static final String[] CATEGORIES = {
            "RESOURCES", "BUILDING", "FOOD", "WEAPONS", "TOOLS", "MISC"
    };

    private static final String[] CATEGORY_ICONS = {
            "⛏", "🏠", "🍖", "⚔", "🔧", "📦"
    };

    private static final String[] CATEGORY_NAMES = {
            "Ressources", "Construction", "Nourriture", "Armes", "Outils", "Divers"
    };

    public MarketScreen() {
        super("Marché");
    }

    @Override
    protected void calculateContentArea() {
        contentWidth = Math.min(600, (int) (screenWidth * 0.85));
        contentHeight = Math.min(450, (int) (screenHeight * 0.8));
        contentX = (screenWidth - contentWidth) / 2;
        contentY = (screenHeight - contentHeight) / 2;
    }

    @Override
    protected void initContent() {
        int sidebarWidth = 150;

        // Category sidebar
        categoryPanel = addWidget(new HegemoniaScrollPanel(
                contentX + 10, contentY + 45,
                sidebarWidth, contentHeight - 100
        ));
        categoryPanel.setPadding(5).setItemSpacing(2);

        for (int i = 0; i < CATEGORIES.length; i++) {
            final String category = CATEGORIES[i];
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, sidebarWidth - 20, 35,
                    CATEGORY_ICONS[i] + "  " + CATEGORY_NAMES[i]
            );
            item.setSelected(category.equals(selectedCategory));
            item.setOnClick(btn -> selectCategory(category));
            categoryPanel.addChild(item);
        }

        // Items panel
        itemsPanel = addWidget(new HegemoniaScrollPanel(
                contentX + sidebarWidth + 20, contentY + 45,
                contentWidth - sidebarWidth - 35, contentHeight - 100
        ));
        itemsPanel.setPadding(5).setItemSpacing(4);

        // Load items for selected category
        loadCategoryItems();

        // Back button
        addWidget(new HegemoniaButton(
                contentX + 10, contentY + contentHeight - 45,
                100, 30,
                "← Retour",
                HegemoniaButton.ButtonStyle.GHOST,
                btn -> hegemonia.getScreenManager().openEconomyMenu()
        ));

        // Sell button
        addWidget(new HegemoniaButton(
                contentX + contentWidth - 160, contentY + contentHeight - 45,
                150, 30,
                "📤 Vendre des items",
                HegemoniaButton.ButtonStyle.PRIMARY,
                btn -> {} // TODO: Sell screen
        ));
    }

    private void selectCategory(String category) {
        selectedCategory = category;

        // Update category selection
        for (HegemoniaWidget widget : categoryPanel.getChildren()) {
            if (widget instanceof HegemoniaListItem item) {
                item.setSelected(item.getTitle().contains(getCategoryName(category)));
            }
        }

        loadCategoryItems();
    }

    private String getCategoryName(String category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) {
                return CATEGORY_NAMES[i];
            }
        }
        return category;
    }

    private void loadCategoryItems() {
        itemsPanel.clearChildren();

        // Request items from server
        hegemonia.getNetworkHandler().requestMarketItems(selectedCategory);

        // Add placeholder items (will be replaced by server data)
        addPlaceholderItems();
    }

    private void addPlaceholderItems() {
        // Placeholder items - in real implementation, these come from server
        String[][] placeholders = {
                {"Diamant", "x64", "§e1,280 H", "Vendeur123"},
                {"Fer (lingot)", "x128", "§e256 H", "Mineur_Pro"},
                {"Or (lingot)", "x32", "§e480 H", "GoldMaster"},
                {"Redstone", "x256", "§e128 H", "TechPlayer"},
                {"Lapis Lazuli", "x64", "§e64 H", "BlueGem"},
        };

        int itemWidth = itemsPanel.getContentWidth() - 10;

        for (String[] data : placeholders) {
            HegemoniaListItem item = new HegemoniaListItem(
                    0, 0, itemWidth, 45,
                    data[0], "§7" + data[1] + " - Vendeur: §f" + data[3]
            );
            item.setRightText(data[2], HegemoniaDesign.MONEY_NEUTRAL);
            item.setOnClick(btn -> {
                // TODO: Open purchase dialog
            });
            itemsPanel.addChild(item);
        }
    }

    @Override
    protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
        // Category header
        int sidebarWidth = 150;
        context.drawText(textRenderer, "§7Catégories", contentX + 15, contentY + 35, HegemoniaDesign.TEXT_MUTED, false);

        // Items header
        context.drawText(textRenderer, "§7Annonces - " + getCategoryName(selectedCategory),
                contentX + sidebarWidth + 25, contentY + 35, HegemoniaDesign.TEXT_MUTED, false);
    }
}
