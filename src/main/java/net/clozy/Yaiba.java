package net.clozy;

import net.clozy.command.YaibaCommands;
import net.clozy.network.YaibaNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Yaiba implements ModInitializer {
    public static final String MOD_ID = "yaiba";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Yaiba Mod");

        // Register Server Packets
        YaibaNetworking.registerServerPackets();

        // Register Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            YaibaCommands.register(dispatcher);
        });
    }
}