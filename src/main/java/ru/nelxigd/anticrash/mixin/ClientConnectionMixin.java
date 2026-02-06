package ru.nelxigd.anticrash.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Shadow private int packetsReceivedCounter;

    private final ConcurrentHashMap<Class<?>, Long> packetTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Integer> packetCounts = new ConcurrentHashMap<>();
    private static final int MAX_PACKET_SIZE = 2097152;
    private static final int MAX_PACKETS_PER_TICK = 1000;

    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (packet == null) {
            ci.cancel();
            return;
        }

        if (Anticrash.safeMode) {
            String packetName = packet.getClass().getSimpleName();
            if (!packetName.contains("KeepAlive") && !packetName.contains("Disconnect")) {
                ci.cancel();
                return;
            }
        }

        try {
            String packetName = packet.getClass().getSimpleName();

            if (packetName.contains("Bundle") || packetName.contains("Commands")) {
                Anticrash.LOGGER.debug("Processing potentially dangerous packet: " + packetName);
            }
        } catch (Throwable e) {
            Anticrash.LOGGER.warn("Blocked malformed packet: " + e.getMessage());
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
                Anticrash.LOGGER.warn("Rate limiting outgoing packet: " + packetClass.getSimpleName());
                ci.cancel();
                return;
            }
            packetCounts.put(packetClass, count + 1);
        } else {
            packetCounts.put(packetClass, 1);
        }

        packetTimestamps.put(packetClass, currentTime);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (packetsReceivedCounter > MAX_PACKETS_PER_TICK) {
            Anticrash.LOGGER.warn("Detected packet flood, entering safe mode");
            Anticrash.safeMode = true;
            packetsReceivedCounter = 0;
        }
    }
}