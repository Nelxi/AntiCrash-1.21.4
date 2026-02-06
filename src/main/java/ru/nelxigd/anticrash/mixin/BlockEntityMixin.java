package ru.nelxigd.anticrash.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    private static final int MAX_TILE_NBT_SIZE = 1048576;

    @Inject(method = "read", at = @At("HEAD"), cancellable = true)
    private void protectTileEntityRead(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (nbt != null && nbt.toString().length() > MAX_TILE_NBT_SIZE) {
            Anticrash.LOGGER.warn("Blocked oversized TileEntity NBT");
            ci.cancel();
        }
    }
}