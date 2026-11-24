package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyClient;
import io.github.satxm.mcwifipnp.network.UPnPModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** 同 {@link ShareToLanScreenNewMixin}：UPnP 结果也发给还没进世界的房主。 */
@Mixin(UPnPModule.class)
public class UPnPModuleMixin {
    @Redirect(method = "run", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void lets_start_together$sendResult(ServerPlayer player, Component message) {
        if (player != null) {
            player.sendSystemMessage(message);
        } else {
            LobbyClient.showToHost(message);
        }
    }
}
