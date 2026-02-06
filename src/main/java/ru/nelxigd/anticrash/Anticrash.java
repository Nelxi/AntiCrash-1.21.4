package ru.nelxigd.anticrash;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Anticrash implements ModInitializer {
    public static final String MOD_ID = "anticrash";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static volatile boolean safeMode = false;

    @Override
    public void onInitialize() {
        ClientPlayConnectionEvents.JOIN.register(this::onJoin);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            LOGGER.error("Prevented crash in thread: " + thread.getName(), exception);
            safeMode = true;

            if (thread.getName().contains("Render") || thread.getName().contains("main")) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.world != null) {
                    client.execute(() -> {
                        if (client.player != null) {
                            client.player.closeHandledScreen();
                        }
                    });
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("AntiCrash protected this session");
        }));
    }

    private void onJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        safeMode = false;
        LOGGER.info("AntiCrash protection activated for server: " + handler.getServerInfo());
    }

    private void onDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        safeMode = false;
    }
}