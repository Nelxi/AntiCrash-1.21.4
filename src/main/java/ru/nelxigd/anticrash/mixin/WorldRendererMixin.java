package ru.nelxigd.anticrash.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    private int limitRenderDistance(int original) {
        if (Anticrash.safeMode && original > 8) {
            return 8;
        }
        return Math.min(original, 32);
    }
}