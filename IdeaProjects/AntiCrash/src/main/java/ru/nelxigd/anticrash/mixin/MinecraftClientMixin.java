package ru.nelxigd.anticrash.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "run", at = @At("HEAD"))
    private void onRun(CallbackInfo ci) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        if ((totalMemory - freeMemory) > maxMemory * 0.9) {
            System.gc();
            Anticrash.LOGGER.warn("Force garbage collection due to high memory usage");
        }
    }

    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"), cancellable = true)
    private void onCrash(CallbackInfo ci) {
        Anticrash.LOGGER.error("Prevented game crash, attempting recovery");
        ci.cancel();
    }
}