package ru.nelxigd.anticrash.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    private static final int MAX_NBT_SIZE = 2097152;

    @Inject(method = "copy", at = @At("HEAD"), cancellable = true)
    private void protectCopy(CallbackInfoReturnable<ItemStack> cir) {
        try {
            ItemStack self = (ItemStack)(Object)this;
            NbtComponent customData = self.get(DataComponentTypes.CUSTOM_DATA);

            if (customData != null) {
                String nbtString = customData.toString();
                if (nbtString.length() > MAX_NBT_SIZE) {
                    Anticrash.LOGGER.warn("Blocked oversized NBT in ItemStack copy: " + nbtString.length() + " bytes");
                    cir.setReturnValue(ItemStack.EMPTY);
                }
            }
        } catch (Exception e) {
            Anticrash.LOGGER.warn("Error checking ItemStack NBT: " + e.getMessage());
        }
    }
}