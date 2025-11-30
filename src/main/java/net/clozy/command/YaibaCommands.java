package net.clozy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.clozy.network.YaibaNetworking;
import net.clozy.registry.Clan;
import net.clozy.registry.BreathingStyle;
import net.clozy.util.IClanAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class YaibaCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Reset Command
        dispatcher.register(CommandManager.literal("yaiba_reset")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof IClanAccessor accessor && source.getPlayer() != null) {
                        accessor.setClan(Clan.NONE);
                        accessor.setBreathingStyle(BreathingStyle.NONE);
                        source.sendMessage(Text.literal("Reset complete. Opening Clan UI..."));
                        ServerPlayNetworking.send(source.getPlayer(), YaibaNetworking.OPEN_SCREEN_PACKET, PacketByteBufs.empty());
                        return 1;
                    }
                    return 0;
                }));

        // Select Breathing Debug
        dispatcher.register(CommandManager.literal("select_breathing")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getPlayer() != null) {
                        ServerPlayNetworking.send(source.getPlayer(), YaibaNetworking.OPEN_BREATHING_SCREEN_PACKET, PacketByteBufs.empty());
                        return 1;
                    }
                    return 0;
                }));

        // NEW: EXP Command
        dispatcher.register(CommandManager.literal("yaiba_exp")
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            int amount = IntegerArgumentType.getInteger(context, "amount");

                            if (source.getEntity() instanceof IClanAccessor accessor && source.getPlayer() != null) {
                                accessor.addExp(amount);
                                source.sendMessage(Text.literal("Added " + amount + " EXP."));
                                YaibaNetworking.syncDataToClient(source.getPlayer());
                                return 1;
                            }
                            return 0;
                        })));
    }
}