package net.clozy;

import net.clozy.client.overlay.AbilityOverlay;
import net.clozy.client.screen.BreathingSelectionScreen;
import net.clozy.client.screen.ClanSelectionScreen;
import net.clozy.client.screen.StatScreen;
import net.clozy.network.YaibaNetworking;
import net.clozy.registry.Clan;
import net.clozy.registry.BreathingStyle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf; // <--- This was missing
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class YaibaClient implements ClientModInitializer {

    private boolean requestSync = false;
    private int tickCounter = 0;

    // Keybinds
    private static KeyBinding switchAbilityKey;
    private static KeyBinding breathingChargeKey;
    private static KeyBinding openMenuKey;

    // NEW: Use Ability Key (Default: V)
    private static KeyBinding useAbilityKey;

    private boolean wasCharging = false;

    @Override
    public void onInitializeClient() {
        Yaiba.LOGGER.info("Yaiba Client Initializer Started!");

        // 1. Screens
        ClientPlayNetworking.registerGlobalReceiver(YaibaNetworking.OPEN_SCREEN_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new ClanSelectionScreen()));
        });
        ClientPlayNetworking.registerGlobalReceiver(YaibaNetworking.OPEN_BREATHING_SCREEN_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new BreathingSelectionScreen()));
        });

        // 2. Data Sync
        ClientPlayNetworking.registerGlobalReceiver(YaibaNetworking.SYNC_DATA_PACKET, (client, handler, buf, responseSender) -> {
            String clanName = buf.readString();
            String breathName = buf.readString();
            int index = buf.readInt();

            // Stats
            int vit = buf.readInt();
            int str = buf.readInt();
            int swd = buf.readInt();
            int agi = buf.readInt();
            int lvl = buf.readInt();
            int xp = buf.readInt();
            int sp = buf.readInt();
            float prog = buf.readFloat();

            client.execute(() -> {
                AbilityOverlay.currentStyle = BreathingStyle.fromString(breathName);
                AbilityOverlay.abilityIndex = index;
                AbilityOverlay.swordsmanship = swd;
                AbilityOverlay.breathingProgress = prog;

                StatScreen.clan = Clan.fromString(clanName);
                StatScreen.style = BreathingStyle.fromString(breathName);
                StatScreen.vitality = vit;
                StatScreen.strength = str;
                StatScreen.swordsmanship = swd;
                StatScreen.agility = agi;
                StatScreen.level = lvl;
                StatScreen.exp = xp;
                StatScreen.sp = sp;
            });
        });

        // 3. HUD
        HudRenderCallback.EVENT.register(new AbilityOverlay());

        // 4. Keybinds
        switchAbilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.yaiba.switch_ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.yaiba"));
        breathingChargeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.yaiba.breathing_charge", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, "category.yaiba"));
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.yaiba.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.yaiba"));

        // Register V key
        useAbilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.yaiba.use_ability", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.yaiba"));

        // 5. Ticks & Input
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // R: Switch Ability
            while (switchAbilityKey.wasPressed()) {
                ClientPlayNetworking.send(YaibaNetworking.CYCLE_ABILITY_PACKET, PacketByteBufs.empty());
            }

            // M: Open Menu
            while (openMenuKey.wasPressed()) {
                client.setScreen(new StatScreen());
            }

            // V: Use Ability
            while (useAbilityKey.wasPressed()) {
                ClientPlayNetworking.send(YaibaNetworking.USE_ABILITY_PACKET, PacketByteBufs.empty());
            }

            // X: Breathing Charge (Hold logic)
            boolean isCharging = breathingChargeKey.isPressed();
            if (isCharging != wasCharging) {
                // Ensure PacketByteBuf is imported for this to work
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(isCharging);
                ClientPlayNetworking.send(YaibaNetworking.BREATHING_CHARGE_PACKET, buf);
                wasCharging = isCharging;
            }

            // Sync
            if (requestSync) {
                tickCounter++;
                if (tickCounter >= 20) {
                    ClientPlayNetworking.send(YaibaNetworking.SYNC_REQUEST_PACKET, PacketByteBufs.empty());
                    requestSync = false;
                    tickCounter = 0;
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            requestSync = true;
            tickCounter = 0;
            client.player.sendMessage(Text.literal("[Yaiba] Client Loaded.").formatted(Formatting.GREEN), false);
        });
    }
}