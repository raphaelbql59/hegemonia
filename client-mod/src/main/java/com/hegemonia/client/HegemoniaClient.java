package com.hegemonia.client;

import com.hegemonia.client.gui.HegemoniaScreenManager;
import com.hegemonia.client.hud.HegemoniaHud;
import com.hegemonia.client.network.HegemoniaNetworkHandler;
import com.hegemonia.client.util.HegemoniaKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hegemonia Client Mod
 *
 * Provides custom GUIs, HUD elements, and server integration
 * for the Hegemonia geopolitical Minecraft experience.
 */
@Environment(EnvType.CLIENT)
public class HegemoniaClient implements ClientModInitializer {

    public static final String MOD_ID = "hegemonia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static HegemoniaClient instance;

    // Managers
    private HegemoniaScreenManager screenManager;
    private HegemoniaHud hud;
    private HegemoniaNetworkHandler networkHandler;

    // Player data (synced from server)
    private PlayerData playerData = new PlayerData();

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("╔══════════════════════════════════════════╗");
        LOGGER.info("║     HEGEMONIA CLIENT - Initialisation    ║");
        LOGGER.info("╚══════════════════════════════════════════╝");

        // Initialize components
        initializeKeybinds();
        initializeNetwork();
        initializeScreens();
        initializeHud();

        LOGGER.info("✓ Hegemonia Client initialized successfully!");
    }

    private void initializeKeybinds() {
        HegemoniaKeybinds.register();
        LOGGER.info("✓ Keybinds registered");
    }

    private void initializeNetwork() {
        networkHandler = new HegemoniaNetworkHandler();
        networkHandler.register();
        LOGGER.info("✓ Network handler registered");
    }

    private void initializeScreens() {
        screenManager = new HegemoniaScreenManager();
        LOGGER.info("✓ Screen manager initialized");
    }

    private void initializeHud() {
        hud = new HegemoniaHud();
        hud.register();
        LOGGER.info("✓ HUD initialized");
    }

    // ==================== Getters ====================

    public static HegemoniaClient getInstance() {
        return instance;
    }

    public HegemoniaScreenManager getScreenManager() {
        return screenManager;
    }

    public HegemoniaHud getHud() {
        return hud;
    }

    public HegemoniaNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    /**
     * Player data synced from the server
     */
    public static class PlayerData {
        // Economy
        public double balance = 0;
        public double bankBalance = 0;

        // Nation
        public String nationName = null;
        public String nationTag = null;
        public String nationRole = null;
        public int nationId = -1;

        // War
        public boolean atWar = false;
        public String warTarget = null;

        // Status
        public boolean isConnectedToHegemonia = false;

        public double getTotalBalance() {
            return balance + bankBalance;
        }

        public boolean hasNation() {
            return nationId > 0 && nationName != null;
        }
    }
}
