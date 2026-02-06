package ru.nelxigd.anticrash.mixin;

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    private static final int MAX_PARTICLES = 10000;
    private long lastParticleTime = 0;
    private int particleCount = 0;

    @Inject(method = "onParticle", at = @At("HEAD"), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastParticleTime > 1000) {
            particleCount = 0;
            lastParticleTime = currentTime;
        }

        particleCount += packet.getCount();

        if (particleCount > MAX_PARTICLES || packet.getCount() > 5000) {
            Anticrash.LOGGER.warn("Blocked excessive particles: " + packet.getCount());
            ci.cancel();
        }
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"), cancellable = true)
    private void protectGameJoin(CallbackInfo ci) {
        try {
            Thread.sleep(1);
        } catch (Exception e) {
            Anticrash.LOGGER.warn("Protected from game join exploit");
            ci.cancel();
        }
    }
}