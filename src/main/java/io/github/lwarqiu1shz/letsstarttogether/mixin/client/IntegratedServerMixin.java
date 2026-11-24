package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 在小厅里开放联机。26.3 的 publishServer 只有一步要用房主的本地连接（准备聊天签名密钥），
 * 而小厅里房主还没进世界、没有这个连接。这时跳过这一步，其余照原版做；房主进世界时会自己准备密钥。
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @WrapOperation(method = "publishServer(Lnet/minecraft/server/MinecraftServer$MultiplayerScope;I)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;prepareKeyPair()V"))
    private void lets_start_together$publishFromLobby(@Nullable ClientPacketListener connection, Operation<Void> original) {
        if (connection != null) {
            original.call(connection);
        }
    }
}
