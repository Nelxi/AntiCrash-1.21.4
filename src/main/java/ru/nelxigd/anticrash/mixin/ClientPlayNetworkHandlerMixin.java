package ru.nelxigd.anticrash.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    private static final int MAX_PARTICLES_PER_SECOND = 10000;
    private static final int MAX_ENTITIES_PER_PACKET = 100;
    private static final int MAX_EXPLOSIONS_PER_SECOND = 10;
    private static final int MAX_SOUNDS_PER_SECOND = 50;
    private static final int MAX_TEXT_LENGTH = 32767;

    private final ConcurrentHashMap<String, AtomicInteger> packetCounters = new ConcurrentHashMap<>();
    private long lastResetTime = System.currentTimeMillis();

    private boolean checkRateLimit(String type, int maxPerSecond) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > 1000) {
            packetCounters.clear();
            lastResetTime = currentTime;
        }

        AtomicInteger counter = packetCounters.computeIfAbsent(type, k -> new AtomicInteger(0));
        return counter.incrementAndGet() > maxPerSecond;
    }

    @Inject(method = "onParticle", at = @At("HEAD"), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        if (packet.getCount() > 5000 || checkRateLimit("particle", MAX_PARTICLES_PER_SECOND)) {
            Anticrash.LOGGER.warn("Blocked excessive particles: " + packet.getCount());
            ci.cancel();
        }
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("explosion", MAX_EXPLOSIONS_PER_SECOND)) {
            Anticrash.LOGGER.warn("Blocked explosion spam");
            ci.cancel();
        }
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("sound", MAX_SOUNDS_PER_SECOND)) {
            Anticrash.LOGGER.warn("Blocked sound spam");
            ci.cancel();
        }
    }

    @Inject(method = "onEntitySpawn", at = @At("HEAD"), cancellable = true)
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("entity", MAX_ENTITIES_PER_PACKET)) {
            Anticrash.LOGGER.warn("Blocked entity spawn flood");
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        Text content = packet.content();
        if (content != null && content.getString().length() > MAX_TEXT_LENGTH) {
            Anticrash.LOGGER.warn("Blocked oversized game message");
            ci.cancel();
        }
    }

    @Inject(method = "onTitle", at = @At("HEAD"), cancellable = true)
    private void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("title", 5)) {
            Anticrash.LOGGER.warn("Blocked title spam");
            ci.cancel();
        }
    }

    @Inject(method = "onSubtitle", at = @At("HEAD"), cancellable = true)
    private void onSubtitle(SubtitleS2CPacket packet, CallbackInfo ci) {
        Text subtitle = packet.text();
        if (subtitle != null && subtitle.getString().length() > MAX_TEXT_LENGTH) {
            Anticrash.LOGGER.warn("Blocked oversized subtitle");
            ci.cancel();
        }
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        packetCounters.clear();
        Anticrash.safeMode = false;
    }

    @Inject(method = "onChunkData", at = @At("HEAD"), cancellable = true)
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("chunk", 100)) {
            Anticrash.LOGGER.warn("Blocked chunk data flood");
            ci.cancel();
        }
    }

    @Inject(method = "onWorldEvent", at = @At("HEAD"), cancellable = true)
    private void onWorldEvent(WorldEventS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("world_event", 100)) {
            Anticrash.LOGGER.warn("Blocked world event spam");
            ci.cancel();
        }
    }

    @Inject(method = "onBlockUpdate", at = @At("HEAD"), cancellable = true)
    private void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("block_update", 500)) {
            Anticrash.LOGGER.warn("Blocked block update flood");
            ci.cancel();
        }
    }

    @Inject(method = "onItemPickupAnimation", at = @At("HEAD"), cancellable = true)
    private void onItemPickup(ItemPickupAnimationS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("pickup", 100)) {
            Anticrash.LOGGER.warn("Blocked item pickup spam");
            ci.cancel();
        }
    }

    @Inject(method = "onEntityAnimation", at = @At("HEAD"), cancellable = true)
    private void onEntityAnimation(EntityAnimationS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("animation", 200)) {
            Anticrash.LOGGER.warn("Blocked animation spam");
            ci.cancel();
        }
    }

    @Inject(method = "onEntityPosition", at = @At("HEAD"), cancellable = true)
    private void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("entity_pos", 1000)) {
            Anticrash.LOGGER.warn("Blocked entity position flood");
            ci.cancel();
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onEntityVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("velocity", 500)) {
            Anticrash.LOGGER.warn("Blocked velocity update flood");
            ci.cancel();
        }
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("HEAD"), cancellable = true)
    private void onSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        if (checkRateLimit("slot_update", 200)) {
            Anticrash.LOGGER.warn("Blocked slot update spam");
            ci.cancel();
        }
    }
}