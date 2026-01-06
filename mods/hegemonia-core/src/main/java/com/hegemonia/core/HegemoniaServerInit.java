package com.hegemonia.core;

import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Server-side initialization
 */
public class HegemoniaServerInit implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        HegemoniaCore.LOGGER.info("Hegemonia Core - Server initialization");

        // Server-specific initialization
        // Load nations from database
        // Setup RCON listeners
        // Configure world settings
    }
}
