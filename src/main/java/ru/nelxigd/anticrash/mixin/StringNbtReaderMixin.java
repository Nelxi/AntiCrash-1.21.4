package ru.nelxigd.anticrash.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nelxigd.anticrash.Anticrash;

@Mixin(StringNbtReader.class)
public class StringNbtReaderMixin {

    private static final int MAX_NBT_DEPTH = 50;
    private static final int MAX_NBT_SIZE = 2000000;

    @Inject(method = "parse", at = @At("HEAD"), cancellable = true)
    private static void protectNbtParsing(String nbt, CallbackInfoReturnable<NbtCompound> cir) throws CommandSyntaxException {
        if (nbt == null || nbt.length() > MAX_NBT_SIZE) {
            Anticrash.LOGGER.warn("Blocked oversized NBT data: " + (nbt != null ? nbt.length() : "null"));
            cir.setReturnValue(new NbtCompound());
        }
    }
}