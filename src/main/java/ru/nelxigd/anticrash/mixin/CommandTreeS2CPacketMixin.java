package ru.nelxigd.anticrash.mixin;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.nelxigd.anticrash.Anticrash;

import java.util.Map;

@Mixin(CommandTreeS2CPacket.class)
public class CommandTreeS2CPacketMixin {

    @Inject(method = "getCommandTree", at = @At("HEAD"), cancellable = true)
    private void protectCommandTree(CommandNode<Object> commandNode, CallbackInfoReturnable<Map<CommandNode<Object>, Integer>> cir) {
        try {
            if (commandNode.getChildren().size() > 10000) {
                Anticrash.LOGGER.warn("Blocked oversized command tree");
                cir.cancel();
            }
        } catch (Exception e) {
            Anticrash.LOGGER.warn("Blocked malformed command tree");
            cir.cancel();
        }
    }
}