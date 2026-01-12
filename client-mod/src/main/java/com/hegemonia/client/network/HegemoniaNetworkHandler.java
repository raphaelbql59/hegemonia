package com.hegemonia.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hegemonia.client.HegemoniaClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Handles network communication between client and Hegemonia server
 */
public class HegemoniaNetworkHandler {

    private static final Gson GSON = new Gson();

    // Channel identifiers
    public static final Identifier HEGEMONIA_HANDSHAKE = new Identifier("hegemonia", "handshake");
    public static final Identifier HEGEMONIA_SYNC = new Identifier("hegemonia", "sync");
    public static final Identifier HEGEMONIA_REQUEST = new Identifier("hegemonia", "request");
    public static final Identifier HEGEMONIA_ACTION = new Identifier("hegemonia", "action");
    public static final Identifier HEGEMONIA_NOTIFICATION = new Identifier("hegemonia", "notification");

    public void register() {
        // Register connection events
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Send handshake to server when joining
            sendHandshake();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // Reset player data on disconnect
            HegemoniaClient.getInstance().getPlayerData().isConnectedToHegemonia = false;
            HegemoniaClient.LOGGER.info("Disconnected from server");
        });

        // Register packet receivers
        registerReceivers();
    }

    private void registerReceivers() {
        // Handshake response - confirms this is a Hegemonia server
        ClientPlayNetworking.registerGlobalReceiver(HEGEMONIA_HANDSHAKE, (client, handler, buf, responseSender) -> {
            String serverVersion = buf.readString();
            boolean isHegemonia = buf.readBoolean();

            client.execute(() -> {
                HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();
                data.isConnectedToHegemonia = isHegemonia;

                if (isHegemonia) {
                    HegemoniaClient.LOGGER.info("Connected to Hegemonia server v{}", serverVersion);
                    // Request initial sync
                    requestFullSync();
                }
            });
        });

        // Sync packet - updates player data
        ClientPlayNetworking.registerGlobalReceiver(HEGEMONIA_SYNC, (client, handler, buf, responseSender) -> {
            String jsonData = buf.readString();

            client.execute(() -> {
                try {
                    JsonObject json = GSON.fromJson(jsonData, JsonObject.class);
                    updatePlayerData(json);
                } catch (Exception e) {
                    HegemoniaClient.LOGGER.error("Failed to parse sync data", e);
                }
            });
        });

        // Notification packet - shows notifications to player
        ClientPlayNetworking.registerGlobalReceiver(HEGEMONIA_NOTIFICATION, (client, handler, buf, responseSender) -> {
            String type = buf.readString();
            String title = buf.readString();
            String message = buf.readString();
            int duration = buf.readInt();

            client.execute(() -> {
                HegemoniaClient.getInstance().getHud().showNotification(type, title, message, duration);
            });
        });
    }

    private void updatePlayerData(JsonObject json) {
        HegemoniaClient.PlayerData data = HegemoniaClient.getInstance().getPlayerData();

        // Economy data
        if (json.has("balance")) {
            data.balance = json.get("balance").getAsDouble();
        }
        if (json.has("bankBalance")) {
            data.bankBalance = json.get("bankBalance").getAsDouble();
        }

        // Nation data
        if (json.has("nationName")) {
            data.nationName = json.get("nationName").isJsonNull() ? null : json.get("nationName").getAsString();
        }
        if (json.has("nationTag")) {
            data.nationTag = json.get("nationTag").isJsonNull() ? null : json.get("nationTag").getAsString();
        }
        if (json.has("nationRole")) {
            data.nationRole = json.get("nationRole").isJsonNull() ? null : json.get("nationRole").getAsString();
        }
        if (json.has("nationId")) {
            data.nationId = json.get("nationId").getAsInt();
        }

        // War data
        if (json.has("atWar")) {
            data.atWar = json.get("atWar").getAsBoolean();
        }
        if (json.has("warTarget")) {
            data.warTarget = json.get("warTarget").isJsonNull() ? null : json.get("warTarget").getAsString();
        }
    }

    // ==================== Send Methods ====================

    public void sendHandshake() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("1.0.0"); // Client mod version
        ClientPlayNetworking.send(HEGEMONIA_HANDSHAKE, buf);
    }

    public void requestFullSync() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString("full_sync");
        ClientPlayNetworking.send(HEGEMONIA_REQUEST, buf);
    }

    public void sendAction(String action, JsonObject data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(action);
        buf.writeString(GSON.toJson(data));
        ClientPlayNetworking.send(HEGEMONIA_ACTION, buf);
    }

    // Convenience methods for common actions

    public void requestBankDeposit(double amount) {
        JsonObject data = new JsonObject();
        data.addProperty("amount", amount);
        sendAction("bank_deposit", data);
    }

    public void requestBankWithdraw(double amount) {
        JsonObject data = new JsonObject();
        data.addProperty("amount", amount);
        sendAction("bank_withdraw", data);
    }

    public void requestNationInfo(int nationId) {
        JsonObject data = new JsonObject();
        data.addProperty("nationId", nationId);
        sendAction("nation_info", data);
    }

    public void requestCreateNation(String name, String tag) {
        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        data.addProperty("tag", tag);
        sendAction("create_nation", data);
    }

    public void requestJoinNation(int nationId) {
        JsonObject data = new JsonObject();
        data.addProperty("nationId", nationId);
        sendAction("join_nation", data);
    }

    public void requestLeaveNation() {
        sendAction("leave_nation", new JsonObject());
    }

    public void requestDeclareWar(int targetNationId) {
        JsonObject data = new JsonObject();
        data.addProperty("targetNationId", targetNationId);
        sendAction("declare_war", data);
    }

    public void requestMarketItems(String category) {
        JsonObject data = new JsonObject();
        data.addProperty("category", category);
        sendAction("market_list", data);
    }

    public void requestBuyItem(int listingId, int quantity) {
        JsonObject data = new JsonObject();
        data.addProperty("listingId", listingId);
        data.addProperty("quantity", quantity);
        sendAction("market_buy", data);
    }

    // ==================== Territory Methods ====================

    public void requestClaimChunk() {
        sendAction("claim_chunk", new JsonObject());
    }

    public void requestUnclaimChunk() {
        sendAction("unclaim_chunk", new JsonObject());
    }

    // ==================== War Methods ====================

    public void requestDeclareWar(String targetNation, String warGoal) {
        JsonObject data = new JsonObject();
        data.addProperty("targetNation", targetNation);
        data.addProperty("warGoal", warGoal);
        sendAction("declare_war_advanced", data);
    }

    public void requestSurrender() {
        sendAction("surrender", new JsonObject());
    }

    // ==================== Treasury Methods ====================

    public void requestTreasuryDeposit(double amount) {
        JsonObject data = new JsonObject();
        data.addProperty("amount", amount);
        sendAction("treasury_deposit", data);
    }

    public void requestTreasuryWithdraw(double amount) {
        JsonObject data = new JsonObject();
        data.addProperty("amount", amount);
        sendAction("treasury_withdraw", data);
    }

    // ==================== Transfer Methods ====================

    public void requestTransfer(String recipient, double amount) {
        JsonObject data = new JsonObject();
        data.addProperty("recipient", recipient);
        data.addProperty("amount", amount);
        sendAction("transfer_money", data);
    }

    // ==================== Diplomacy Methods ====================

    public void requestNationList() {
        sendAction("nation_list", new JsonObject());
    }

    public void requestProposeAlliance(int nationId) {
        JsonObject data = new JsonObject();
        data.addProperty("nationId", nationId);
        sendAction("propose_alliance", data);
    }

    public void requestDeclareEnemy(int nationId) {
        JsonObject data = new JsonObject();
        data.addProperty("nationId", nationId);
        sendAction("declare_enemy", data);
    }

    public void requestProposeTrade(int nationId) {
        JsonObject data = new JsonObject();
        data.addProperty("nationId", nationId);
        sendAction("propose_trade", data);
    }
}
