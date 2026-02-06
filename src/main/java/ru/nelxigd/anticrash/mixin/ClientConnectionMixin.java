package ru.nelxigd.anticrash.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    private final ConcurrentHashMap<Class<?>, Long> packetTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Integer> packetCounts = new ConcurrentHashMap<>();

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet == null) {
            ci.cancel();
            return;
        }

        try {
            String packetName = packet.getClass().getSimpleName();

            if (packetName.contains("Explosion") || packetName.contains("GameStateChange")) {
                Anticrash.LOGGER.debug("Processing potentially dangerous packet: " + packetName);
            }
        } catch (Exception e) {
            Anticrash.LOGGER.warn("Blocked malformed packet");
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        if (packet == null) {
            ci.cancel();
            return;
        }

        Class<?> packetClass = packet.getClass();
        long currentTime = System.currentTimeMillis();

        Long lastTime = packetTimestamps.get(packetClass);
        if (lastTime != null && currentTime - lastTime < 50) {
            Integer count = packetCounts.getOrDefault(packetClass, 0);
            if (count > 20) {
                Anticrash.LOGGER.warn("Rate limiting packet: " + packetClass.getSimpleName());
                ci.cancel();
                return;
            }
            packetCounts.put(packetClass, count + 1);
        } else {
            packetCounts.put(packetClass, 1);
        }

        packetTimestamps.put(packetClass, currentTime);
    }
}