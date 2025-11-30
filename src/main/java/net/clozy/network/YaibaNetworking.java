package net.clozy.network;

import net.clozy.logic.BreathingHandler;
import net.clozy.registry.Clan;
import net.clozy.registry.BreathingStyle;
import net.clozy.util.IClanAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class YaibaNetworking {
    public static final Identifier OPEN_SCREEN_PACKET = new Identifier("yaiba", "open_clan_screen");
    public static final Identifier SELECT_CLAN_PACKET = new Identifier("yaiba", "select_clan");
    public static final Identifier SYNC_REQUEST_PACKET = new Identifier("yaiba", "sync_request");

    public static final Identifier OPEN_BREATHING_SCREEN_PACKET = new Identifier("yaiba", "open_breathing_screen");
    public static final Identifier SELECT_BREATHING_PACKET = new Identifier("yaiba", "select_breathing");
    public static final Identifier CYCLE_ABILITY_PACKET = new Identifier("yaiba", "cycle_ability");
    public static final Identifier SYNC_DATA_PACKET = new Identifier("yaiba", "sync_data");
    public static final Identifier BREATHING_CHARGE_PACKET = new Identifier("yaiba", "breathing_charge");
    public static final Identifier UPGRADE_STAT_PACKET = new Identifier("yaiba", "upgrade_stat");

    // NEW: Use Ability Packet
    public static final Identifier USE_ABILITY_PACKET = new Identifier("yaiba", "use_ability");

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SELECT_CLAN_PACKET, YaibaNetworking::handleClanSelection);
        ServerPlayNetworking.registerGlobalReceiver(SYNC_REQUEST_PACKET, YaibaNetworking::handleSyncRequest);
        ServerPlayNetworking.registerGlobalReceiver(SELECT_BREATHING_PACKET, YaibaNetworking::handleBreathingSelection);
        ServerPlayNetworking.registerGlobalReceiver(CYCLE_ABILITY_PACKET, YaibaNetworking::handleAbilityCycle);
        ServerPlayNetworking.registerGlobalReceiver(BREATHING_CHARGE_PACKET, YaibaNetworking::handleBreathingCharge);
        ServerPlayNetworking.registerGlobalReceiver(UPGRADE_STAT_PACKET, YaibaNetworking::handleStatUpgrade);

        // Register Ability Usage
        ServerPlayNetworking.registerGlobalReceiver(USE_ABILITY_PACKET, YaibaNetworking::handleUseAbility);
    }

    // --- Packet Handlers ---

    private static void handleUseAbility(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            IClanAccessor accessor = (IClanAccessor) player;
            BreathingStyle style = accessor.getBreathingStyle();
            int index = accessor.getSelectedAbilityIndex();

            // Check if they have a style
            if (style != BreathingStyle.NONE) {
                // Execute logic via handler
                BreathingHandler.executeMove(player, style, index);
            }
        });
    }

    private static void handleBreathingCharge(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        boolean charging = buf.readBoolean();
        server.execute(() -> {
            ((IClanAccessor) player).setBreathingCharging(charging);
            syncDataToClient(player);
        });
    }

    private static void handleStatUpgrade(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        String statName = buf.readString();
        server.execute(() -> {
            ((IClanAccessor) player).upgradeStat(statName);
            syncDataToClient(player);
        });
    }

    private static void handleSyncRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            IClanAccessor accessor = (IClanAccessor) player;
            if (!accessor.hasSelectedClan()) {
                ServerPlayNetworking.send(player, OPEN_SCREEN_PACKET, PacketByteBufs.empty());
                return;
            }
            if (accessor.getBreathingStyle() == BreathingStyle.NONE) {
                ServerPlayNetworking.send(player, OPEN_BREATHING_SCREEN_PACKET, PacketByteBufs.empty());
                return;
            }
            syncDataToClient(player);
        });
    }

    private static void handleClanSelection(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        String clanName = buf.readString();
        server.execute(() -> {
            IClanAccessor accessor = (IClanAccessor) player;
            if (!accessor.hasSelectedClan()) {
                Clan selected = Clan.fromString(clanName);
                if (selected != Clan.NONE) {
                    accessor.setClan(selected);
                    player.sendMessage(Text.literal("You have joined the " + selected.getDisplayName() + " Clan.").formatted(selected.getColor()), true);
                    syncDataToClient(player);
                    ServerPlayNetworking.send(player, OPEN_BREATHING_SCREEN_PACKET, PacketByteBufs.empty());
                }
            }
        });
    }

    private static void handleBreathingSelection(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        String styleName = buf.readString();
        server.execute(() -> {
            IClanAccessor accessor = (IClanAccessor) player;
            BreathingStyle style = BreathingStyle.fromString(styleName);
            accessor.setBreathingStyle(style);
            player.sendMessage(Text.literal("You have learned " + style.getDisplayName()).formatted(style.getColor()), true);
            syncDataToClient(player);
        });
    }

    private static void handleAbilityCycle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            ((IClanAccessor) player).cycleAbility();
            syncDataToClient(player);
        });
    }

    public static void syncDataToClient(ServerPlayerEntity player) {
        IClanAccessor accessor = (IClanAccessor) player;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(accessor.getClan().name());
        buf.writeString(accessor.getBreathingStyle().name());
        buf.writeInt(accessor.getSelectedAbilityIndex());

        buf.writeInt(accessor.getStat("Vitality"));
        buf.writeInt(accessor.getStat("Strength"));
        buf.writeInt(accessor.getStat("Swordsmanship"));
        buf.writeInt(accessor.getStat("Agility"));
        buf.writeInt(accessor.getLevel());
        buf.writeInt(accessor.getExp());
        buf.writeInt(accessor.getSp());
        buf.writeFloat(accessor.getBreathingProgress());

        ServerPlayNetworking.send(player, SYNC_DATA_PACKET, buf);
    }
}