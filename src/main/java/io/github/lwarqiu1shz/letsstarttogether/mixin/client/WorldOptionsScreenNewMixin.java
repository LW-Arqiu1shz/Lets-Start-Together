package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import io.github.lwarqiu1shz.letsstarttogether.client.LobbyClient;
import io.github.satxm.mcwifipnp.client.WorldOptionsScreenNew;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * mcwifipnp 开服后在后台线程里把 IP 发给房主这个玩家；小厅里房主还没进世界（找不到这个玩家），
 * 就改成直接放进房主的聊天栏（小厅会显示出来）。
 */
@Mixin(WorldOptionsScreenNew.class)
public class WorldOptionsScreenNewMixin {
    @Redirect(method = "lambda$GetPublicIP$0", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private static void lets_start_together$sendIp(ServerPlayer player, Component message) {
        if (player != null) {
            player.sendSystemMessage(message);
        } else {
            LobbyClient.showToHost(message);
        }
    }
}
