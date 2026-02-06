package ru.nelxigd.anticrash.mixin;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

    protected DeathScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onDeath(CallbackInfo ci) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
                if (client != null && client.player != null) {
                    client.player.requestRespawn();
                }
            } catch (Exception ignored) {
            }
        }).start();
    }
}