package ru.nelxigd.anticrash.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow public ClientWorld world;
    private long lastMemoryCheck = 0;
    private int crashAttempts = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(boolean tick, CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMemoryCheck > 5000) {
            lastMemoryCheck = currentTime;

            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            if (usedMemory > maxMemory * 0.85) {
                System.gc();
                Anticrash.LOGGER.warn("Force garbage collection due to high memory usage");

                if (world != null && world.getEntities() != null) {
                    int entityCount = 0;
                    for (Object ignored : world.getEntities()) {
                        entityCount++;
                        if (entityCount > 1000) {
                            Anticrash.LOGGER.warn("Too many entities loaded: 1000+");
                            Anticrash.safeMode = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"), cancellable = true)
    private void onCrash(CallbackInfo ci) {
        crashAttempts++;
        Anticrash.LOGGER.error("Prevented game crash #" + crashAttempts + ", attempting recovery");
        Anticrash.safeMode = true;

        if (crashAttempts < 3) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (Anticrash.safeMode) {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();

            if (usedMemory < maxMemory * 0.7) {
                Anticrash.safeMode = false;
                Anticrash.LOGGER.info("Safe mode disabled, memory usage normalized");
            }
        }
    }
}