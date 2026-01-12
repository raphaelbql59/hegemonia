package com.hegemonia.client.util;

import com.hegemonia.client.HegemoniaClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Manages all Hegemonia keybindings
 */
public class HegemoniaKeybinds {

    // Main menu key (default: H)
    public static KeyBinding OPEN_MAIN_MENU;

    // Economy key (default: E + Shift)
    public static KeyBinding OPEN_ECONOMY;

    // Nation key (default: N)
    public static KeyBinding OPEN_NATION;

    // War key (default: W + Shift)
    public static KeyBinding OPEN_WAR;

    // Toggle HUD (default: F6)
    public static KeyBinding TOGGLE_HUD;

    public static void register() {
        // Main menu - H key
        OPEN_MAIN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hegemonia.main_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.hegemonia"
        ));

        // Economy menu - Y key (near inventory key)
        OPEN_ECONOMY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hegemonia.economy",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.hegemonia"
        ));

        // Nation menu - N key
        OPEN_NATION = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hegemonia.nation",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.hegemonia"
        ));

        // War menu - J key
        OPEN_WAR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hegemonia.war",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.hegemonia"
        ));

        // Toggle HUD - F6 key
        TOGGLE_HUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hegemonia.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "category.hegemonia"
        ));

        // Register tick event to handle key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (client.currentScreen != null) return; // Don't open if screen is already open

            // Auto-detect Hegemonia server by checking for server brand or specific conditions
            // For now, enable menus on any multiplayer server for testing
            var playerData = HegemoniaClient.getInstance().getPlayerData();
            if (client.getCurrentServerEntry() != null && !playerData.isConnectedToHegemonia) {
                // Auto-enable when on multiplayer
                playerData.isConnectedToHegemonia = true;
                HegemoniaClient.LOGGER.info("Connected to server, enabling Hegemonia features");
            }

            while (OPEN_MAIN_MENU.wasPressed()) {
                HegemoniaClient.getInstance().getScreenManager().openMainMenu();
            }

            while (OPEN_ECONOMY.wasPressed()) {
                HegemoniaClient.getInstance().getScreenManager().openEconomyMenu();
            }

            while (OPEN_NATION.wasPressed()) {
                HegemoniaClient.getInstance().getScreenManager().openNationMenu();
            }

            while (OPEN_WAR.wasPressed()) {
                HegemoniaClient.getInstance().getScreenManager().openWarMenu();
            }

            while (TOGGLE_HUD.wasPressed()) {
                HegemoniaClient.getInstance().getHud().toggleVisibility();
            }
        });
    }
}
