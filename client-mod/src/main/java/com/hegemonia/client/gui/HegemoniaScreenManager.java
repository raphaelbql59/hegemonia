package com.hegemonia.client.gui;

import com.hegemonia.client.HegemoniaClient;
import com.hegemonia.client.gui.screen.*;
import net.minecraft.client.MinecraftClient;

/**
 * Manages all Hegemonia screens and their transitions
 */
public class HegemoniaScreenManager {

    private final MinecraftClient client;

    public HegemoniaScreenManager() {
        this.client = MinecraftClient.getInstance();
    }

    /**
     * Opens the main Hegemonia menu
     */
    public void openMainMenu() {
        if (!isConnected()) return;
        client.setScreen(new MainMenuScreen());
    }

    /**
     * Opens the economy menu
     */
    public void openEconomyMenu() {
        if (!isConnected()) return;
        client.setScreen(new EconomyScreen());
    }

    /**
     * Opens the bank submenu
     */
    public void openBankMenu() {
        if (!isConnected()) return;
        client.setScreen(new BankScreen());
    }

    /**
     * Opens the market submenu
     */
    public void openMarketMenu() {
        if (!isConnected()) return;
        client.setScreen(new MarketScreen());
    }

    /**
     * Opens the nation menu
     */
    public void openNationMenu() {
        if (!isConnected()) return;
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();

        if (data.hasNation()) {
            client.setScreen(new NationScreen());
        } else {
            client.setScreen(new NationJoinScreen());
        }
    }

    /**
     * Opens the nation creation screen
     */
    public void openNationCreateScreen() {
        if (!isConnected()) return;
        client.setScreen(new NationCreateScreen());
    }

    /**
     * Opens the war menu
     */
    public void openWarMenu() {
        if (!isConnected()) return;
        client.setScreen(new WarScreen());
    }

    /**
     * Opens the settings screen
     */
    public void openSettingsMenu() {
        client.setScreen(new SettingsScreen());
    }

    /**
     * Closes the current screen and returns to game
     */
    public void closeScreen() {
        client.setScreen(null);
    }

    /**
     * Goes back to the main menu
     */
    public void goBack() {
        openMainMenu();
    }

    private boolean isConnected() {
        if (!HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia) {
            HegemoniaClient.LOGGER.warn("Tried to open menu but not connected to Hegemonia server");
            return false;
        }
        return true;
    }
}
