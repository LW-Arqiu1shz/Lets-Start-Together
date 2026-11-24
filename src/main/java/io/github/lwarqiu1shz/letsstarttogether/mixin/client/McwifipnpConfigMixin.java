package io.github.lwarqiu1shz.letsstarttogether.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.satxm.mcwifipnp.Config;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 在小厅里打开 mcwifipnp 的设置界面。界面要读房主自己的游戏模式，
 * 可房主还没进世界，原版会返回 null，界面就建不出来。这时改用世界的默认游戏模式。
 */
@Mixin(value = Config.class, remap = false)
public abstract class McwifipnpConfigMixin {
    @ModifyExpressionValue(method = "readFromRunningServer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;getPersonalGameMode()Lnet/minecraft/world/level/GameType;"))
    private GameType lets_start_together$hostNotInWorldYet(@Nullable GameType personal, MinecraftServer server) {
        return personal != null ? personal : ((IntegratedServer) server).getWorldData().getGameType();
    }
}
